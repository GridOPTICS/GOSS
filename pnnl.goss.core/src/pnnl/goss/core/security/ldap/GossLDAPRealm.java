package pnnl.goss.core.security.ldap;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.security.GossPermissionResolver;
import pnnl.goss.core.security.GossRealm;

/**
 * LDAP-based authentication realm for GOSS.
 *
 * This component only activates when a configuration file exists
 * (pnnl.goss.core.security.ldap.cfg) with enabled=true and the LDAP server is
 * reachable.
 *
 * Example configuration:
 *
 * <pre>
 * enabled=true
 * ldap.url=ldap://localhost:10389
 * ldap.userDnTemplate=uid={0},ou=users,ou=goss,ou=system
 * ldap.systemUsername=uid=admin,ou=system
 * ldap.systemPassword=secret
 * ldap.connectionTimeout=5000
 * </pre>
 */
@Component(service = GossRealm.class, configurationPid = "pnnl.goss.core.security.ldap", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class GossLDAPRealm extends DefaultLdapRealm implements GossRealm {

    private static final Logger log = LoggerFactory.getLogger(GossLDAPRealm.class);

    private static final String PROP_ENABLED = "enabled";
    private static final String PROP_LDAP_URL = "ldap.url";
    private static final String PROP_USER_DN_TEMPLATE = "ldap.userDnTemplate";
    private static final String PROP_SYSTEM_USERNAME = "ldap.systemUsername";
    private static final String PROP_SYSTEM_PASSWORD = "ldap.systemPassword";
    private static final String PROP_CONNECTION_TIMEOUT = "ldap.connectionTimeout";

    private static final String DEFAULT_USER_DN_TEMPLATE = "uid={0},ou=users,ou=goss,ou=system";
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    @Reference
    private GossPermissionResolver gossPermissionResolver;

    private boolean enabled = false;
    private String ldapUrl = null;

    public GossLDAPRealm() {
        // Don't configure in constructor - wait for configuration
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        log.info("Activating GossLDAPRealm");
        configure(properties);
    }

    @Deactivate
    public void deactivate() {
        log.info("Deactivating GossLDAPRealm");
        enabled = false;
    }

    private void configure(Map<String, Object> properties) {
        if (properties == null) {
            log.warn("No configuration provided for LDAP realm");
            enabled = false;
            return;
        }

        // Check if enabled
        String enabledStr = getStringProperty(properties, PROP_ENABLED, "false");
        if (!"true".equalsIgnoreCase(enabledStr)) {
            log.info("LDAP realm is disabled by configuration");
            enabled = false;
            return;
        }

        // Get LDAP URL
        ldapUrl = getStringProperty(properties, PROP_LDAP_URL, null);
        if (ldapUrl == null || ldapUrl.isEmpty()) {
            log.warn("LDAP URL not configured - LDAP realm will not be active");
            enabled = false;
            return;
        }

        // Get connection timeout
        int connectionTimeout = getIntProperty(properties, PROP_CONNECTION_TIMEOUT,
                DEFAULT_CONNECTION_TIMEOUT);

        // Test connectivity before enabling
        if (!isLdapServerReachable(ldapUrl, connectionTimeout)) {
            log.warn("LDAP server at {} is not reachable - LDAP realm will not be active", ldapUrl);
            enabled = false;
            return;
        }

        // Configure the realm
        String userDnTemplate = getStringProperty(properties, PROP_USER_DN_TEMPLATE,
                DEFAULT_USER_DN_TEMPLATE);
        String systemUsername = getStringProperty(properties, PROP_SYSTEM_USERNAME, null);
        String systemPassword = getStringProperty(properties, PROP_SYSTEM_PASSWORD, null);

        try {
            setUserDnTemplate(userDnTemplate);

            JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
            contextFactory.setUrl(ldapUrl);

            if (systemUsername != null && !systemUsername.isEmpty()) {
                contextFactory.setSystemUsername(systemUsername);
            }
            if (systemPassword != null && !systemPassword.isEmpty()) {
                contextFactory.setSystemPassword(systemPassword);
            }

            setContextFactory(contextFactory);
            enabled = true;
            log.info("LDAP realm configured: url={}, userDnTemplate={}", ldapUrl, userDnTemplate);

        } catch (Exception e) {
            log.error("Failed to configure LDAP realm", e);
            enabled = false;
        }
    }

    private boolean isLdapServerReachable(String url, int timeoutMs) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            int port = uri.getPort();

            if (port == -1) {
                port = "ldaps".equalsIgnoreCase(uri.getScheme()) ? 636 : 389;
            }

            log.debug("Testing LDAP connectivity to {}:{}", host, port);

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeoutMs);
                log.debug("LDAP server {}:{} is reachable", host, port);
                return true;
            }
        } catch (Exception e) {
            log.debug("LDAP server {} is not reachable: {}", url, e.getMessage());
            return false;
        }
    }

    private String getStringProperty(Map<String, Object> props, String key, String defaultVal) {
        Object value = props.get(key);
        if (value instanceof String && !((String) value).isEmpty()) {
            return (String) value;
        }
        return defaultVal;
    }

    private int getIntProperty(Map<String, Object> props, String key, int defaultVal) {
        Object value = props.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                // Fall through
            }
        }
        return defaultVal;
    }

    @Override
    public Set<String> getPermissions(String identifier) {
        if (!enabled) {
            return new HashSet<>();
        }
        log.debug("LDAP getPermissions for: {}", identifier);
        // TODO: Implement LDAP-based permission lookup
        return new HashSet<>();
    }

    @Override
    public boolean hasIdentifier(String identifier) {
        if (!enabled) {
            return false;
        }
        log.debug("LDAP hasIdentifier: {}", identifier);
        return false;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (!enabled) {
            return null;
        }

        log.debug("LDAP doGetAuthorizationInfo for principals: {}", principals);
        AuthorizationInfo info = super.doGetAuthorizationInfo(principals);

        if (info == null) {
            // Provide default permissions for authenticated LDAP users
            SimpleAuthorizationInfo defaultInfo = new SimpleAuthorizationInfo();
            defaultInfo.addRole("user");
            defaultInfo.addStringPermission("queue:*");
            defaultInfo.addStringPermission("temp-queue:*");
            defaultInfo.addStringPermission("topic:*");
            info = defaultInfo;
        }

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        if (!enabled) {
            log.debug("LDAP realm not enabled, skipping authentication");
            return null;
        }

        log.debug("LDAP authentication attempt for: {}", token.getPrincipal());
        try {
            AuthenticationInfo info = super.doGetAuthenticationInfo(token);
            if (info != null) {
                log.info("LDAP authentication successful for: {}", token.getPrincipal());
            }
            return info;
        } catch (AuthenticationException e) {
            log.debug("LDAP authentication failed for {}: {}", token.getPrincipal(), e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        if (!enabled) {
            return false;
        }
        boolean supports = super.supports(token);
        log.debug("LDAP supports token {}: {}", token.getClass().getSimpleName(), supports);
        return supports;
    }

    @Modified
    public synchronized void updated(Map<String, Object> properties) throws SystemException {
        log.info("Updating GossLDAPRealm configuration");
        configure(properties);
    }

    @Override
    public PermissionResolver getPermissionResolver() {
        if (gossPermissionResolver != null) {
            return gossPermissionResolver;
        }
        return super.getPermissionResolver();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }
}
