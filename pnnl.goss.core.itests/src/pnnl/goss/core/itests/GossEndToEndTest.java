package pnnl.goss.core.itests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import jakarta.jms.*;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.GossClient;

/**
 * End-to-end integration tests for GOSS client-server communication. These
 * tests run outside of OSGi for simpler CI execution.
 *
 * Tests verify: - Client connection to broker - Request/response patterns -
 * Pub/sub messaging - Multiple protocol support
 */
@TestInstance(Lifecycle.PER_CLASS)
public class GossEndToEndTest {

    private BrokerService brokerService;
    private static final String OPENWIRE_URI = "tcp://localhost:61620";
    private static final String STOMP_URI = "stomp://localhost:61621";
    private static final int TEST_TIMEOUT_MS = 10000;

    @BeforeAll
    public void setUpBroker() throws Exception {
        System.out.println("Starting test broker...");

        brokerService = new BrokerService();
        brokerService.setBrokerName("goss-test-broker");
        brokerService.setDataDirectory("target/activemq-test-data");
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);

        // OpenWire connector
        TransportConnector openwireConnector = new TransportConnector();
        openwireConnector.setUri(new URI("tcp://0.0.0.0:61620"));
        openwireConnector.setName("openwire");
        brokerService.addConnector(openwireConnector);

        // STOMP connector
        TransportConnector stompConnector = new TransportConnector();
        stompConnector.setUri(new URI("stomp://0.0.0.0:61621"));
        stompConnector.setName("stomp");
        brokerService.addConnector(stompConnector);

        brokerService.start();
        brokerService.waitUntilStarted();

        System.out.println("Test broker started on ports 61620 (OpenWire) and 61621 (STOMP)");
    }

    @AfterAll
    public void tearDownBroker() {
        try {
            if (brokerService != null) {
                brokerService.stop();
                brokerService.waitUntilStopped();
                System.out.println("Test broker stopped");
            }
        } catch (Exception e) {
            System.err.println("Error stopping broker: " + e.getMessage());
        }
    }

    @Test
    public void testGossClientConnection() throws Exception {
        // Create GossClient with OpenWire protocol
        GossClient client = new GossClient(
                PROTOCOL.OPENWIRE,
                null, // no credentials for test
                OPENWIRE_URI,
                STOMP_URI);

        try {
            // Create session
            client.createSession();

            // Verify client is connected (session created)
            assertNotNull(client.getClientId(), "Client should have an ID");
            assertEquals(PROTOCOL.OPENWIRE, client.getProtocol(), "Protocol should be OPENWIRE");

            System.out.println("GossClient connected successfully with ID: " + client.getClientId());
        } finally {
            client.close();
        }
    }

    @Test
    public void testGossClientWithCredentials() throws Exception {
        // Create credentials
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("testuser", "testpass");

        // Create GossClient with credentials
        GossClient client = new GossClient(
                PROTOCOL.OPENWIRE,
                credentials,
                OPENWIRE_URI,
                STOMP_URI);

        try {
            client.createSession();
            assertNotNull(client.getClientId(), "Client should have an ID");
            System.out.println("GossClient with credentials connected: " + client.getClientId());
        } finally {
            client.close();
        }
    }

    @Test
    public void testPublishSubscribe() throws Exception {
        String topicName = "test/pubsub/topic";
        String testMessage = "Hello from pub/sub test!";

        // Create client
        GossClient client = new GossClient(
                PROTOCOL.OPENWIRE,
                null,
                OPENWIRE_URI,
                STOMP_URI);

        try {
            client.createSession();

            // Set up latch and message holder for async reception
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> receivedMessage = new AtomicReference<>();

            // Subscribe to topic
            client.subscribe(topicName, new GossResponseEvent() {
                @Override
                public void onMessage(Serializable response) {
                    System.out.println("Received message: " + response);
                    receivedMessage.set(response.toString());
                    latch.countDown();
                }
            });

            // Give subscriber time to register
            Thread.sleep(200);

            // Publish message
            client.publish(topicName, testMessage);
            System.out.println("Published: " + testMessage);

            // Wait for message
            boolean received = latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            assertTrue(received, "Should receive published message within timeout");
            // GossClient wraps messages in DataResponse JSON format
            assertTrue(receivedMessage.get().contains(testMessage),
                    "Received message should contain published content: " + receivedMessage.get());

        } finally {
            client.close();
        }
    }

    @Test
    public void testMultipleSubscribers() throws Exception {
        String topicName = "test/multi/subscribers";
        String testMessage = "Broadcast message";

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

            // Subscribe both clients
            subscriber1.subscribe(topicName, response -> {
                msg1.set(response.toString());
                latch.countDown();
            });

            subscriber2.subscribe(topicName, response -> {
                msg2.set(response.toString());
                latch.countDown();
            });

            Thread.sleep(200);

            // Publish
            publisher.publish(topicName, testMessage);

            // Wait for both
            boolean received = latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            assertTrue(received, "Both subscribers should receive message");
            // GossClient wraps messages in DataResponse JSON format
            assertTrue(msg1.get().contains(testMessage), "Subscriber 1 should get message: " + msg1.get());
            assertTrue(msg2.get().contains(testMessage), "Subscriber 2 should get message: " + msg2.get());

        } finally {
            publisher.close();
            subscriber1.close();
            subscriber2.close();
        }
    }

    @Test
    public void testStompProtocolFallback() throws Exception {
        // When STOMP protocol is selected, GossClient should use OpenWire internally
        // but still work correctly
        GossClient client = new GossClient(
                PROTOCOL.STOMP,
                null,
                OPENWIRE_URI,
                STOMP_URI);

        try {
            client.createSession();

            // Should connect successfully (using OpenWire internally)
            assertNotNull(client.getClientId());
            assertEquals(PROTOCOL.STOMP, client.getProtocol());

            System.out.println("STOMP protocol client connected (via OpenWire): " + client.getClientId());
        } finally {
            client.close();
        }
    }

    @Test
    public void testPublishJsonData() throws Exception {
        String topicName = "test/json/data";

        GossClient client = new GossClient(PROTOCOL.OPENWIRE, null, OPENWIRE_URI, STOMP_URI);

        try {
            client.createSession();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> received = new AtomicReference<>();

            client.subscribe(topicName, response -> {
                received.set(response.toString());
                latch.countDown();
            });

            Thread.sleep(200);

            // Publish a serializable object (will be converted to JSON)
            TestData data = new TestData("test", 42);
            client.publish(topicName, data);

            boolean gotMessage = latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            assertTrue(gotMessage, "Should receive JSON data");
            assertNotNull(received.get(), "Received data should not be null");
            assertTrue(received.get().contains("test") || received.get().contains("42"),
                    "Received data should contain test values");

        } finally {
            client.close();
        }
    }

    @Test
    public void testMultipleTopics() throws Exception {
        String topic1 = "test/topic/one";
        String topic2 = "test/topic/two";

        GossClient client = new GossClient(PROTOCOL.OPENWIRE, null, OPENWIRE_URI, STOMP_URI);

        try {
            client.createSession();

            CountDownLatch latch = new CountDownLatch(2);
            AtomicReference<String> msg1 = new AtomicReference<>();
            AtomicReference<String> msg2 = new AtomicReference<>();

            client.subscribe(topic1, response -> {
                msg1.set(response.toString());
                latch.countDown();
            });

            client.subscribe(topic2, response -> {
                msg2.set(response.toString());
                latch.countDown();
            });

            Thread.sleep(200);

            client.publish(topic1, "Message for topic 1");
            client.publish(topic2, "Message for topic 2");

            boolean received = latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            assertTrue(received, "Should receive messages on both topics");
            // GossClient wraps messages in DataResponse JSON format
            assertTrue(msg1.get().contains("Message for topic 1"), "Topic 1 message: " + msg1.get());
            assertTrue(msg2.get().contains("Message for topic 2"), "Topic 2 message: " + msg2.get());

        } finally {
            client.close();
        }
    }

    @Test
    public void testClientReconnection() throws Exception {
        String topicName = "test/reconnect";

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
            // Each client should get a unique ID
            assertNotEquals(id1, id2, "Each client connection should have unique ID");

            // Verify second client works
            CountDownLatch latch = new CountDownLatch(1);
            client2.subscribe(topicName, response -> latch.countDown());
            Thread.sleep(100);
            client2.publish(topicName, "test");

            assertTrue(latch.await(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS));

        } finally {
            client2.close();
        }
    }

    // Test data class for JSON serialization
    private static class TestData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
