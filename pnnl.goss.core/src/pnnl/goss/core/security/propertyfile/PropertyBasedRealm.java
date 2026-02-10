package pnnl.goss.core.security.propertyfile;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.security.GossPermissionResolver;
import pnnl.goss.core.security.GossRealm;

/**
 * This class handles property based authentication/authorization. It will only
 * be started as a component if a pnnl.goss.core.security.properties.cfg file
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
 * @author Craig Allwardt
 *
 */
@Component(service = GossRealm.class, configurationPid = "pnnl.goss.core.security.propertyfile", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PropertyBasedRealm extends AuthorizingRealm implements GossRealm {

    private static final Logger log = LoggerFactory.getLogger(PropertyBasedRealm.class);

    private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();

    @Reference
    GossPermissionResolver gossPermissionResolver;

    @Activate
    public void activate(Map<String, Object> properties) {
        log.info("Activating PropertyBasedRealm");
        updated(properties);
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
        String username = upToken.getUsername();
        if (username == null) {
            log.warn("Authentication attempt with null username (client may be using "
                    + "token-based auth against a server without token support)");
            return null;
        }
        return userMap.get(username);
    }

    @Modified
    public synchronized void updated(Map<String, Object> properties) throws SystemException {

        if (properties != null) {
            log.debug("Updating PropertyBasedRealm with {} properties", properties.size());
            userMap.clear();
            userPermissions.clear();

            for (String k : properties.keySet()) {
                // Skip OSGi/ConfigAdmin metadata properties
                if (k.startsWith("service.") || k.startsWith("component.") ||
                        k.startsWith("felix.") || k.equals("osgi.ds.satisfying.condition.target")) {
                    continue;
                }

                Object value = properties.get(k);
                // Only process String values (skip Long, Boolean, etc.)
                if (!(value instanceof String)) {
                    log.debug("Skipping non-string property: {} = {} ({})", k, value,
                            value != null ? value.getClass().getName() : "null");
                    continue;
                }

                String v = (String) value;
                String[] credAndPermissions = v.split(",");

                SimpleAccount acnt = new SimpleAccount(k, credAndPermissions[0], getName());
                Set<String> perms = new HashSet<>();
                for (int i = 1; i < credAndPermissions.length; i++) {
                    acnt.addStringPermission(credAndPermissions[i]);
                    perms.add(credAndPermissions[i]);
                }
                userMap.put(k, acnt);
                userPermissions.put(k, perms);
                log.debug("Loaded user: {} with {} permissions", k, perms.size());
            }
            log.info("PropertyBasedRealm configured with {} users", userMap.size());
        }
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
