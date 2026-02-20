package pnnl.goss.core.client;

import jakarta.jms.BytesMessage;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.ClientListener;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Response;
import pnnl.goss.core.security.SecurityConstants;

public class DefaultClientListener implements ClientListener {
    private static Logger log = LoggerFactory.getLogger(DefaultClientListener.class);

    private GossResponseEvent responseEvent;

    public DefaultClientListener(GossResponseEvent event) {
        log.debug("Instantiating");
        responseEvent = event;
    }

    /**
     * Get the reply destination from a JMS message, falling back to string
     * properties when the STOMP adapter can't parse a bare reply-to header into a
     * JMS Destination (e.g., "feeder-models" instead of "/queue/feeder-models").
     */
    private Destination getReplyDestination(Message msg) {
        try {
            Destination dest = msg.getJMSReplyTo();
            if (dest != null) {
                // Accept any ActiveMQ destination type directly, including
                // ActiveMQTempQueue and ActiveMQTempTopic which do NOT extend
                // ActiveMQQueue/ActiveMQTopic (they extend ActiveMQTempDestination).
                if (dest instanceof ActiveMQDestination)
                    return dest;
                log.debug("Normalizing reply destination: {} -> ActiveMQQueue", dest);
                return new ActiveMQQueue(dest.toString());
            }
            // JMSReplyTo is null - check for bare reply-to string property
            // ActiveMQ STOMP adapter sets this when it can't map to a Destination
            String replyTo = msg.getStringProperty("reply-to");
            if (replyTo != null && !replyTo.isEmpty()) {
                log.debug("Using bare reply-to property as queue: {}", replyTo);
                return new ActiveMQQueue(replyTo);
            }
        } catch (Exception e) {
            log.warn("Failed to get reply destination", e);
        }
        return null;
    }

    public void onMessage(Message message) {
        log.info("DefaultClientListener.onMessage called with message type: {}",
                message != null ? message.getClass().getSimpleName() : "null");
        try {
            if (message instanceof ObjectMessage) {
                log.debug("message of type: " + message.getClass() + " received");
                ObjectMessage objectMessage = (ObjectMessage) message;
                if (objectMessage.getObject() instanceof pnnl.goss.core.Response) {
                    Response response = (Response) objectMessage.getObject();
                    responseEvent.onMessage(response);
                } else {
                    DataResponse response = new DataResponse(
                            objectMessage.getObject());
                    if (response.getDestination() == null)
                        response.setDestination(message.getJMSDestination().toString());
                    // Set reply destination and username from JMS headers
                    Destination replyDest = getReplyDestination(message);
                    if (replyDest != null)
                        response.setReplyDestination(replyDest);
                    if (message.getStringProperty(SecurityConstants.SUBJECT_HEADER) != null)
                        response.setUsername(message.getStringProperty(SecurityConstants.SUBJECT_HEADER));
                    responseEvent.onMessage(response);
                }
            } else if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                DataResponse response = new DataResponse(textMessage.getText());
                if (response.getDestination() == null)
                    response.setDestination(message.getJMSDestination().toString());
                // Set reply destination and username from JMS headers
                Destination replyDest = getReplyDestination(message);
                if (replyDest != null)
                    response.setReplyDestination(replyDest);
                if (message.getStringProperty(SecurityConstants.SUBJECT_HEADER) != null)
                    response.setUsername(message.getStringProperty(SecurityConstants.SUBJECT_HEADER));
                responseEvent.onMessage(response);
            } else if (message instanceof BytesMessage) {
                // BytesMessage is used by STOMP clients (Python, JavaScript, etc.)
                BytesMessage bytesMessage = (BytesMessage) message;
                // Read the bytes and convert to string
                long bodyLength = bytesMessage.getBodyLength();
                byte[] bytes = new byte[(int) bodyLength];
                bytesMessage.readBytes(bytes);
                String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                log.debug("BytesMessage received, body length: {}, content: {}", bodyLength, text);

                DataResponse response = new DataResponse(text);
                if (response.getDestination() == null)
                    response.setDestination(message.getJMSDestination().toString());
                // Set reply destination and username from JMS headers
                Destination replyDest = getReplyDestination(message);
                if (replyDest != null)
                    response.setReplyDestination(replyDest);
                if (message.getStringProperty(SecurityConstants.SUBJECT_HEADER) != null)
                    response.setUsername(message.getStringProperty(SecurityConstants.SUBJECT_HEADER));
                responseEvent.onMessage(response);
            } else {
                log.warn("Unhandled message type: {}", message.getClass().getName());
            }
            // Other possible types that could be implemented:
            // MapMessage - A set of keyword/value pairs.
            // StreamMessage - A list of Java primitive values.

        } catch (Exception e) {
            log.error("ERROR Receiving message", e);
            e.printStackTrace();
        }
    }
}
