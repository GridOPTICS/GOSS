package pnnl.goss.core.security.impl.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceReference;

import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.impl.Activator;

/**
 * Covers the shutdown-path defect seen alongside GOSS-025: removing the last
 * GossRealm made registerAllRealms() call Shiro's
 * RealmSecurityManager.setRealms() with an empty collection, which throws
 * IllegalArgumentException("Realms collection argument cannot be empty."). DS
 * reported it as "The realmRemoved method has thrown an exception" on every
 * teardown.
 *
 * Shiro offers no way to install an empty realm set, so the guard must decline
 * the write and log it. The realm set the SecurityManager still holds is not a
 * widened authorization surface: the Activator's realm reference is
 * AT_LEAST_ONE, so DS deactivates this component when the last realm goes, and
 * the SecurityManager service is unpublished with it.
 */
public class ActivatorRealmRemovalTest {

    private static final String SYSTEM_USER = "system";
    private static final String SYSTEM_PASSWORD = "manager";

    private static final class FakeSystemRealm extends AuthorizingRealm implements GossRealm {
        private final SimpleAccount account;

        FakeSystemRealm() {
            this.account = new SimpleAccount(SYSTEM_USER, SYSTEM_PASSWORD, getName());
            this.account.addStringPermission("*");
        }

        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            return SYSTEM_USER.equals(upToken.getUsername()) ? account : null;
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            String username = (String) getAvailablePrincipal(principals);
            return SYSTEM_USER.equals(username) ? account : null;
        }

        @Override
        public Set<String> getPermissions(String identifier) {
            return SYSTEM_USER.equals(identifier) ? Set.of("*") : new HashSet<>();
        }

        @Override
        public boolean hasIdentifier(String identifier) {
            return SYSTEM_USER.equals(identifier);
        }
    }

    @SuppressWarnings("unchecked")
    private static ServiceReference<GossRealm> realmRef() {
        return mock(ServiceReference.class);
    }

    @Test
    @DisplayName("Removing the last realm does not throw out of realmRemoved")
    public void removingLastRealmDoesNotThrow() {
        Activator securityManager = new Activator();
        ServiceReference<GossRealm> ref = realmRef();
        securityManager.realmAdded(ref, new FakeSystemRealm());
        securityManager.activate();

        assertThatCode(() -> securityManager.realmRemoved(ref))
                .as("teardown of the final realm must not surface IllegalArgumentException")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Removing one of several realms still narrows the registered realm set")
    public void removingOneOfSeveralRealmsStillRewritesTheSet() {
        Activator securityManager = new Activator();
        ServiceReference<GossRealm> first = realmRef();
        ServiceReference<GossRealm> second = realmRef();
        FakeSystemRealm keep = new FakeSystemRealm();
        securityManager.realmAdded(first, new FakeSystemRealm());
        securityManager.realmAdded(second, keep);
        securityManager.activate();

        securityManager.realmRemoved(first);

        RealmSecurityManager rsm = securityManager;
        assertThat(rsm.getRealms())
                .as("the removed realm must be gone and the surviving realm retained")
                .containsExactly(keep);
    }

    @Test
    @DisplayName("Removing an unknown realm reference is a no-op on the registered set")
    public void removingUnknownReferenceIsANoOp() {
        Activator securityManager = new Activator();
        ServiceReference<GossRealm> known = realmRef();
        FakeSystemRealm realm = new FakeSystemRealm();
        securityManager.realmAdded(known, realm);
        securityManager.activate();

        securityManager.realmRemoved(realmRef());

        RealmSecurityManager rsm = securityManager;
        assertThat(rsm.getRealms()).containsExactly(realm);
    }
}
