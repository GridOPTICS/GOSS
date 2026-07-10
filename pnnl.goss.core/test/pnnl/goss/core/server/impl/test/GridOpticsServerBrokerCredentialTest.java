package pnnl.goss.core.server.impl.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pnnl.goss.core.server.impl.GridOpticsServer;

/**
 * GADP-014 / issue #42: the configured broker system-manager credential
 * (goss.system.manager / goss.system.manager.password) was read via
 * getProperty(...) but the returned values were discarded, so
 * createConnection(...) always used the literal "system"/"manager" regardless
 * of configuration.
 *
 * These tests assert the fix at the field level that createConnection(...)
 * reads from: systemManagerUser / systemManagerPassword. GridOpticsServer has
 * no OSGi container in a unit test, so updated(Map) is invoked directly (the
 * same method @Activate's start(Map) delegates to before createConnection), and
 * the resulting private fields are read via reflection since the class exposes
 * no getters for them.
 */
public class GridOpticsServerBrokerCredentialTest {

    private static final String PROP_SYSTEM_MANAGER = "goss.system.manager";
    private static final String PROP_SYSTEM_MANAGER_PASSWORD = "goss.system.manager.password";

    private String readSystemManagerUser(GridOpticsServer server) throws Exception {
        return readPrivateField(server, "systemManagerUser");
    }

    private String readSystemManagerPassword(GridOpticsServer server) throws Exception {
        return readPrivateField(server, "systemManagerPassword");
    }

    private String readPrivateField(GridOpticsServer server, String fieldName) throws Exception {
        Field field = GridOpticsServer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(server);
    }

    @Test
    @DisplayName("Configured goss.system.manager / .password are honored when set")
    public void configuredCredentialIsHonoredWhenSet() throws Exception {
        GridOpticsServer server = new GridOpticsServer();
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROP_SYSTEM_MANAGER, "configuredUser");
        properties.put(PROP_SYSTEM_MANAGER_PASSWORD, "configuredPassword");

        server.updated(properties);

        assertThat(readSystemManagerUser(server))
                .as("the configured goss.system.manager value must reach the connection principal")
                .isEqualTo("configuredUser");
        assertThat(readSystemManagerPassword(server))
                .as("the configured goss.system.manager.password value must reach the connection principal")
                .isEqualTo("configuredPassword");
    }

    @Test
    @DisplayName("system/manager defaults are used when the credential properties are absent")
    public void defaultsToSystemManagerWhenPropertiesAbsent() throws Exception {
        GridOpticsServer server = new GridOpticsServer();
        Map<String, Object> properties = new HashMap<>();
        // Deliberately omit PROP_SYSTEM_MANAGER / PROP_SYSTEM_MANAGER_PASSWORD:
        // a deployment that never sets these must see unchanged behavior.

        server.updated(properties);

        assertThat(readSystemManagerUser(server))
                .as("absent goss.system.manager must default to 'system', preserving prior behavior")
                .isEqualTo("system");
        assertThat(readSystemManagerPassword(server))
                .as("absent goss.system.manager.password must default to 'manager', preserving prior behavior")
                .isEqualTo("manager");
    }

    @Test
    @DisplayName("empty-string credential properties fall back to the system/manager default")
    public void defaultsToSystemManagerWhenPropertiesEmpty() throws Exception {
        GridOpticsServer server = new GridOpticsServer();
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROP_SYSTEM_MANAGER, "");
        properties.put(PROP_SYSTEM_MANAGER_PASSWORD, "");

        server.updated(properties);

        assertThat(readSystemManagerUser(server)).isEqualTo("system");
        assertThat(readSystemManagerPassword(server)).isEqualTo("manager");
    }
}
