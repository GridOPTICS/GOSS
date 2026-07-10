package pnnl.goss.core.security.impl.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.impl.Activator;

/**
 * Verifies the activation-order contract that closes GADP-012 / issue #1882.
 *
 * The regression: GridOpticsServer.start() authenticated the system/manager
 * principal against the Shiro SecurityManager before the system-authenticating
 * realm was wired, because the SecurityManager was published as soon as ANY
 * realm bound (an AT_LEAST_ONE guard) and two components both wrote the realm
 * set. When only a realm that returns null for "system" was present, startup
 * died.
 *
 * The fix has two parts, both asserted here: 1. Ordering gate: the system realm
 * carries a realm.type=system service property, and the SecurityManager
 * component (Activator) holds a mandatory, target-filtered reference on it, so
 * Declarative Services cannot publish the SecurityManager until the system
 * realm is bound. 2. Single writer: SecurityManagerRealmHandler no longer
 * writes the shared SecurityManager's realm set, so it cannot clobber the
 * system realm.
 *
 * True multi-bundle activation ordering across a running Felix container is an
 * integration concern (Pax Exam / a boot on Armando's machine), not a unit
 * concern; that coverage gap is stated in the class-level note and in the
 * report. These tests assert the wiring and authentication invariants that the
 * container ordering depends on.
 */
public class SystemRealmActivationOrderTest {

    private static final String SYSTEM_USER = "system";
    private static final String SYSTEM_PASSWORD = "manager";

    /**
     * A GossRealm that authenticates the system principal with the "*" permission,
     * mirroring SystemBasedRealm's production behavior without its ConfigAdmin
     * wiring.
     */
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

    /**
     * A GossRealm that intentionally returns null for the system principal,
     * mirroring UnauthTokenBasedRealm's deliberate refusal to authenticate "system"
     * (UnauthTokenBasedRealm.java:143-144). This models the realm that won the race
     * in the #1882 failure.
     */
    private static final class UnauthLikeRealm extends AuthorizingRealm implements GossRealm {
        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            if (SYSTEM_USER.equals(upToken.getUsername())) {
                return null;
            }
            return new SimpleAccount(upToken.getUsername(), new String(upToken.getPassword()), getName());
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            return null;
        }

        @Override
        public Set<String> getPermissions(String identifier) {
            return new HashSet<>();
        }

        @Override
        public boolean hasIdentifier(String identifier) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static ServiceReference<GossRealm> realmRef() {
        return mock(ServiceReference.class);
    }

    @Test
    @DisplayName("SystemBasedRealm publishes the realm.type=system service property the gate filters on")
    public void systemRealmPublishesDistinguishingProperty() throws Exception {
        Document doc = descriptor("pnnl.goss.core.security-system.jar",
                "pnnl.goss.core.security.system.SystemBasedRealm");
        NodeList properties = doc.getElementsByTagName("property");
        boolean hasMarker = false;
        for (int i = 0; i < properties.getLength(); i++) {
            Element p = (Element) properties.item(i);
            if ("realm.type".equals(p.getAttribute("name")) && "system".equals(p.getAttribute("value"))) {
                hasMarker = true;
            }
        }
        assertThat(hasMarker)
                .as("system realm descriptor must publish realm.type=system so the target filter can select it")
                .isTrue();
    }

    @Test
    @DisplayName("SecurityManager component gates activation on the system realm via a mandatory target filter")
    public void activatorReferencesSystemRealmWithTargetFilter() throws Exception {
        Document doc = descriptor("pnnl.goss.core.goss-core-security.jar",
                "pnnl.goss.core.security.impl.Activator");
        Element systemRef = referenceByName(doc, "systemRealm");
        assertNotNull(systemRef,
                "Activator descriptor must declare a systemRealm reference so DS can gate @Activate on it");
        assertThat(systemRef.getAttribute("target"))
                .as("reference must select the system realm specifically, not any realm")
                .isEqualTo("(realm.type=system)");
        assertThat(systemRef.getAttribute("interface"))
                .isEqualTo("pnnl.goss.core.security.GossRealm");
        // A missing cardinality attribute means the DS default 1..1 (mandatory),
        // which is what forces the ordering: DS will not activate until it binds.
        String cardinality = systemRef.getAttribute("cardinality");
        assertThat(cardinality.isEmpty() || "1..1".equals(cardinality))
                .as("systemRealm reference must be mandatory (1..1); was '%s'", cardinality)
                .isTrue();
    }

    @Test
    @DisplayName("SecurityManagerRealmHandler no longer references the SecurityManager (single-writer invariant)")
    public void realmHandlerIsNotASecondRealmWriter() throws Exception {
        Document doc = descriptor("pnnl.goss.core.goss-core-security.jar",
                "pnnl.goss.core.security.impl.SecurityManagerRealmHandler");
        NodeList refs = doc.getElementsByTagName("reference");
        for (int i = 0; i < refs.getLength(); i++) {
            Element ref = (Element) refs.item(i);
            assertThat(ref.getAttribute("interface"))
                    .as("handler must not reference the SecurityManager; the Activator is the sole realm writer")
                    .isNotEqualTo("org.apache.shiro.mgt.SecurityManager");
        }
    }

    @Test
    @DisplayName("System principal fails to authenticate when only the unauth-style realm is wired (the #1882 failure)")
    public void systemAuthFailsWithoutSystemRealm() {
        Activator securityManager = new Activator();
        securityManager.realmAdded(realmRef(), new UnauthLikeRealm());
        securityManager.activate();

        assertThatThrownBy(() -> securityManager.authenticate(
                new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD)))
                .as("without the system realm in the set, system auth must fail; this is the race symptom")
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("System principal authenticates once the system realm is in the wired realm set")
    public void systemAuthSucceedsWithSystemRealmWired() {
        Activator securityManager = new Activator();
        securityManager.realmAdded(realmRef(), new UnauthLikeRealm());
        securityManager.realmAdded(realmRef(), new FakeSystemRealm());
        securityManager.activate();

        AuthenticationInfo info = securityManager.authenticate(
                new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD));

        assertNotNull(info, "system/manager must authenticate against the wired realm set");
        assertThat(info.getPrincipals().getPrimaryPrincipal()).isEqualTo(SYSTEM_USER);

        RealmSecurityManager rsm = securityManager;
        assertThat(rsm.getRealms())
                .as("the final realm set the broker authenticates against must contain the system realm")
                .anyMatch(r -> r instanceof FakeSystemRealm);
    }

    @Test
    @DisplayName("System realm survives a subsequent realm binding (no double-writer clobber)")
    public void systemRealmSurvivesLaterRealmBinding() {
        Activator securityManager = new Activator();
        securityManager.realmAdded(realmRef(), new FakeSystemRealm());
        securityManager.activate();
        // A later realm binding must extend, never replace, the realm set.
        securityManager.realmAdded(realmRef(), new UnauthLikeRealm());

        RealmSecurityManager rsm = securityManager;
        assertThat(rsm.getRealms()).anyMatch(r -> r instanceof FakeSystemRealm);
        assertThat(securityManager.authenticate(
                new UsernamePasswordToken(SYSTEM_USER, SYSTEM_PASSWORD)))
                .as("system auth must still succeed after another realm binds")
                .isNotNull();

        Realm systemRealm = rsm.getRealms().stream()
                .filter(r -> r instanceof FakeSystemRealm).findFirst().orElseThrow();
        assertThat(((FakeSystemRealm) systemRealm).getPermissions(SYSTEM_USER)).contains("*");
    }

    /**
     * Loads the generated Declarative Services descriptor for a component from a
     * built bundle jar. DS annotations are CLASS-retention and invisible to runtime
     * reflection, so the bnd-emitted OSGI-INF XML is the authoritative runtime
     * contract to assert against. The 'goss.generated.dir' system property is set
     * by the test task, which depends on the jar task, so under Gradle the bundle
     * is guaranteed to exist: a missing bundle there is a real failure, not a skip.
     * Only when the property is unset (an IDE run that bypasses the Gradle test
     * task and its 'jar' dependency) is the case genuinely indeterminate, and this
     * falls back to a skip.
     */
    private Document descriptor(String bundleName, String componentName) throws Exception {
        String generatedDir = System.getProperty("goss.generated.dir");
        assumeTrue(generatedDir != null, "goss.generated.dir not set; run via the Gradle test task");
        File bundle = new File(generatedDir, bundleName);
        if (!bundle.isFile()) {
            fail("bundle not built: " + bundle
                    + " (goss.generated.dir is set, so the 'jar' task ran; a missing bundle here"
                    + " is a real build/wiring failure, not a skip)");
        }

        try (JarFile jar = new JarFile(bundle)) {
            String entryName = "OSGI-INF/" + componentName + ".xml";
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry == null) {
                // Fall back to scanning OSGI-INF for a descriptor naming the component.
                entry = findByComponentName(jar, componentName);
            }
            assertNotNull(entry, "no DS descriptor for " + componentName + " in " + bundleName);
            try (InputStream in = jar.getInputStream(entry)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                return factory.newDocumentBuilder().parse(in);
            }
        }
    }

    private JarEntry findByComponentName(JarFile jar, String componentName) throws IOException {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            if (e.getName().startsWith("OSGI-INF/") && e.getName().endsWith(".xml")
                    && e.getName().contains(componentName)) {
                return e;
            }
        }
        return null;
    }

    private Element referenceByName(Document doc, String refName) {
        NodeList refs = doc.getElementsByTagName("reference");
        for (int i = 0; i < refs.getLength(); i++) {
            Node node = refs.item(i);
            if (node instanceof Element) {
                Element ref = (Element) node;
                if (refName.equals(ref.getAttribute("name"))) {
                    return ref;
                }
            }
        }
        return null;
    }
}
