package pnnl.goss.core.itests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.EnabledIf;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.server.ServerControl;

/**
 * OSGi-based end-to-end integration tests. These tests run inside an OSGi
 * framework and use the actual GOSS services.
 *
 * Run with: ./gradlew :pnnl.goss.core.itests:testOSGi
 */
@TestInstance(Lifecycle.PER_CLASS)
public class GossOSGiEndToEndTest {

    private static final String OPENWIRE_URI = "tcp://localhost:61616";
    private static final String STOMP_URI = "stomp://localhost:61613";
    private static final int TEST_TIMEOUT_MS = 10000;
    private static final int SERVICE_TIMEOUT_MS = 30000;

    private ServerControl serverControl;
    private ClientFactory clientFactory;
    private boolean serverStarted = false;

    /**
     * Check if running in OSGi environment
     */
    boolean isOSGiEnvironment() {
        try {
            BundleContext ctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            return ctx != null;
        } catch (Exception e) {
            return false;
        }
    }

    protected BundleContext getBundleContext() {
        try {
            return FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        } catch (Exception e) {
            return null;
        }
    }

    protected <T> T getService(Class<T> clazz, long timeoutMs) throws Exception {
        BundleContext context = getBundleContext();
        if (context == null) {
            return null;
        }

        long endTime = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < endTime) {
            ServiceReference<T> ref = context.getServiceReference(clazz);
            if (ref != null) {
                T service = context.getService(ref);
                if (service != null) {
                    return service;
                }
            }
            Thread.sleep(100);
        }
        return null;
    }

    protected void configureServer() throws Exception {
        ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class, SERVICE_TIMEOUT_MS);
        if (configAdmin == null) {
            System.out.println("ConfigurationAdmin not available - using defaults");
            return;
        }

        // Configure server
        Configuration serverConfig = configAdmin.getConfiguration("pnnl.goss.core.server", null);
        Dictionary<String, Object> serverProps = new Hashtable<>();
        serverProps.put("goss.openwire.uri", OPENWIRE_URI);
        serverProps.put("goss.stomp.uri", STOMP_URI);
        serverProps.put("goss.start.broker", "true");
        serverProps.put("goss.broker.uri", "tcp://0.0.0.0:61616");
        serverConfig.update(serverProps);

        // Configure client
        Configuration clientConfig = configAdmin.getConfiguration("pnnl.goss.core.client", null);
        Dictionary<String, Object> clientProps = new Hashtable<>();
        clientProps.put("goss.openwire.uri", OPENWIRE_URI);
        clientProps.put("goss.stomp.uri", STOMP_URI);
        clientConfig.update(clientProps);

        // Give time for configuration to propagate
        Thread.sleep(500);
    }

    @BeforeAll
    public void setUp() throws Exception {
        if (!isOSGiEnvironment()) {
            System.out.println("Not in OSGi environment - skipping OSGi tests");
            return;
        }

        System.out.println("Setting up OSGi end-to-end tests...");

        // Configure the server
        configureServer();

        // Get ServerControl service
        serverControl = getService(ServerControl.class, SERVICE_TIMEOUT_MS);
        if (serverControl == null) {
            System.out.println("ServerControl service not available");
            return;
        }

        // Get ClientFactory service
        clientFactory = getService(ClientFactory.class, SERVICE_TIMEOUT_MS);
        if (clientFactory == null) {
            System.out.println("ClientFactory service not available");
        }

        // Start the server
        if (!serverControl.isRunning()) {
            System.out.println("Starting GOSS server...");
            serverControl.start();
            serverStarted = true;

            // Wait for server to be fully started
            Thread.sleep(2000);
            System.out.println("GOSS server started");
        } else {
            System.out.println("GOSS server already running");
            serverStarted = true;
        }
    }

    @AfterAll
    public void tearDown() {
        if (serverControl != null && serverStarted && serverControl.isRunning()) {
            System.out.println("Stopping GOSS server...");
            try {
                serverControl.stop();
                System.out.println("GOSS server stopped");
            } catch (Exception e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }
    }

    @Test
    @EnabledIf("isOSGiEnvironment")
    public void testServerIsRunning() {
        assertNotNull(serverControl, "ServerControl should be available");
        assertTrue(serverControl.isRunning(), "Server should be running");
    }

    @Test
    @EnabledIf("isOSGiEnvironment")
    public void testClientFactoryAvailable() {
        assertNotNull(clientFactory, "ClientFactory should be available");
    }

    @Test
    @EnabledIf("isOSGiEnvironment")
    public void testGossClientConnection() throws Exception {
        GossClient client = new GossClient(
                PROTOCOL.OPENWIRE,
                null,
                OPENWIRE_URI,
                STOMP_URI);

        try {
            client.createSession();
            assertNotNull(client.getClientId(), "Client should have an ID");
            System.out.println("GossClient connected: " + client.getClientId());
        } finally {
            client.close();
        }
    }

    @Test
    @EnabledIf("isOSGiEnvironment")
    public void testPublishSubscribe() throws Exception {
        String topicName = "test/osgi/pubsub";
        String testMessage = "Hello from OSGi test!";

        GossClient client = new GossClient(
                PROTOCOL.OPENWIRE,
                null,
                OPENWIRE_URI,
                STOMP_URI);

        try {
            client.createSession();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> receivedMessage = new AtomicReference<>();

            client.subscribe(topicName, new GossResponseEvent() {
                @Override
                public void onMessage(Serializable response) {
                    System.out.println("Received: " + response);
                    receivedMessage.set(response.toString());
                    latch.countDown();
                }
            });

            Thread.sleep(200);

            client.publish(topicName, testMessage);
            System.out.println("Published: " + testMessage);

            boolean received = latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            assertTrue(received, "Should receive message");
            assertTrue(receivedMessage.get().contains(testMessage),
                    "Message should contain: " + testMessage);

        } finally {
            client.close();
        }
    }

    @Test
    @EnabledIf("isOSGiEnvironment")
    public void testMultipleClients() throws Exception {
        String topicName = "test/osgi/multi";
        String testMessage = "Broadcast to all";

        GossClient publisher = new GossClient(PROTOCOL.OPENWIRE, null, OPENWIRE_URI, STOMP_URI);
        GossClient subscriber1 = new GossClient(PROTOCOL.OPENWIRE, null, OPENWIRE_URI, STOMP_URI);
        GossClient subscriber2 = new GossClient(PROTOCOL.OPENWIRE, null, OPENWIRE_URI, STOMP_URI);

        try {
            publisher.createSession();
            subscriber1.createSession();
            subscriber2.createSession();

            CountDownLatch latch = new CountDownLatch(2);
            AtomicReference<String> msg1 = new AtomicReference<>();
            AtomicReference<String> msg2 = new AtomicReference<>();

            subscriber1.subscribe(topicName, response -> {
                msg1.set(response.toString());
                latch.countDown();
            });

            subscriber2.subscribe(topicName, response -> {
                msg2.set(response.toString());
                latch.countDown();
            });

            Thread.sleep(200);

            publisher.publish(topicName, testMessage);

            boolean received = latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            assertTrue(received, "Both subscribers should receive message");
            assertTrue(msg1.get().contains(testMessage), "Subscriber 1: " + msg1.get());
            assertTrue(msg2.get().contains(testMessage), "Subscriber 2: " + msg2.get());

        } finally {
            publisher.close();
            subscriber1.close();
            subscriber2.close();
        }
    }

    @Test
    @EnabledIf("isOSGiEnvironment")
    public void testClientReconnection() throws Exception {
        // First connection
        GossClient client1 = new GossClient(PROTOCOL.OPENWIRE, null, OPENWIRE_URI, STOMP_URI);
        client1.createSession();
        String id1 = client1.getClientId();
        client1.close();

        // Second connection
        GossClient client2 = new GossClient(PROTOCOL.OPENWIRE, null, OPENWIRE_URI, STOMP_URI);
        client2.createSession();
        String id2 = client2.getClientId();

        try {
            assertNotEquals(id1, id2, "Each client should have unique ID");

            // Verify second client works
            String topicName = "test/osgi/reconnect";
            CountDownLatch latch = new CountDownLatch(1);
            client2.subscribe(topicName, response -> latch.countDown());
            Thread.sleep(100);
            client2.publish(topicName, "test");

            assertTrue(latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS));

        } finally {
            client2.close();
        }
    }
}
