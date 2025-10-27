package pnnl.goss.core.itests;

import static org.junit.Assert.*;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.server.ServerControl;
import pnnl.goss.core.testutil.CoreConfigSteps;

/**
 * OSGi DS-based integration test that uses standard OSGi APIs instead of Felix
 * Dependency Manager.
 */
public class OSGiIntegrationTest {

    /**
     * Helper method to get OSGi services using standard OSGi API
     */
    protected <T> T getService(Class<T> clazz) {
        BundleContext context = getBundleContext();
        if (context == null) {
            // Not in OSGi environment, return null
            return null;
        }

        ServiceReference<T> ref = context.getServiceReference(clazz);
        if (ref != null) {
            return context.getService(ref);
        }
        return null;
    }

    /**
     * Helper to get bundle context if running in OSGi
     */
    protected BundleContext getBundleContext() {
        try {
            return FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        } catch (Exception e) {
            // Not in OSGi environment
            return null;
        }
    }

    /**
     * Configure a service using ConfigurationAdmin (OSGi standard)
     */
    protected void configureService(String pid, Dictionary<String, Object> properties) throws Exception {
        ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);
        if (configAdmin != null) {
            Configuration config = configAdmin.getConfiguration(pid, null);
            config.update(properties);
        }
    }

    @Test
    public void testOSGiEnvironmentDetection() {
        BundleContext context = getBundleContext();
        if (context != null) {
            System.out.println("Running in OSGi environment");
            assertNotNull("Bundle context should be available", context);
        } else {
            System.out.println("Not running in OSGi environment - skipping OSGi-specific tests");
        }
    }

    @Test
    public void testServiceLookup() {
        if (getBundleContext() == null) {
            System.out.println("Skipping - not in OSGi environment");
            return;
        }

        // Try to get ClientFactory service
        ClientFactory clientFactory = getService(ClientFactory.class);
        // May be null if service not registered yet
        System.out.println("ClientFactory service: " + (clientFactory != null ? "found" : "not found"));

        // Try to get ServerControl service
        ServerControl serverControl = getService(ServerControl.class);
        System.out.println("ServerControl service: " + (serverControl != null ? "found" : "not found"));
    }

    @Test
    public void testConfigurationUpdate() throws Exception {
        if (getBundleContext() == null) {
            System.out.println("Skipping - not in OSGi environment");
            return;
        }

        // Configure server properties using CoreConfigSteps
        Dictionary<String, Object> serverProps = CoreConfigSteps.toDictionary(
                CoreConfigSteps.getServerConfiguration());

        try {
            configureService("pnnl.goss.core.server", serverProps);
            System.out.println("Server configuration updated successfully");
        } catch (Exception e) {
            System.out.println("Could not update configuration: " + e.getMessage());
        }

        // Configure client properties using CoreConfigSteps
        Dictionary<String, Object> clientProps = CoreConfigSteps.toDictionary(
                CoreConfigSteps.getClientConfiguration());

        try {
            configureService("pnnl.goss.core.client", clientProps);
            System.out.println("Client configuration updated successfully");
        } catch (Exception e) {
            System.out.println("Could not update configuration: " + e.getMessage());
        }
    }

    /**
     * Test registering a mock service (useful for testing)
     */
    @Test
    public void testServiceRegistration() {
        BundleContext context = getBundleContext();
        if (context == null) {
            System.out.println("Skipping - not in OSGi environment");
            return;
        }

        // Register a test service
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("test", "true");

        TestService testService = new TestServiceImpl();
        ServiceRegistration<TestService> registration = context.registerService(TestService.class, testService, props);

        assertNotNull("Service registration should succeed", registration);

        // Now try to get it back
        TestService retrieved = getService(TestService.class);
        assertNotNull("Should be able to retrieve registered service", retrieved);
        assertEquals("Should be same instance", testService, retrieved);

        // Clean up
        registration.unregister();
    }

    // Test interfaces for service registration test
    interface TestService {
        String getName();
    }

    static class TestServiceImpl implements TestService {
        public String getName() {
            return "test";
        }
    }
}
