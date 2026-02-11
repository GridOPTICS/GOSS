package pnnl.goss.core.itests;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.client.GossClient;

/**
 * Integration tests that connect to an already-running GOSS server process.
 *
 * Unlike {@link GossEndToEndTest} which embeds its own ActiveMQ broker, these
 * tests hit a real GOSS server externally — the same way Python/STOMP clients
 * (and the companion test_stomp_token_auth.py) connect.
 *
 * This validates the full stack: ActiveMQ transport, Shiro authentication,
 * message routing, and pub/sub — from a GossClient over OpenWire.
 *
 * Configuration (system properties or environment variables): goss.openwire.uri
 * / GOSS_OPENWIRE_URI (default: tcp://localhost:61617) goss.stomp.uri /
 * GOSS_STOMP_URI (default: stomp://localhost:61618) goss.username /
 * GOSS_USERNAME (default: system) goss.password / GOSS_PASSWORD (default:
 * manager)
 *
 * Run: ./gradlew :pnnl.goss.core.itests:testExternal
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class GossExternalServerTest {

    private static final int TEST_TIMEOUT_MS = 10000;

    private String openwireUri;
    private String stompUri;
    private String username;
    private String password;

    /**
     * Read config from system properties, falling back to env vars, then defaults.
     */
    private static String config(String sysProp, String envVar, String defaultVal) {
        String val = System.getProperty(sysProp);
        if (val != null && !val.isEmpty())
            return val;
        val = System.getenv(envVar);
        if (val != null && !val.isEmpty())
            return val;
        return defaultVal;
    }

    @BeforeAll
    public void setUp() {
        openwireUri = config("goss.openwire.uri", "GOSS_OPENWIRE_URI", "tcp://localhost:61617");
        stompUri = config("goss.stomp.uri", "GOSS_STOMP_URI", "stomp://localhost:61618");
        username = config("goss.username", "GOSS_USERNAME", "system");
        password = config("goss.password", "GOSS_PASSWORD", "manager");

        System.out.println("GossExternalServerTest targeting:");
        System.out.println("  OpenWire: " + openwireUri);
        System.out.println("  STOMP:    " + stompUri);
        System.out.println("  User:     " + username);

        // Skip the entire class if the server is not reachable
        assumeTrue(isServerReachable(),
                "GOSS server not reachable at " + openwireUri + " — skipping external tests");
    }

    /** Quick TCP probe to see if the OpenWire port is listening. */
    private boolean isServerReachable() {
        try {
            String host = openwireUri.replaceAll("^tcp://", "").split(":")[0];
            int port = Integer.parseInt(openwireUri.replaceAll("^tcp://", "").split(":")[1]);
            try (Socket s = new Socket(host, port)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /** Helper: create a connected GossClient with credentials. */
    private GossClient connect() throws Exception {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        GossClient client = new GossClient(PROTOCOL.OPENWIRE, creds, openwireUri, stompUri);
        client.createSession();
        return client;
    }

    // ------------------------------------------------------------------
    // Tests mirror GossOSGiEndToEndTest + GossEndToEndTest, but against
    // an external server process.
    // ------------------------------------------------------------------

    @Test
    public void test01_clientConnection() throws Exception {
        GossClient client = connect();
        try {
            assertNotNull(client.getClientId(), "Client should have an ID");
            System.out.println("Connected with ID: " + client.getClientId());
        } finally {
            client.close();
        }
    }

    @Test
    public void test02_publishSubscribe() throws Exception {
        String topic = "test/external/pubsub";
        String message = "Hello from external Java test!";

        GossClient client = connect();
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> received = new AtomicReference<>();

            client.subscribe(topic, new GossResponseEvent() {
                @Override
                public void onMessage(Serializable response) {
                    received.set(response.toString());
                    latch.countDown();
                }
            });

            Thread.sleep(200);
            client.publish(topic, message);

            assertTrue(latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    "Should receive published message");
            assertTrue(received.get().contains(message),
                    "Received should contain: " + message + " — got: " + received.get());
        } finally {
            client.close();
        }
    }

    @Test
    public void test03_multipleSubscribers() throws Exception {
        String topic = "test/external/multi";
        String message = "Broadcast to all";

        GossClient publisher = connect();
        GossClient sub1 = connect();
        GossClient sub2 = connect();

        try {
            CountDownLatch latch = new CountDownLatch(2);
            AtomicReference<String> msg1 = new AtomicReference<>();
            AtomicReference<String> msg2 = new AtomicReference<>();

            sub1.subscribe(topic, response -> {
                msg1.set(response.toString());
                latch.countDown();
            });
            sub2.subscribe(topic, response -> {
                msg2.set(response.toString());
                latch.countDown();
            });

            Thread.sleep(200);
            publisher.publish(topic, message);

            assertTrue(latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    "Both subscribers should receive");
            assertTrue(msg1.get().contains(message), "Sub1: " + msg1.get());
            assertTrue(msg2.get().contains(message), "Sub2: " + msg2.get());
        } finally {
            publisher.close();
            sub1.close();
            sub2.close();
        }
    }

    @Test
    public void test04_clientReconnection() throws Exception {
        // First connection
        GossClient client1 = connect();
        String id1 = client1.getClientId();
        client1.close();

        // Second connection
        GossClient client2 = connect();
        String id2 = client2.getClientId();

        try {
            assertNotEquals(id1, id2, "Each connection should have a unique ID");

            // Verify second client works
            String topic = "test/external/reconnect";
            CountDownLatch latch = new CountDownLatch(1);
            client2.subscribe(topic, response -> latch.countDown());
            Thread.sleep(100);
            client2.publish(topic, "after reconnect");

            assertTrue(latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    "Should work after reconnection");
        } finally {
            client2.close();
        }
    }

    @Test
    public void test05_publishJsonData() throws Exception {
        String topic = "test/external/json";
        String jsonMessage = "{\"name\":\"test\",\"value\":42}";

        GossClient client = connect();
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> received = new AtomicReference<>();

            client.subscribe(topic, response -> {
                received.set(response.toString());
                latch.countDown();
            });

            Thread.sleep(200);
            client.publish(topic, jsonMessage);

            assertTrue(latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    "Should receive JSON data");
            assertTrue(received.get().contains("test") && received.get().contains("42"),
                    "JSON should survive round-trip: " + received.get());
        } finally {
            client.close();
        }
    }

    @Test
    public void test06_multipleTopics() throws Exception {
        String topicA = "test/external/topicA";
        String topicB = "test/external/topicB";

        GossClient client = connect();
        try {
            CountDownLatch latch = new CountDownLatch(2);
            AtomicReference<String> msgA = new AtomicReference<>();
            AtomicReference<String> msgB = new AtomicReference<>();

            client.subscribe(topicA, response -> {
                msgA.set(response.toString());
                latch.countDown();
            });
            client.subscribe(topicB, response -> {
                msgB.set(response.toString());
                latch.countDown();
            });

            Thread.sleep(200);
            client.publish(topicA, "Message for A");
            client.publish(topicB, "Message for B");

            assertTrue(latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    "Should receive on both topics");
            assertTrue(msgA.get().contains("Message for A"), "Topic A: " + msgA.get());
            assertTrue(msgB.get().contains("Message for B"), "Topic B: " + msgB.get());
        } finally {
            client.close();
        }
    }

    @Test
    public void test07_stompProtocolFallback() throws Exception {
        // GossClient with STOMP protocol uses OpenWire internally; verify it works
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        GossClient client = new GossClient(PROTOCOL.STOMP, creds, openwireUri, stompUri);

        try {
            client.createSession();
            assertNotNull(client.getClientId());
            assertEquals(PROTOCOL.STOMP, client.getProtocol());
        } finally {
            client.close();
        }
    }
}
