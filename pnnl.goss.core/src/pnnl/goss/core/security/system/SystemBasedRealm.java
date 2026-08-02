package pnnl.goss.core.security.system;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.GossPermissionResolver;
import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.SecurityConfig;

/**
 * This class handles property based authentication/authorization. It will only
 * be started as a component if a pnnl.goss.core.security.systemrealm.cfg file
 * exists within the configuration directory.
 *
 * The format of each property should be
 * username=password,permission1,permission2 ... where permission1 and
 * permission2 are of the format domain:object:action. There can be multiple
 * levels of domain object and action. An example permission string format is
 * printers:lp2def:create or topic:request:subscribe.
 *
 * NOTE: This class assumes uniqueness of username in the properties file.
 *
 * The realm.type=system service property is the distinguishing marker that lets
 * ordering-sensitive consumers (the SecurityManager Activator and
 * GridOpticsServer) select this realm with a target filter, so Declarative
 * Services can gate their activation on the system-authenticating realm rather
 * than on "some realm". See GADP-012 / issue #1882: without this marker the
 * AT_LEAST_ONE realm guard let GridOpticsServer connect as system/manager
 * before this realm was wired.
 *
 * LIFECYCLE (GOSS-025): the account map is loaded from {@link #activate}, which
 * Declarative Services is guaranteed to call. It used to be loaded only from
 * Shiro's {@code onInit()} hook, which is unreachable in this deployment:
 * {@code onInit()} is protected and called only from
 * {@code Initializable.init()}, normally driven by
 * {@code LifecycleUtils.init()} out of an ini/Spring bootstrap. Nothing in
 * goss-core, in Felix DS, or in shiro-core 2.0.0's own wiring calls it for a
 * DS-managed realm: {@code RealmSecurityManager.setRealms()} reaches only
 * {@code afterRealmsSet() -> applyCacheManagerToRealms()}. The realm therefore
 * activated with a permanently empty map and the broker connect in
 * GridOpticsServer.start() failed with UnknownAccountException for "system".
 * {@code onInit()} is retained and delegates to the same idempotent loader, so
 * the realm stays correct if any future host does drive Shiro's lifecycle.
 *
 * @author Craig Allwardt
 *
 */
@Component(service = GossRealm.class, configurationPid = "pnnl.goss.core.security.systemrealm", configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
        GossRealm.REALM_TYPE_PROPERTY + "=" + GossRealm.SYSTEM_REALM_TYPE})
public class SystemBasedRealm extends AuthorizingRealm implements GossRealm {

    private static final Logger log = LoggerFactory.getLogger(SystemBasedRealm.class);

    /**
     * The permission string granted to the system principal. Consumed verbatim by
     * the ActiveMQ Shiro authorization plugin through
     * {@link pnnl.goss.core.security.impl.GossWildcardPermissionResolver}; changing
     * it changes what the broker connection is allowed to do.
     */
    private static final String SYSTEM_PERMISSIONS = "queue:*,topic:*,temp-queue:*,fusion:*:read,fusion:*:write";

    private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();

    @Reference
    GossPermissionResolver gossPermissionResolver;

    private volatile SecurityConfig securityConfig;

    /**
     * The credential source, bound through methods rather than a field so this
     * component is notified when it changes.
     *
     * The manager user and password do not come from this component's own PID
     * (pnnl.goss.core.security.systemrealm carries only load=true); they come from
     * the pnnl.goss.security PID by way of SecurityConfigImpl. A FileInstall edit
     * to pnnl.goss.security.cfg calls SecurityConfigImpl's modified method, which
     * leaves the same service instance registered with updated service properties.
     * A field reference would give this component no callback for that, so the
     * realm would keep serving a superseded credential. The updated= callback below
     * is the DS 1.3+ hook for exactly that case, and the dynamic bind reloads on
     * service replacement.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, unbind = "unsetSecurityConfig", updated = "updatedSecurityConfig")
    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
        loadSystemAccount();
    }

    /**
     * Called when the bound SecurityConfig's service properties change, which DS
     * does after SecurityConfigImpl's modified method runs for a
     * pnnl.goss.security.cfg edit. Reloads so a rotated manager credential takes
     * effect without a bundle restart.
     */
    public void updatedSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
        loadSystemAccount();
    }

    /**
     * Fails closed on unbind: the account map is emptied so this realm cannot keep
     * authenticating against a credential source that is gone. The reference is
     * mandatory, so DS deactivates this component (and transitively the
     * SecurityManager and GridOpticsServer) when no replacement exists. The
     * identity check keeps a greedy bind-then-unbind swap from wiping the account
     * that the new service just loaded.
     */
    public void unsetSecurityConfig(SecurityConfig securityConfig) {
        if (this.securityConfig == securityConfig) {
            this.securityConfig = null;
            clearAccounts();
            log.warn("SecurityConfig unbound from SystemBasedRealm; system account cleared. "
                    + "This realm cannot authenticate the system principal until a SecurityConfig rebinds.");
        }
    }

    /**
     * Rebuilds the system account from the bound {@link SecurityConfig}.
     *
     * The map is cleared before it is rebuilt, so a rename of the manager user
     * cannot leave the previous principal behind and a failure cannot leave a
     * half-built or stale account in place.
     *
     * @throws IllegalStateException
     *             when no SecurityConfig is bound or the configured manager
     *             credential is missing. Thrown rather than logged-and-swallowed so
     *             the failure is fail-closed: propagated out of activate() it
     *             aborts DS activation, this realm's service is never published,
     *             the Activator's mandatory realm.type=system reference stays
     *             unsatisfied, the SecurityManager service is never published, and
     *             GridOpticsServer never opens a broker connection against an empty
     *             realm.
     */
    private synchronized void loadSystemAccount() {
        clearAccounts();

        SecurityConfig config = this.securityConfig;
        if (config == null) {
            throw new IllegalStateException("SystemBasedRealm cannot build the system account: no SecurityConfig "
                    + "service is bound. Check that pnnl.goss.security.cfg exists and that SecurityConfigImpl "
                    + "activated.");
        }

        String managerUser = config.getManagerUser();
        if (managerUser == null || managerUser.trim().isEmpty()) {
            throw new IllegalStateException("SystemBasedRealm cannot build the system account: property "
                    + "goss.system.manager is missing or blank in pnnl.goss.security.cfg.");
        }

        String managerPassword = config.getManagerPassword();
        if (managerPassword == null || managerPassword.trim().isEmpty()) {
            throw new IllegalStateException("SystemBasedRealm cannot build the system account for user '"
                    + managerUser + "': property goss.system.manager.password is missing or blank in "
                    + "pnnl.goss.security.cfg.");
        }

        SimpleAccount account = new SimpleAccount(managerUser, managerPassword, getName());
        account.addStringPermission(SYSTEM_PERMISSIONS);

        Set<String> perms = new HashSet<>();
        perms.add(SYSTEM_PERMISSIONS);

        userMap.put(managerUser, account);
        userPermissions.put(managerUser, perms);

        log.info("SystemBasedRealm loaded the system account for manager user '{}'", managerUser);
    }

    private void clearAccounts() {
        userMap.clear();
        userPermissions.clear();
    }

    /**
     * Shiro's Initializable hook. Nothing drives it in this OSGi deployment (see
     * the class comment), but it is kept wired to the same idempotent loader so the
     * realm is correct under either entry point rather than silently empty under
     * one of them.
     */
    @Override
    protected void onInit() {
        super.onInit();
        loadSystemAccount();
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        log.info("Activating SystemBasedRealm");
        // The component properties (systemrealm.cfg carries only load=true) are not
        // the credential source; the bound SecurityConfig is. Reload anyway so
        // activation never depends on bind-callback ordering.
        loadSystemAccount();
    }

    @Modified
    public synchronized void updated(Map<String, Object> properties) {
        log.info("Reloading SystemBasedRealm after a configuration update");
        loadSystemAccount();
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {

        // get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);
        return userMap.get(username);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {
        // we can safely cast to a UsernamePasswordToken here, because this class
        // 'supports' UsernamePasswordToken
        // objects. See the Realm.supports() method if your application will use a
        // different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        upToken.setRememberMe(true);
        return userMap.get(upToken.getUsername());
    }

    @Override
    public Set<String> getPermissions(String identifier) {
        if (hasIdentifier(identifier)) {
            return userPermissions.get(identifier);
        }
        return new HashSet<>();
    }

    @Override
    public boolean hasIdentifier(String identifier) {
        return userMap.containsKey(identifier);
    }

    @Override
    public PermissionResolver getPermissionResolver() {
        if (gossPermissionResolver != null)
            return gossPermissionResolver;
        else
            return super.getPermissionResolver();
    }
}
