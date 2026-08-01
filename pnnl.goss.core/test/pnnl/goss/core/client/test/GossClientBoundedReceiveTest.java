package pnnl.goss.core.client.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.MessageConsumer;
import jakarta.jms.ObjectMessage;
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
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.Response;
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
 *
 * <p>GADP-051 (session-isolation): getResponse() now derives a dedicated,
 * listener-free Session from the Connection for its synchronous request/reply
 * (so an async subscribe() listener on the shared session cannot poison the
 * synchronous receive()). The mock wiring therefore stubs the Connection to
 * return a mock Session, on which the temporary reply queue and its consumer
 * are created; the test additionally asserts that dedicated session is closed
 * after the call, matching the leak-safe teardown the fix guarantees.
 */
public class GossClientBoundedReceiveTest {

    private static final String DESTINATION = "goss.gridappsd.request.data.cimtopology";
    private static final long TIMEOUT_MILLIS = 3000L;

    private GossClient client;
    private Connection mockConnection;
    private Session mockSession;
    private MessageConsumer mockConsumer;
    private ClientPublishser mockPublisher;

    @BeforeEach
    void setUp() throws Exception {
        client = new GossClient(PROTOCOL.OPENWIRE, null, "tcp://localhost:61616",
                "stomp://localhost:61613");

        mockConnection = mock(Connection.class);
        mockSession = mock(Session.class);
        TemporaryQueue mockTempQueue = mock(TemporaryQueue.class);
        Queue mockQueue = mock(Queue.class);
        mockConsumer = mock(MessageConsumer.class);
        mockPublisher = mock(ClientPublishser.class);

        // getResponse() derives its dedicated sync session from the Connection.
        when(mockConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(mockSession);
        when(mockSession.createTemporaryQueue()).thenReturn(mockTempQueue);
        when(mockSession.createQueue(DESTINATION)).thenReturn(mockQueue);
        when(mockSession.createConsumer(mockTempQueue)).thenReturn(mockConsumer);

        // Both the shared session and the connection are pre-populated so
        // getSession() is a no-op (session already present) and the dedicated
        // sync session is derived from the injected connection.
        setPrivateField(client, "connection", mockConnection);
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
        // The dedicated request/reply session must be closed (leak-safe teardown).
        verify(mockSession, times(1)).close();
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
        // The dedicated request/reply session must be closed (leak-safe teardown).
        verify(mockSession, times(1)).close();
    }

    @Test
    @DisplayName("GADP-051 (message-type dispatch): a BytesMessage reply (STOMP-to-OpenWire bridged, e.g. the "
            + "topology background service) is decoded to its UTF-8 text body instead of throwing ClassCastException "
            + "on an unconditional TextMessage cast")
    void bytesMessageReplyIsDecodedToItsUtf8TextBody() throws Exception {
        String expectedBody = "{\"feeders\":[{\"mrid\":\"abc123\"}]}";
        byte[] expectedBytes = expectedBody.getBytes(StandardCharsets.UTF_8);

        BytesMessage mockBytesMessage = mock(BytesMessage.class);
        when(mockBytesMessage.getBodyLength()).thenReturn((long) expectedBytes.length);
        when(mockBytesMessage.readBytes(any(byte[].class))).thenAnswer(invocation -> {
            byte[] target = invocation.getArgument(0);
            System.arraycopy(expectedBytes, 0, target, 0, expectedBytes.length);
            return expectedBytes.length;
        });
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockBytesMessage);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS);

        assertThat(result).isEqualTo(expectedBody);
        verify(mockConsumer, times(1)).receive(TIMEOUT_MILLIS);
        verify(mockConsumer, times(1)).close();
        verify(mockSession, times(1)).close();
    }

    @Test
    @DisplayName("GADP-051 review remediation item 1: an ObjectMessage carrying a Response is unwrapped to that "
            + "exact Response instance")
    void objectMessageCarryingResponseIsUnwrappedToTheResponseBody() throws Exception {
        DataResponse expected = new DataResponse();
        expected.setDestination(DESTINATION);
        expected.setUsername("expected-user");

        ObjectMessage mockObjectMessage = mock(ObjectMessage.class);
        when(mockObjectMessage.getObject()).thenReturn(expected);
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockObjectMessage);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS);

        // Value-assert the decoded body is the exact Response instance the
        // ObjectMessage carried, not merely "some non-null Response".
        assertThat(result).isSameAs(expected);
        assertThat(((Response) result).getId()).isEqualTo(expected.getId());
        assertThat(((DataResponse) result).getUsername()).isEqualTo("expected-user");
        verify(mockConsumer, times(1)).receive(TIMEOUT_MILLIS);
        verify(mockConsumer, times(1)).close();
        verify(mockSession, times(1)).close();
    }

    @Test
    @DisplayName("GADP-051 review remediation item 1: an ObjectMessage carrying a plain (non-Response) "
            + "Serializable is passed through unchanged, per the current else-branch contract")
    void objectMessageCarryingPlainSerializableIsPassedThroughUnchanged() throws Exception {
        // A plain Serializable that is NOT a Response/DataResponse. The code
        // under test's else branch does `response = (Serializable)
        // objectMessage.getObject()` with no further transformation, so the
        // correct assertion is that the exact same String instance/value
        // comes back untouched.
        String expectedPayload = "plain-serializable-payload";

        ObjectMessage mockObjectMessage = mock(ObjectMessage.class);
        when(mockObjectMessage.getObject()).thenReturn(expectedPayload);
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockObjectMessage);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS);

        assertThat(result).isEqualTo(expectedPayload);
        assertThat(result).isNotInstanceOf(Response.class);
        verify(mockConsumer, times(1)).receive(TIMEOUT_MILLIS);
        verify(mockConsumer, times(1)).close();
        verify(mockSession, times(1)).close();
    }

    @Test
    @DisplayName("GADP-051 review remediation item 3: a BytesMessage whose reported body length exceeds the "
            + "sane cap throws SystemException instead of allocating an unbounded byte[]")
    void oversizedBytesMessageBodyLengthThrowsInsteadOfAllocating() throws Exception {
        BytesMessage mockBytesMessage = mock(BytesMessage.class);
        // One byte over the 8 MiB cap.
        when(mockBytesMessage.getBodyLength()).thenReturn((8L * 1024 * 1024) + 1);
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockBytesMessage);

        assertThatThrownBy(() -> client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS))
                .isInstanceOf(com.northconcepts.exception.SystemException.class)
                .hasMessageContaining("exceeds");
        // readBytes must never be called: the allocation itself is what this
        // guard exists to prevent, so it must be rejected before any attempt
        // to size or fill a buffer from the wire-supplied length.
        verify(mockBytesMessage, never()).readBytes(any(byte[].class));
        verify(mockConsumer, times(1)).close();
        verify(mockSession, times(1)).close();
    }

    @Test
    @DisplayName("GADP-051 review remediation item 3: a BytesMessage reporting a negative body length throws "
            + "SystemException instead of a lossy (int) cast feeding NegativeArraySizeException")
    void negativeBytesMessageBodyLengthThrowsInsteadOfAllocating() throws Exception {
        BytesMessage mockBytesMessage = mock(BytesMessage.class);
        when(mockBytesMessage.getBodyLength()).thenReturn(-1L);
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockBytesMessage);

        assertThatThrownBy(() -> client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS))
                .isInstanceOf(com.northconcepts.exception.SystemException.class)
                .hasMessageContaining("negative");
        verify(mockBytesMessage, never()).readBytes(any(byte[].class));
        verify(mockConsumer, times(1)).close();
        verify(mockSession, times(1)).close();
    }

    @Test
    @DisplayName("GADP-051 review remediation item 3 invariant: a valid normal-size BytesMessage still decodes "
            + "to the exact same UTF-8 string as before the length-cap guard was added")
    void normalSizeBytesMessageStillDecodesToExactUtf8String() throws Exception {
        String expectedBody = "{\"feeders\":[{\"mrid\":\"abc123\"}]}";
        byte[] expectedBytes = expectedBody.getBytes(StandardCharsets.UTF_8);

        BytesMessage mockBytesMessage = mock(BytesMessage.class);
        when(mockBytesMessage.getBodyLength()).thenReturn((long) expectedBytes.length);
        when(mockBytesMessage.readBytes(any(byte[].class))).thenAnswer(invocation -> {
            byte[] target = invocation.getArgument(0);
            System.arraycopy(expectedBytes, 0, target, 0, expectedBytes.length);
            return expectedBytes.length;
        });
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockBytesMessage);

        Serializable result = client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS);

        assertThat(result).isEqualTo(expectedBody);
    }

    @Test
    @DisplayName("GADP-051 (message-type dispatch): an unsupported JMS message type fails loudly with a "
            + "SystemException naming the concrete type, rather than silently returning null/empty (data-invariants "
            + "Rule 2: no default that could pass as a valid-but-wrong body downstream)")
    void unsupportedMessageTypeFailsLoudlyInsteadOfReturningNullOrEmpty() throws Exception {
        jakarta.jms.MapMessage mockMapMessage = mock(jakarta.jms.MapMessage.class);
        when(mockConsumer.receive(TIMEOUT_MILLIS)).thenReturn(mockMapMessage);

        assertThatThrownBy(() -> client.getResponse(DESTINATION + "-request", DESTINATION,
                RESPONSE_FORMAT.JSON, DESTINATION_TYPE.QUEUE, TIMEOUT_MILLIS))
                .isInstanceOf(com.northconcepts.exception.SystemException.class)
                .hasMessageContaining("Unsupported JMS message type")
                .hasMessageContaining("MapMessage");
        // The dedicated request/reply session and consumer must still be closed
        // (leak-safe teardown) even though the reply body was unusable.
        verify(mockConsumer, times(1)).close();
        verify(mockSession, times(1)).close();
    }

    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
