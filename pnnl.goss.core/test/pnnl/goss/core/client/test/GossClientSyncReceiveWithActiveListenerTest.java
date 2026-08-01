package pnnl.goss.core.client.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pnnl.goss.core.Client.DESTINATION_TYPE;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.client.GossClient;

/**
 * GADP-051 (session-isolation) regression coverage.
 *
 * <p>
 * Root cause verified at runtime cold start: FieldBusManager registers an async
 * device-output {@code MessageListener} via {@code subscribe(...)} and then
 * issues a synchronous topology {@code getResponse(...)} on the SAME
 * {@link pnnl.goss.core.client.GossClient}. Both operations shared the client's
 * single JMS {@code Session}, and {@code jakarta.jms} forbids a synchronous
 * {@code receive()} on a session that has a {@code MessageListener} attached:
 * {@code org.apache.activemq.ActiveMQSession.checkMessageListener} throws
 * {@code jakarta.jms.IllegalStateException} with the message "Cannot
 * synchronously receive a message when a MessageListener is set". That made
 * every topology attempt throw instantly regardless of timing, so the caller's
 * bounded-retry window (GADP-051 / GOSS-023) could never succeed.
 *
 * <p>
 * This test reproduces the exact interaction against a real embedded broker (a
 * mock cannot exercise {@code ActiveMQSession.checkMessageListener}): it
 * subscribes an async listener, then calls the bounded {@code getResponse}
 * overload against a destination with no responder.
 *
 * <ul>
 * <li>RED (pre-fix, shared session): the synchronous receive throws
 * {@code IllegalStateException} within a few milliseconds; it never reaches its
 * timeout budget.</li>
 * <li>GREEN (post-fix, dedicated listener-free session): the synchronous
 * receive runs cleanly on its own session, blocks for its timeout budget, and
 * returns {@code null} (no reply arrived) without throwing.</li>
 * </ul>
 *
 * <p>
 * Same embedded-broker pattern as
 * {@link GossClientBoundedReceiveWallClockTest}.
 */
public class GossClientSyncReceiveWithActiveListenerTest {

    private static final String LISTENER_TOPIC = "goss.gridappsd.test.device.output";
    private static final String SYNC_DESTINATION = "goss.gridappsd.test.topology.no.responder";
    private static final long TIMEOUT_MILLIS = 1200L;
    // Scheduling jitter margin, mirrored from
    // GossClientBoundedReceiveWallClockTest.
    private static final long TOLERANCE_MILLIS = 400L;

    private BrokerService broker;
    private String brokerUri;
    private GossClient client;

    @BeforeEach
    void startBrokerAndClient() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName("gadp-051-session-isolation-test-broker");
        broker.setPersistent(false);
        broker.setUseJmx(false);
        TransportConnector connector = new TransportConnector();
        connector.setUri(new URI("tcp://0.0.0.0:0"));
        broker.addConnector(connector);
        broker.start();
        broker.waitUntilStarted();
        brokerUri = broker.getTransportConnectors().get(0).getPublishableConnectString();

        client = new GossClient(PROTOCOL.OPENWIRE, null, brokerUri, "stomp://localhost:0");
        client.createSession();
    }

    @AfterEach
    void stopBrokerAndClient() throws Exception {
        if (client != null) {
            client.close();
        }
        if (broker != null) {
            broker.stop();
            broker.waitUntilStopped();
        }
    }

    @Test
    void syncGetResponseSucceedsWhileAnAsyncListenerIsActiveOnTheClient() throws Exception {
        // Attach an async MessageListener on the client, exactly as
        // FieldBusManager.publishDeviceOutput() does before it issues its
        // synchronous topology request. Pre-fix this poisons the shared session
        // for any later synchronous receive() on the same client.
        client.subscribe(LISTENER_TOPIC, response -> {
            // no-op: presence of the listener is what matters, not its behavior
        });

        long t0 = System.currentTimeMillis();

        // Pre-fix this throws jakarta.jms.IllegalStateException
        // ("Cannot synchronously receive a message when a MessageListener is set")
        // within a few ms. Post-fix it runs on a dedicated listener-free session,
        // blocks for its timeout budget, and returns null (no responder).
        final Object[] result = new Object[1];
        assertThatCode(() -> result[0] = client.getResponse("topology-request", SYNC_DESTINATION,
                pnnl.goss.core.Request.RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS))
                .withFailMessage(
                        "getResponse must complete a synchronous receive even while an async "
                                + "MessageListener is active on the same client; pre-fix the shared "
                                + "session threw IllegalStateException (\"Cannot synchronously receive "
                                + "a message when a MessageListener is set\"), which made every "
                                + "FieldBusManager topology attempt fail instantly (GADP-051)")
                .doesNotThrowAnyException();

        long elapsed = System.currentTimeMillis() - t0;

        // No responder on SYNC_DESTINATION, so a healthy synchronous receive
        // returns null only after blocking for its timeout budget. This is the
        // GREEN signal: the receive actually ran (rather than throwing instantly).
        assertThat(result[0]).isNull();
        assertThat(elapsed)
                .withFailMessage(
                        "getResponse returned/threw after only %dms; with an active async listener "
                                + "and a dedicated sync session it must block for approximately its "
                                + "%dms timeout budget, not fail instantly (GADP-051)",
                        elapsed, TIMEOUT_MILLIS)
                .isGreaterThanOrEqualTo(TIMEOUT_MILLIS - TOLERANCE_MILLIS);
    }
}
