package pnnl.goss.core;

import java.io.Serializable;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;

import pnnl.goss.core.Request.RESPONSE_FORMAT;

import com.northconcepts.exception.SystemException;

//import org.apache.activemq.ConfigurationException;

public interface Client {

    public enum PROTOCOL {
        OPENWIRE, STOMP, SSL
    };

    /**
     * Destination type for JMS messaging. TOPIC: Publish/subscribe semantics -
     * message delivered to all subscribers QUEUE: Point-to-point semantics -
     * message delivered to one consumer
     */
    public enum DESTINATION_TYPE {
        TOPIC, QUEUE
    };

    /**
     * Makes synchronous call to the server using QUEUE destination (default). This
     * matches Python client behavior where bare destination names are treated as
     * queues.
     *
     * @param request
     * @param destination
     * @param responseFormat
     * @return
     * @throws SystemException
     */
    public Serializable getResponse(Serializable request, String destination,
            RESPONSE_FORMAT responseFormat) throws SystemException, JMSException;

    /**
     * Makes synchronous call to the server with specified destination type.
     *
     * @param request
     * @param destination
     *            destination name
     * @param responseFormat
     * @param destinationType
     *            TOPIC or QUEUE
     * @return
     * @throws SystemException
     */
    public Serializable getResponse(Serializable request, String destination,
            RESPONSE_FORMAT responseFormat, DESTINATION_TYPE destinationType) throws SystemException, JMSException;

    /**
     * Makes synchronous call to the server using QUEUE destination (default),
     * bounded by an explicit receive timeout. Unlike the no-timeout overloads
     * (which block indefinitely waiting for a reply), this returns {@code null}
     * once {@code timeoutMillis} elapses without a reply, rather than blocking the
     * calling thread forever. Intended for callers that need to retry against a
     * service that may not be answerable yet (a boot-order race), where an
     * unbounded wait would defeat the retry loop entirely.
     *
     * @param request
     * @param destination
     * @param responseFormat
     * @param timeoutMillis
     *            maximum time to wait for a reply, in milliseconds. A value of
     *            {@code 0} blocks indefinitely (matches
     *            {@link jakarta.jms.MessageConsumer#receive(long)} semantics).
     * @return the response, or {@code null} if no reply arrived within
     *         {@code timeoutMillis}
     * @throws SystemException
     */
    public Serializable getResponse(Serializable request, String destination,
            RESPONSE_FORMAT responseFormat, long timeoutMillis) throws SystemException, JMSException;

    /**
     * Makes synchronous call to the server with specified destination type, bounded
     * by an explicit receive timeout. See
     * {@link #getResponse(Serializable, String, RESPONSE_FORMAT, long)} for the
     * timeout semantics.
     *
     * @param request
     * @param destination
     *            destination name
     * @param responseFormat
     * @param destinationType
     *            TOPIC or QUEUE
     * @param timeoutMillis
     *            maximum time to wait for a reply, in milliseconds. A value of
     *            {@code 0} blocks indefinitely (matches
     *            {@link jakarta.jms.MessageConsumer#receive(long)} semantics).
     * @return the response, or {@code null} if no reply arrived within
     *         {@code timeoutMillis}
     * @throws SystemException
     */
    public Serializable getResponse(Serializable request, String destination,
            RESPONSE_FORMAT responseFormat, DESTINATION_TYPE destinationType, long timeoutMillis)
            throws SystemException, JMSException;

    /**
     * Lets the client subscribe to a Topic of the given name for event based
     * communication.
     *
     * @param topicName
     *            throws IllegalStateException if GossCLient is not initialized with
     *            an GossResponseEvent. Cannot asynchronously receive a message when
     *            a MessageListener is not set. throws JMSException
     */
    public Client subscribe(String topic, GossResponseEvent event)
            throws SystemException;

    /**
     * Lets the client subscribe to a destination with specified type for event
     * based communication.
     *
     * @param destinationName
     *            the destination name
     * @param event
     *            the event handler
     * @param destinationType
     *            TOPIC or QUEUE
     * @return this client for chaining
     * @throws SystemException
     */
    public Client subscribe(String destinationName, GossResponseEvent event, DESTINATION_TYPE destinationType)
            throws SystemException;

    public void publish(String topicName, Serializable message)
            throws SystemException;

    /**
     * Publish a message to a destination with specified type.
     *
     * @param destinationName
     *            the destination name
     * @param message
     *            the message to publish
     * @param destinationType
     *            TOPIC or QUEUE
     * @throws SystemException
     */
    public void publish(String destinationName, Serializable message, DESTINATION_TYPE destinationType)
            throws SystemException;

    public void publish(Destination destination, Serializable data)
            throws SystemException;

    /**
     * Close a connection with the server.
     */
    public void close();

    /**
     * Gets the type of protocol that the client will use to connect with.
     *
     * @return
     */
    public PROTOCOL getProtocol();

}
