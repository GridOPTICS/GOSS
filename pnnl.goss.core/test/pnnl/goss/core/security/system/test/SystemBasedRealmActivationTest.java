package pnnl.goss.core.security.system.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pnnl.goss.core.security.JWTAuthenticationToken;
import pnnl.goss.core.security.SecurityConfig;
import pnnl.goss.core.security.system.SystemBasedRealm;

/**
 * Covers the GOSS-025 bootstrap failure: SystemBasedRealm populated its account
 * map only from Shiro's {@code onInit()} hook, and nothing in this OSGi
 * deployment ever calls {@code Initializable.init()}. Declarative Services
 * calls {@code @Activate}; Shiro's RealmSecurityManager.setRealms() reaches
 * only afterRealmsSet() -> applyCacheManagerToRealms(), never init(). So the
 * realm activated with a permanently empty userMap and every system/manager
 * authentication against it threw UnknownAccountException, which broke the
 * broker connect in GridOpticsServer.start().
 *
 * The load-bearing assertion in this class is that the realm resolves the
 * system account after DS activation WITHOUT any call to init(). Everything
 * else here guards the surrounding lifecycle: config reload, reference rebind,
 * and fail- closed behavior when the credential source is unusable.
 */
public class SystemBasedRealmActivationTest {

    private static final String SYSTEM_USER = "system";
    private static final String SYSTEM_PASSWORD = "manager";

    /**
     * The comma-separated permission list configured for the system principal.
     * Deliberately duplicated here as a literal rather than referenced from
     * production code: this test is the guard that the realm grants exactly these
     * permissions and no others.
     *
     * The realm must split this into individual Shiro permissions
     * ({@link #SYSTEM_PERMISSION_SET}). Storing the whole comma-joined string as
     * one permission is a bug: Shiro's WildcardPermission treats ',' as
     * alternatives within a single ':'-delimited part, so a single
     * "queue:*,topic:*,..." permission does not imply the concrete
     * "topic:ActiveMQ.Advisory.*:create" permissions the broker checks, and the
     * system principal is denied.
     */
    private static final String SYSTEM_PERMISSIONS = "queue:*,topic:*,temp-queue:*,fusion:*:read,fusion:*:write";

    /**
     * The individual permissions the realm must grant, one Shiro permission each.
     */
    private static final String[] SYSTEM_PERMISSION_SET = {"queue:*", "topic:*", "temp-queue:*", "fusion:*:read",
            "fusion:*:write"};

    /** Mutable SecurityConfig stub standing in for SecurityConfigImpl. */
    private static final class FakeSecurityConfig implements SecurityConfig {
        private String managerUser;
        private String managerPassword;

        FakeSecurityConfig(String managerUser, String managerPassword) {
            this.managerUser = managerUser;
            this.managerPassword = managerPassword;
        }

        @Override
        public String getManagerUser() {
            return managerUser;
        }

        @Override
        public String getManagerPassword() {
            return managerPassword;
        }

        @Override
        public boolean getUseToken() {
            return false;
        }

        @Override
        public boolean validateToken(String token) {
            return false;
        }

        @Override
        public JWTAuthenticationToken parseToken(String token) {
            return null;
        }

        @Override
        public String createToken(Object userId, Set<String> roles) {
            return null;
        }
    }

    /**
     * Simulates the Declarative Services bind callback for the securityConfig
     * reference. DS invokes bind methods before @Activate.
     */
    private static void bindSecurityConfig(SystemBasedRealm realm, SecurityConfig config) {
        realm.setSecurityConfig(config);
    }

    /** The DS-supplied component properties: systemrealm.cfg carries load=true. */
    private static Map<String, Object> componentProperties() {
        return Map.of("load", "true");
    }

    @Test
    @DisplayName("Realm resolves the system account after DS activation, with no call to Shiro init()")
    public void systemAccountResolvesAfterActivateWithoutShiroInit() throws Exception {
        SystemBasedRealm realm = new SystemBasedRealm();
        bindSecurityConfig(realm, new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD));

        // Exactly what the container does: @Activate, and nothing else. No init().
        realm.activate(componentProperties());

        assertThat(realm.hasIdentifier(SYSTEM_USER))
                .as("the realm must know the system principal after DS activation alone")
                .isTrue();

        AuthenticationInfo info = realm.getAuthenticationInfo(
                new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD));

        assertThat(info).as("system/manager must authenticate against the activated realm").isNotNull();
        assertThat(info.getPrincipals().getPrimaryPrincipal()).isEqualTo(SYSTEM_USER);
        assertThat(info.getCredentials()).isEqualTo(SYSTEM_PASSWORD);

        assertThat(info).isInstanceOf(SimpleAccount.class);
        SimpleAccount account = (SimpleAccount) info;
        assertThat(account.getStringPermissions())
                .as("the system account must carry each broker permission as a separate Shiro permission")
                .containsExactlyInAnyOrder(SYSTEM_PERMISSION_SET);

        assertThat(realm.getPermissions(SYSTEM_USER))
                .as("PermissionAdapter view must report the same split permissions")
                .containsExactlyInAnyOrder(SYSTEM_PERMISSION_SET);
    }

    @Test
    @DisplayName("The granted permission set is exactly the comma-separated source list, split")
    public void grantedPermissionsAreTheSourceListSplit() {
        // Guards the split invariant against the source-of-truth string: the set the
        // realm grants must be exactly SYSTEM_PERMISSIONS split on ',', with no part
        // dropped, merged, or left as the whole joined blob.
        assertThat(SYSTEM_PERMISSION_SET)
                .as("test's split set must match the configured permission string")
                .containsExactlyInAnyOrder(SYSTEM_PERMISSIONS.split(","));

        SystemBasedRealm realm = new SystemBasedRealm();
        bindSecurityConfig(realm, new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD));
        realm.activate(componentProperties());

        assertThat(realm.getPermissions(SYSTEM_USER))
                .as("the realm must not store the joined string as a single permission")
                .doesNotContain(SYSTEM_PERMISSIONS)
                .containsExactlyInAnyOrder(SYSTEM_PERMISSION_SET);
    }

    @Test
    @DisplayName("Unknown principals are still rejected after activation")
    public void unknownPrincipalIsRejected() throws Exception {
        SystemBasedRealm realm = new SystemBasedRealm();
        bindSecurityConfig(realm, new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD));
        realm.activate(componentProperties());

        assertThat(realm.hasIdentifier("intruder")).isFalse();
        assertThat(realm.getPermissions("intruder")).isEmpty();
        assertThat(realm.getAuthenticationInfo(new UsernamePasswordToken("intruder", "manager")))
                .as("the realm must not vend an account for an unconfigured principal")
                .isNull();
    }

    @Test
    @DisplayName("A wrong password for the system principal is rejected")
    public void wrongSystemPasswordIsRejected() throws Exception {
        SystemBasedRealm realm = new SystemBasedRealm();
        bindSecurityConfig(realm, new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD));
        realm.activate(componentProperties());

        assertThatThrownBy(() -> realm.getAuthenticationInfo(
                new UsernamePasswordToken(SYSTEM_USER, "not-the-password")))
                .isInstanceOf(IncorrectCredentialsException.class);
    }

    @Test
    @DisplayName("Shiro init() remains safe and idempotent if anything ever calls it")
    public void shiroInitIsIdempotentAfterActivation() throws Exception {
        SystemBasedRealm realm = new SystemBasedRealm();
        bindSecurityConfig(realm, new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD));
        realm.activate(componentProperties());

        realm.init();

        assertThat(realm.hasIdentifier(SYSTEM_USER)).isTrue();
        assertThat(realm.getPermissions(SYSTEM_USER)).containsExactlyInAnyOrder(SYSTEM_PERMISSION_SET);
        AuthenticationInfo info = realm.getAuthenticationInfo(
                new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD));
        assertThat(info.getPrincipals().getPrimaryPrincipal()).isEqualTo(SYSTEM_USER);
        assertThat(info.getCredentials()).isEqualTo(SYSTEM_PASSWORD);
    }

    @Test
    @DisplayName("A configuration update replaces the stored credential rather than keeping the stale one")
    public void modifiedReloadsChangedCredentials() throws Exception {
        SystemBasedRealm realm = new SystemBasedRealm();
        FakeSecurityConfig config = new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD);
        bindSecurityConfig(realm, config);
        realm.activate(componentProperties());

        config.managerPassword = "rotated";
        realm.updated(componentProperties());

        assertThatThrownBy(() -> realm.getAuthenticationInfo(
                new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD)))
                .as("the superseded password must no longer authenticate")
                .isInstanceOf(IncorrectCredentialsException.class);

        AuthenticationInfo info = realm.getAuthenticationInfo(
                new UsernamePasswordToken(SYSTEM_USER, "rotated"));
        assertThat(info).isNotNull();
        assertThat(info.getCredentials()).isEqualTo("rotated");
    }

    @Test
    @DisplayName("A renamed manager user does not leave the previous account behind")
    public void modifiedDoesNotLeaveStaleAccounts() throws Exception {
        SystemBasedRealm realm = new SystemBasedRealm();
        FakeSecurityConfig config = new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD);
        bindSecurityConfig(realm, config);
        realm.activate(componentProperties());

        config.managerUser = "operator";
        realm.updated(componentProperties());

        assertThat(realm.hasIdentifier(SYSTEM_USER))
                .as("the previous manager user must be evicted, not accumulated")
                .isFalse();
        assertThat(realm.getPermissions(SYSTEM_USER)).isEmpty();
        assertThat(realm.hasIdentifier("operator")).isTrue();
        assertThat(realm.getPermissions("operator")).containsExactlyInAnyOrder(SYSTEM_PERMISSION_SET);
    }

    @Test
    @DisplayName("A rebound SecurityConfig replaces the account rather than adding to it")
    public void rebindReplacesTheSystemAccount() {
        SystemBasedRealm realm = new SystemBasedRealm();
        FakeSecurityConfig first = new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD);
        bindSecurityConfig(realm, first);
        realm.activate(componentProperties());

        realm.setSecurityConfig(new FakeSecurityConfig("operator", "rotated"));
        realm.unsetSecurityConfig(first);

        assertThat(realm.hasIdentifier(SYSTEM_USER))
                .as("greedy rebind must not leave the previous credential source's account behind")
                .isFalse();
        assertThat(realm.hasIdentifier("operator")).isTrue();
        assertThat(realm.getPermissions("operator")).containsExactlyInAnyOrder(SYSTEM_PERMISSION_SET);

        AuthenticationInfo info = realm.getAuthenticationInfo(
                new UsernamePasswordToken("operator", "rotated"));
        assertThat(info.getCredentials()).isEqualTo("rotated");
    }

    @Test
    @DisplayName("An updated SecurityConfig service picks up a rotated credential without reactivation")
    public void updatedReferenceCallbackReloadsCredentials() {
        SystemBasedRealm realm = new SystemBasedRealm();
        FakeSecurityConfig config = new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD);
        bindSecurityConfig(realm, config);
        realm.activate(componentProperties());

        // pnnl.goss.security.cfg edited: SecurityConfigImpl's modified method runs
        // on the same instance, DS updates its service properties, and DS calls the
        // updated= callback on this reference.
        config.managerPassword = "rotated";
        realm.updatedSecurityConfig(config);

        assertThatThrownBy(() -> realm.getAuthenticationInfo(
                new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD)))
                .isInstanceOf(IncorrectCredentialsException.class);
        assertThat(realm.getAuthenticationInfo(new UsernamePasswordToken(SYSTEM_USER, "rotated"))
                .getCredentials()).isEqualTo("rotated");
    }

    @Test
    @DisplayName("Unbinding the credential source empties the realm rather than leaving it authenticating")
    public void unbindFailsClosed() {
        SystemBasedRealm realm = new SystemBasedRealm();
        FakeSecurityConfig config = new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD);
        bindSecurityConfig(realm, config);
        realm.activate(componentProperties());

        realm.unsetSecurityConfig(config);

        assertThat(realm.hasIdentifier(SYSTEM_USER)).isFalse();
        assertThat(realm.getPermissions(SYSTEM_USER)).isEmpty();
        assertThat(realm.getAuthenticationInfo(new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD)))
                .isNull();
    }

    @Test
    @DisplayName("Wiring fails closed with an empty realm when the manager user is absent")
    public void wiringFailsClosedWithoutManagerUser() {
        SystemBasedRealm realm = new SystemBasedRealm();

        assertThatThrownBy(() -> realm.setSecurityConfig(new FakeSecurityConfig(null, SYSTEM_PASSWORD)))
                .as("a missing manager user must abort DS wiring, not publish an empty realm")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("goss.system.manager");

        assertThat(realm.hasIdentifier(SYSTEM_USER)).isFalse();
        assertThat(realm.getAuthenticationInfo(new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD)))
                .isNull();
        assertThatThrownBy(() -> realm.activate(componentProperties()))
                .as("activation must not paper over the unusable credential source either")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("goss.system.manager");
    }

    @Test
    @DisplayName("Wiring fails closed with an empty realm when the manager password is blank")
    public void wiringFailsClosedWithoutManagerPassword() {
        SystemBasedRealm realm = new SystemBasedRealm();

        assertThatThrownBy(() -> realm.setSecurityConfig(new FakeSecurityConfig(SYSTEM_USER, "   ")))
                .as("a blank manager password must abort DS wiring, not publish an empty realm")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("goss.system.manager.password");

        assertThat(realm.hasIdentifier(SYSTEM_USER)).isFalse();
        assertThat(realm.getAuthenticationInfo(new UsernamePasswordToken(SYSTEM_USER, "   ")))
                .isNull();
    }

    @Test
    @DisplayName("Activation with no bound SecurityConfig fails closed instead of NPEing into an empty realm")
    public void activationFailsClosedWithoutSecurityConfig() {
        SystemBasedRealm realm = new SystemBasedRealm();

        assertThatThrownBy(() -> realm.activate(componentProperties()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SecurityConfig");

        assertThat(realm.hasIdentifier(SYSTEM_USER)).isFalse();
    }

    @Test
    @DisplayName("A configuration update that breaks the credential source empties the realm")
    public void modifiedFailsClosedOnBrokenConfig() {
        SystemBasedRealm realm = new SystemBasedRealm();
        FakeSecurityConfig config = new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD);
        bindSecurityConfig(realm, config);
        realm.activate(componentProperties());

        config.managerPassword = null;
        assertThatThrownBy(() -> realm.updated(componentProperties()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("goss.system.manager.password");

        assertThat(realm.hasIdentifier(SYSTEM_USER))
                .as("a failed reload must not leave the superseded account authenticating")
                .isFalse();
    }

    @Test
    @DisplayName("Component properties are never treated as the credential source")
    public void componentPropertiesAreNotACredentialSource() throws Exception {
        SystemBasedRealm realm = new SystemBasedRealm();
        bindSecurityConfig(realm, new FakeSecurityConfig(SYSTEM_USER, SYSTEM_PASSWORD));

        // systemrealm.cfg carries only load=true; a null property map must not
        // change which principal the realm serves.
        assertThatCode(() -> realm.activate(Collections.emptyMap())).doesNotThrowAnyException();

        assertThat(realm.hasIdentifier(SYSTEM_USER)).isTrue();
        assertThat(realm.hasIdentifier("load")).isFalse();
        assertThat(realm.getPermissions(SYSTEM_USER)).containsExactlyInAnyOrder(SYSTEM_PERMISSION_SET);
    }
}
