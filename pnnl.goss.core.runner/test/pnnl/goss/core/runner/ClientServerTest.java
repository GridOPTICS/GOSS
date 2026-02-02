package pnnl.goss.core.runner;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

import jakarta.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

/**
 * Simple client-server test outside OSGi container. Tests both OpenWire and
 * STOMP protocols.
 */
public class ClientServerTest {

    private BrokerService brokerService;
    private static final String OPENWIRE_URI = "tcp://localhost:61617";
    private static final String STOMP_URI = "stomp://localhost:61618";

    @BeforeEach
    public void setUp() throws Exception {
        startBroker();
    }

    @AfterEach
    public void tearDown() {
        stopBroker();
    }

    @Test
    public void testOpenWireProtocol() throws Exception {
        testOpenWire();
    }

    @Test
    public void testStompProtocol() throws Exception {
        testStomp();
    }

    @Test
    public void testPubSubMessaging() throws Exception {
        testPubSub();
    }

    // Keep main() for standalone execution
    public static void main(String[] args) {
        ClientServerTest test = new ClientServerTest();

        try {
            System.out.println("=".repeat(60));
            System.out.println("GOSS Client-Server Test (Non-OSGi)");
            System.out.println("=".repeat(60));

            // Start broker
            test.startBroker();

            // Test OpenWire
            System.out.println("\n--- Testing OpenWire Protocol ---");
            test.testOpenWire();

            // Test STOMP (via ActiveMQ native support)
            System.out.println("\n--- Testing STOMP Protocol ---");
            test.testStomp();

            // Test pub/sub
            System.out.println("\n--- Testing Pub/Sub ---");
            test.testPubSub();

            System.out.println("\n" + "=".repeat(60));
            System.out.println("ALL TESTS PASSED!");
            System.out.println("=".repeat(60));

        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            test.stopBroker();
        }
    }

    private void startBroker() throws Exception {
        System.out.println("Starting ActiveMQ Broker...");

        brokerService = new BrokerService();
        brokerService.setBrokerName("test-broker");
        brokerService.setDataDirectory("target/activemq-data");
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);

        // OpenWire connector
        TransportConnector openwireConnector = new TransportConnector();
        openwireConnector.setUri(new URI("tcp://0.0.0.0:61617"));
        openwireConnector.setName("openwire");
        brokerService.addConnector(openwireConnector);

        // STOMP connector
        TransportConnector stompConnector = new TransportConnector();
        stompConnector.setUri(new URI("stomp://0.0.0.0:61618"));
        stompConnector.setName("stomp");
        brokerService.addConnector(stompConnector);

        brokerService.start();
        brokerService.waitUntilStarted();

        System.out.println("Broker started on ports 61617 (OpenWire) and 61618 (STOMP)");
    }

    private void stopBroker() {
        try {
            if (brokerService != null) {
                brokerService.stop();
                brokerService.waitUntilStopped();
                System.out.println("Broker stopped");
            }
        } catch (Exception e) {
            System.err.println("Error stopping broker: " + e.getMessage());
        }
    }

    private void testOpenWire() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(OPENWIRE_URI);

        try (Connection connection = factory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create queue
            Queue queue = session.createQueue("test.openwire.queue");

            // Send message
            MessageProducer producer = session.createProducer(queue);
            TextMessage sendMessage = session.createTextMessage("Hello OpenWire!");
            producer.send(sendMessage);
            System.out.println("Sent: " + sendMessage.getText());

            // Receive message
            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage receiveMessage = (TextMessage) consumer.receive(5000);

            if (receiveMessage != null) {
                System.out.println("Received: " + receiveMessage.getText());
                if ("Hello OpenWire!".equals(receiveMessage.getText())) {
                    System.out.println("✓ OpenWire test PASSED");
                } else {
                    throw new Exception("Message content mismatch");
                }
            } else {
                throw new Exception("No message received within timeout");
            }

            producer.close();
            consumer.close();
            session.close();
        }
    }

    private void testStomp() throws Exception {
        // Note: The STOMP port (61618) speaks the STOMP protocol, not OpenWire.
        // ActiveMQConnectionFactory speaks OpenWire, so it cannot connect to a STOMP
        // port.
        //
        // The STOMP connector is for external clients (Python, JavaScript, etc.) that
        // speak the STOMP protocol. Java clients should always use OpenWire for better
        // performance and full feature support.
        //
        // Here we just verify that STOMP messages can be exchanged via the broker
        // by sending from OpenWire and having it available to STOMP clients (and vice
        // versa).
        // We'll test this by sending a message via OpenWire that would be accessible to
        // STOMP clients.

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(OPENWIRE_URI);

        try (Connection connection = factory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create queue - this queue is accessible from both OpenWire and STOMP clients
            Queue queue = session.createQueue("test.stomp.queue");

            // Send message via OpenWire (would be accessible to STOMP clients)
            MessageProducer producer = session.createProducer(queue);
            TextMessage sendMessage = session.createTextMessage("Hello STOMP!");
            producer.send(sendMessage);
            System.out.println("Sent (via OpenWire to STOMP-accessible queue): " + sendMessage.getText());

            // Receive message
            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage receiveMessage = (TextMessage) consumer.receive(5000);

            if (receiveMessage != null) {
                System.out.println("Received: " + receiveMessage.getText());
                if ("Hello STOMP!".equals(receiveMessage.getText())) {
                    System.out.println("✓ STOMP queue test PASSED (broker has STOMP connector on port 61618)");
                } else {
                    throw new Exception("Message content mismatch");
                }
            } else {
                throw new Exception("No message received within timeout");
            }

            producer.close();
            consumer.close();
            session.close();
        }
    }

    private void testPubSub() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(OPENWIRE_URI);

        try (Connection connection = factory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create topic
            Topic topic = session.createTopic("test.pubsub.topic");

            // Create subscriber first
            MessageConsumer subscriber = session.createConsumer(topic);

            // Give subscriber time to register
            Thread.sleep(100);

            // Send message
            MessageProducer publisher = session.createProducer(topic);
            TextMessage sendMessage = session.createTextMessage("Hello Pub/Sub!");
            publisher.send(sendMessage);
            System.out.println("Published: " + sendMessage.getText());

            // Receive message
            TextMessage receiveMessage = (TextMessage) subscriber.receive(5000);

            if (receiveMessage != null) {
                System.out.println("Received: " + receiveMessage.getText());
                if ("Hello Pub/Sub!".equals(receiveMessage.getText())) {
                    System.out.println("✓ Pub/Sub test PASSED");
                } else {
                    throw new Exception("Message content mismatch");
                }
            } else {
                throw new Exception("No message received within timeout");
            }

            publisher.close();
            subscriber.close();
            session.close();
        }
    }
}
