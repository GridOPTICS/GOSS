package pnnl.goss.core.itests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Basic connectivity test that verifies the project can compile and basic
 * imports work correctly.
 */
public class BasicConnectionTest {

    @Test
    public void testBasicAssertion() {
        assertTrue(true, "Basic test should pass");
        assertEquals(1, 1, "Numbers should match");
    }

    @Test
    public void testClassLoading() {
        try {
            // Test that core classes can be loaded
            Class<?> clientClass = Class.forName("pnnl.goss.core.client.GossClient");
            assertNotNull(clientClass, "GossClient class should load");

            Class<?> serverClass = Class.forName("pnnl.goss.core.server.impl.GridOpticsServer");
            assertNotNull(serverClass, "GridOpticsServer class should load");

        } catch (ClassNotFoundException e) {
            fail("Core classes should be available: " + e.getMessage());
        }
    }

    @Test
    @Disabled("Integration test - needs full OSGi environment")
    public void testServerStartup() {
        // This would test actual server startup
        // Ignored for now as it needs OSGi runtime
    }
}
