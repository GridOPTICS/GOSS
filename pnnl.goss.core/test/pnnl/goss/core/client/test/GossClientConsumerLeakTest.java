package pnnl.goss.core.client.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientConsumer;
import pnnl.goss.core.client.GossClient;

/**
 * Regression coverage for GADP-018: GossClient.subscribe() created a
 * DefaultClientConsumer and dropped the reference immediately after the method
 * returned, so the underlying JMS MessageConsumer (and its listener thread) was
 * never closed and stayed registered on the broker for the lifetime of the
 * connection.
 *
 * GossClient has no broker-backed constructor path suitable for a unit test
 * (createSession() dials a real ActiveMQConnectionFactory), so these tests
 * inject a mocked Session via reflection to exercise subscribe() and close()
 * without a live broker. This covers the unit-level contract (consumer tracked,
 * closed, list cleared); it does not cover the broker-observable consequence
 * (the consumer count on the ActiveMQ destination actually drops to zero),
 * which needs a live-broker integration test.
 */
public class GossClientConsumerLeakTest {

    private GossClient client;
    private Session mockSession;
    private MessageConsumer mockConsumer;

    @BeforeEach
    void setUp() throws Exception {
        client = new GossClient(PROTOCOL.OPENWIRE, null, "tcp://localhost:61616",
                "stomp://localhost:61613");

        mockSession = mock(Session.class);
        Topic mockTopic = mock(Topic.class);
        mockConsumer = mock(MessageConsumer.class);
        when(mockSession.createTopic("test.topic")).thenReturn(mockTopic);
        when(mockSession.createConsumer(mockTopic)).thenReturn(mockConsumer);

        setPrivateField(client, "session", mockSession);
    }

    @Test
    @DisplayName("subscribe() tracks the created consumer instead of dropping the reference")
    void subscribeTracksConsumerForLifecycleManagement() throws Exception {
        client.subscribe("test.topic", response -> {
        });

        List<?> tracked = getPrivateField(client, "subscriptionConsumers", List.class);
        assertThat(tracked).hasSize(1);
        assertThat(tracked.get(0)).isInstanceOf(ClientConsumer.class);
    }

    @Test
    @DisplayName("close() closes every MessageConsumer created by subscribe(), deregistering it from the broker")
    void closeClosesTrackedConsumers() throws Exception {
        client.subscribe("test.topic", response -> {
        });

        client.close();

        verify(mockConsumer, times(1)).close();
    }

    @Test
    @DisplayName("close() clears the tracked-consumer list so a second close() is a no-op, not a double-close")
    void closeClearsTrackedConsumers() throws Exception {
        client.subscribe("test.topic", response -> {
        });
        client.close();

        List<?> tracked = getPrivateField(client, "subscriptionConsumers", List.class);
        assertThat(tracked).isEmpty();
    }

    @Test
    @DisplayName("close() tolerates a consumer whose close() throws: remaining consumers still close, "
            + "the tracked list still clears, and session teardown still runs")
    void closeSurvivesAThrowingConsumerAndStillTearsDownSession() throws Exception {
        // Regression guard for the HIGH finding: a consumer left in a bad state
        // (e.g. a null messageConsumer from a failed subscribe()) must not abort
        // the close() loop and strand the remaining consumers, the tracked list,
        // or the session/connection teardown that follows.
        ClientConsumer throwingConsumer = mock(ClientConsumer.class);
        doThrow(new RuntimeException("boom")).when(throwingConsumer).close();
        ClientConsumer healthyConsumer = mock(ClientConsumer.class);

        List<ClientConsumer> tracked = new ArrayList<>();
        tracked.add(throwingConsumer);
        tracked.add(healthyConsumer);
        setPrivateField(client, "subscriptionConsumers", tracked);

        client.close();

        verify(throwingConsumer, times(1)).close();
        verify(healthyConsumer, times(1)).close();

        List<?> trackedAfter = getPrivateField(client, "subscriptionConsumers", List.class);
        assertThat(trackedAfter).isEmpty();

        verify(mockSession, times(1)).close();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
