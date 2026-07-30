package pnnl.goss.core.client.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Field;

import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TextMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pnnl.goss.core.Client.DESTINATION_TYPE;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientPublishser;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.client.GossClient;

/**
 * Regression coverage for GADP-051: GossClient.getResponse()'s reply wait was
 * unbounded (MessageConsumer.receive() with no timeout), so a caller polling a
 * service that has not started answering yet (a boot-order race, e.g. the
 * topology-background-service at cold start) blocked forever on the first
 * attempt and could never retry. These tests cover the new bounded-timeout
 * overload's contract directly against a mocked JMS Session/MessageConsumer (no
 * live broker, same pattern as GossClientConsumerLeakTest): a timed-out receive
 * returns null and still closes the consumer rather than blocking or throwing,
 * and the pre-existing unbounded overloads keep calling receive(0), preserving
 * their exact prior "block indefinitely" behavior for other callers.
 */
public class GossClientBoundedReceiveTest {

    private static final String DESTINATION = "goss.gridappsd.request.data.cimtopology";
    private static final long TIMEOUT_MILLIS = 3000L;

    private GossClient client;
    private Session mockSession;
    private MessageConsumer mockConsumer;
    private ClientPublishser mockPublisher;

    @BeforeEach
    void setUp() throws Exception {
        client = new GossClient(PROTOCOL.OPENWIRE, null, "tcp://localhost:61616",
                "stomp://localhost:61613");

        mockSession = mock(Session.class);
        TemporaryQueue mockTempQueue = mock(TemporaryQueue.class);
        Queue mockQueue = mock(Queue.class);
        mockConsumer = mock(MessageConsumer.class);
        mockPublisher = mock(ClientPublishser.class);

        when(mockSession.createTemporaryQueue()).thenReturn(mockTempQueue);
        when(mockSession.createQueue(DESTINATION)).thenReturn(mockQueue);
        when(mockSession.createConsumer(mockTempQueue)).thenReturn(mockConsumer);

        setPrivateField(client, "session", mockSession);
        setPrivateField(client, "clientPublisher", mockPublisher);
    }

    @Test
    @DisplayName("a timed-out receive returns null instead of blocking or throwing, and still closes the consumer")
    void timedOutReceiveReturnsNullAndClosesConsumer() throws Exception {
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(null);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS);

        assertThat(result).isNull();
        verify(mockConsumer, times(1)).receive(TIMEOUT_MILLIS);
        verify(mockConsumer, times(1)).close();
    }

    @Test
    @DisplayName("the caller-supplied timeout is honored: receive(timeoutMillis) is called, not the no-arg receive()")
    void receiveIsCalledWithTheGivenTimeout() throws Exception {
        TextMessage mockMessage = mock(TextMessage.class);
        when(mockMessage.getText()).thenReturn("{\"ok\":true}");
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockMessage);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS);

        assertThat(result).isEqualTo("{\"ok\":true}");
        verify(mockConsumer, times(1)).receive(TIMEOUT_MILLIS);
        verify(mockConsumer, never()).receive();
    }

    @Test
    @DisplayName("the pre-existing 4-arg (destinationType) overload still calls receive(0): block-indefinitely contract unchanged")
    void unboundedDestinationTypeOverloadStillBlocksIndefinitely() throws Exception {
        TextMessage mockMessage = mock(TextMessage.class);
        when(mockMessage.getText()).thenReturn("{\"ok\":true}");
        when(mockConsumer.receive(0L)).thenReturn(mockMessage);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE);

        assertThat(result).isEqualTo("{\"ok\":true}");
        verify(mockConsumer, times(1)).receive(0L);
    }

    @Test
    @DisplayName("the pre-existing 3-arg (default QUEUE) overload still calls receive(0): block-indefinitely contract unchanged")
    void defaultQueueOverloadStillBlocksIndefinitely() throws Exception {
        TextMessage mockMessage = mock(TextMessage.class);
        when(mockMessage.getText()).thenReturn("{\"ok\":true}");
        when(mockConsumer.receive(0L)).thenReturn(mockMessage);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON);

        assertThat(result).isEqualTo("{\"ok\":true}");
        verify(mockConsumer, times(1)).receive(0L);
    }

    @Test
    @DisplayName("the new 4-arg (default QUEUE, timeoutMillis) overload delegates the given timeout through to receive()")
    void defaultQueueTimeoutOverloadHonorsTheGivenTimeout() throws Exception {
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(null);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, TIMEOUT_MILLIS);

        assertThat(result).isNull();
        verify(mockConsumer, times(1)).receive(TIMEOUT_MILLIS);
        verify(mockConsumer, times(1)).close();
    }

    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
