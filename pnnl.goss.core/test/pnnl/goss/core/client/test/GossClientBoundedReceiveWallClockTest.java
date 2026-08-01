package pnnl.goss.core.client.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.Client.DESTINATION_TYPE;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.client.GossClient;

/**
 * GOSS-023 regression coverage: the bounded getResponse(...,timeoutMillis)
 * overload must actually BLOCK the calling thread for (approximately) its
 * timeout budget when no reply arrives, not return early. The existing
 * GossClientBoundedReceiveTest is mock-only: it proves receive(timeoutMillis)
 * is the method INVOKED, but a Mockito stub returning null returns immediately
 * regardless of the argument, so it cannot catch a regression where the real
 * JMS call path returns early (the exact defect Hale's GADP-051 runtime-verify
 * caught: the retry window collapsed from a designed ~19s to ~4s because each
 * bounded receive() returned in ~5ms instead of blocking ~3000ms).
 *
 * This test uses a real embedded ActiveMQ broker (same pattern as
 * pnnl.goss.core.runner's ClientServerTest) and a destination with no
 * responder, so the only way getResponse(...,timeoutMillis) can return before
 * the deadline is a genuine defect in the blocking path.
 */
public class GossClientBoundedReceiveWallClockTest {

    private static final String DESTINATION = "goss.gridappsd.test.no.responder";
    private static final long TIMEOUT_MILLIS = 1500L;
    // Real scheduling jitter (GC, thread wakeup) is normally low tens of ms;
    // allow a generous margin without weakening the assertion enough to miss
    // the ~5ms-fast-return defect class this test targets.
    private static final long TOLERANCE_MILLIS = 400L;

    private BrokerService broker;
    private String brokerUri;
    private GossClient client;

    @BeforeEach
    void startBrokerAndClient() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName("goss-023-test-broker");
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
    void boundedGetResponseBlocksForItsFullTimeoutBudgetWhenNoReplyArrives() throws Exception {
        long t0 = System.currentTimeMillis();
        Object result = client.getResponse("no-op-request", DESTINATION,
                pnnl.goss.core.Request.RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS);
        long elapsed = System.currentTimeMillis() - t0;

        assertThat(result).isNull();
        assertThat(elapsed)
                .withFailMessage(
                        "getResponse(...,%dms) returned after only %dms; it must block for approximately "
                                + "its full timeout budget when no reply arrives (GOSS-023: this collapsed the "
                                + "FieldBusManager retry window from ~19s to ~4s in GADP-051 runtime-verify)",
                        TIMEOUT_MILLIS, elapsed)
                .isGreaterThanOrEqualTo(TIMEOUT_MILLIS - TOLERANCE_MILLIS);
    }

    /**
     * GOSS-023 root-cause coverage: the actual mechanism behind Hale's GADP-051
     * finding. The single-attempt "no responder" scenario above proves the ordinary
     * receive(timeout) blocking path is healthy; it does NOT reproduce a fast
     * return. What DOES reproduce a fast return is a genuine JMSException during
     * receive() (e.g. the broker/transport going away mid-call): before the fix,
     * GossClient's catch (JMSException e) block constructed a SystemException and
     * never threw it, silently falling through to "return null" after only the few
     * ms it took the provider to report the failure, indistinguishable from a real
     * multi-second timeout. That is exactly what collapsed FieldBusManager's
     * designed ~19s retry budget to ~4s: every attempt "timed out" almost
     * instantly.
     *
     * A hard transport failure must be surfaced, not swallowed into null: this test
     * kills the broker shortly after the receive() call starts, and asserts
     * getResponse propagates a SystemException (wrapping the real JMSException)
     * rather than returning null.
     */
    @Test
    void boundedGetResponseSurfacesAGenuineTransportFailureInsteadOfReturningNull() throws Exception {
        Thread killer = new Thread(() -> {
            try {
                Thread.sleep(300);
                broker.stop();
                broker.waitUntilStopped();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        killer.start();

        try {
            assertThatThrownBy(() -> client.getResponse("no-op-request", DESTINATION,
                    pnnl.goss.core.Request.RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS))
                    .withFailMessage(
                            "getResponse(...) must surface a genuine transport failure (a real "
                                    + "JMSException from a dead broker/consumer) rather than silently "
                                    + "returning null, which is indistinguishable from a real timeout "
                                    + "and defeats bounded-retry callers like FieldBusManager (GOSS-023)")
                    .isInstanceOf(SystemException.class)
                    .hasCauseInstanceOf(jakarta.jms.JMSException.class);
        } finally {
            killer.join();
        }
    }
}
