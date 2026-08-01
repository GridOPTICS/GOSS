/*
    Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE

    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
 */
package pnnl.goss.core.client;

//import static pnnl.goss.core.GossCoreContants.PROP_CORE_CLIENT_CONFIG;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.shiro.authc.AuthenticationTokenFactory;
import org.apache.activemq.shiro.subject.SubjectConnectionReference;
import org.apache.http.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Client;
import pnnl.goss.core.ClientConsumer;
import pnnl.goss.core.ClientPublishser;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.security.GossSecurityManager;
import pnnl.goss.core.security.JWTAuthenticationToken;
import pnnl.goss.core.security.SecurityConstants;
import pnnl.goss.core.security.impl.SecurityManagerImpl;
import pnnl.goss.core.Response;
import pnnl.goss.core.ResponseError;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.northconcepts.exception.ConnectionCode;
import com.northconcepts.exception.SystemException;

public class GossClient implements Client {

    private static final Logger log = LoggerFactory.getLogger(GossClient.class);

    // jakarta.jms.MessageConsumer#receive(long) treats a timeout of 0 as "block
    // indefinitely", the same contract as the no-arg receive() it replaces. Used
    // by the unbounded getResponse() overloads below so their behavior is
    // unchanged after the bounded-timeout overload was added (GADP-051).
    private static final long UNBOUNDED_RECEIVE_TIMEOUT_MS = 0L;

    // Sane upper bound on a BytesMessage reply body decoded by getResponse().
    // A topology/response JSON reply is at most a few MB; a length beyond this
    // is either a malformed/adversarial wire value or provider corruption, and
    // allocating a byte[] directly from an unbounded wire-supplied length is an
    // OOM denial-of-service vector (and the (int) cast on getBodyLength() is
    // lossy/negative past Integer.MAX_VALUE). Reject rather than allocate.
    private static final long MAX_BYTES_MESSAGE_BODY_LENGTH = 8L * 1024 * 1024;

    private UUID uuid = null;
    private String brokerUri = null;
    private String stompUri = null;
    private ClientConfiguration config;
    private volatile ClientPublishser clientPublisher;
    private Connection connection = null;
    private Session session = null;
    private boolean used;
    private String trustStore;
    private String trustStorePassword;
    private List<Thread> threads = new ArrayList<Thread>();
    // Consumers created by subscribe() are long-lived (unlike the getResponse()
    // consumer, which is closed in its own try/finally). Track them here so
    // close() can deregister every subscription's MessageConsumer from the
    // broker instead of leaking it when the reference goes out of scope.
    private final List<ClientConsumer> subscriptionConsumers = new ArrayList<ClientConsumer>();
    private PROTOCOL protocol;
    private Credentials credentials = null;
    private String token = null;
    private boolean useToken = false;

    public GossClient(PROTOCOL protocol, Credentials credentials,
            String openwireUri, String stompUri, String trustStorePassword,
            String trustStore, boolean useToken) {
        this.uuid = UUID.randomUUID();
        this.protocol = protocol;
        this.credentials = credentials;
        this.brokerUri = openwireUri;
        this.stompUri = stompUri;
        this.trustStorePassword = trustStorePassword;
        this.trustStore = trustStore;
        this.useToken = useToken;
    }

    public GossClient(PROTOCOL protocol, Credentials credentials,
            String openwireUri, String stompUri, String trustStorePassword,
            String trustStore) {
        this.uuid = UUID.randomUUID();
        this.protocol = protocol;
        this.credentials = credentials;
        this.brokerUri = openwireUri;
        this.stompUri = stompUri;
        this.trustStorePassword = trustStorePassword;
        this.trustStore = trustStore;
    }

    public GossClient(PROTOCOL protocol, Credentials credentials,
            String openwireUri, String stompUri, boolean useToken) {
        this.uuid = UUID.randomUUID();
        this.protocol = protocol;
        this.credentials = credentials;
        this.brokerUri = openwireUri;
        this.stompUri = stompUri;
        this.useToken = useToken;
    }

    public GossClient(PROTOCOL protocol, Credentials credentials,
            String openwireUri, String stompUri) {
        this.uuid = UUID.randomUUID();
        this.protocol = protocol;
        this.credentials = credentials;
        this.brokerUri = openwireUri;
        this.stompUri = stompUri;
    }

    private void createSslSession() throws Exception {
        ActiveMQSslConnectionFactory cf = new ActiveMQSslConnectionFactory(
                brokerUri);

        cf.setTrustStore(trustStore);
        cf.setTrustStorePassword(trustStorePassword);

        if (credentials != null) {
            cf.setUserName(credentials.getUserPrincipal().getName());
            cf.setPassword(credentials.getPassword());
        }

        connection = (ActiveMQConnection) cf.createConnection();
        if (connection == null) {
            throw new SystemException(ConnectionCode.CONNECTION_ERROR);
        }

        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (session == null) {
            throw new SystemException(ConnectionCode.SESSION_ERROR);
        }

        if (credentials != null) {
            clientPublisher = new DefaultClientPublisher(credentials
                    .getUserPrincipal().getName(), session);
        } else {
            clientPublisher = new DefaultClientPublisher(session);
        }
    }

    public void createSession() throws Exception {

        config = new ClientConfiguration().set("TCP_BROKER", brokerUri);

        if (credentials != null) {
            config.set("CREDENTIALS", credentials);
        }

        if (protocol.equals(PROTOCOL.SSL)) {
            createSslSession();
        }

        else if (protocol.equals(PROTOCOL.OPENWIRE)) {
            if (credentials != null) {
                log.debug("Creating OPENWIRE client session for "
                        + credentials.getUserPrincipal());
            } else {
                log.debug("Creating OPENWIRE client session without credentials");
            }

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
                    brokerUri);

            if (credentials != null) {
                factory.setUserName(credentials.getUserPrincipal().getName());
                factory.setPassword(credentials.getPassword());
            }

            connection = factory.createConnection();
        } else if (protocol.equals(PROTOCOL.STOMP)) {
            // Note: The STOMP protocol in ActiveMQ is for external clients (Python,
            // JavaScript, etc.)
            // that speak the STOMP protocol. Java clients should use OpenWire for better
            // performance and full JMS feature support.
            //
            // When STOMP protocol is selected, we use the OpenWire URI instead because:
            // 1. ActiveMQConnectionFactory speaks OpenWire, not STOMP
            // 2. The broker routes messages between protocols internally
            // 3. Messages sent via OpenWire are accessible to STOMP clients and vice versa
            //
            // If you need true STOMP protocol support for Java, use a dedicated STOMP
            // library.

            log.warn("STOMP protocol selected - using OpenWire connection to broker. " +
                    "STOMP is intended for external clients (Python, JS). " +
                    "Java clients should use OPENWIRE for best performance.");

            if (credentials != null) {
                log.debug("Creating session for " + credentials.getUserPrincipal() +
                        " (STOMP requested, using OpenWire)");
            } else {
                log.debug("Creating session without credentials (STOMP requested, using OpenWire)");
            }

            // Use the OpenWire broker URI instead of the STOMP URI
            // This allows Java clients to still communicate with the broker
            // while STOMP clients can connect via the STOMP port
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUri);

            if (credentials != null) {
                factory.setUserName(credentials.getUserPrincipal().getName());
                factory.setPassword(credentials.getPassword());
            }

            connection = factory.createConnection();
        }

        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (credentials != null) {
            clientPublisher = new DefaultClientPublisher(credentials
                    .getUserPrincipal().getName(), session);
        } else {
            clientPublisher = new DefaultClientPublisher(session);
        }
    }

    /**
     * Sends request and gets response for synchronous communication. Defaults to
     * QUEUE destination type to match Python client behavior.
     *
     * @param request
     *            instance of pnnl.goss.core.Request or any of its subclass.
     * @return return an Object which could be a pnnl.goss.core.DataResponse,
     *         pnnl.goss.core.UploadResponse or pnnl.goss.core.DataError.
     * @throws IllegalStateException
     *             when GossCLient is initialized with an GossResponseEvent. Cannot
     *             synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    @Override
    public Serializable getResponse(Serializable message, String destinationName,
            RESPONSE_FORMAT responseFormat) throws SystemException, JMSException {
        // Default to QUEUE to match Python client behavior
        return getResponse(message, destinationName, responseFormat, DESTINATION_TYPE.QUEUE);
    }

    /**
     * Sends request and gets response for synchronous communication with specified
     * destination type. Blocks indefinitely for a reply; see
     * {@link #getResponse(Serializable, String, RESPONSE_FORMAT, DESTINATION_TYPE, long)}
     * for a bounded-wait variant.
     *
     * @param message
     *            instance of pnnl.goss.core.Request or any of its subclass.
     * @param destinationName
     *            the destination name (topic or queue)
     * @param responseFormat
     *            the response format
     * @param destinationType
     *            TOPIC or QUEUE
     * @return return an Object which could be a pnnl.goss.core.DataResponse,
     *         pnnl.goss.core.UploadResponse or pnnl.goss.core.DataError.
     * @throws IllegalStateException
     *             when GossCLient is initialized with an GossResponseEvent. Cannot
     *             synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    @Override
    public Serializable getResponse(Serializable message, String destinationName,
            RESPONSE_FORMAT responseFormat, DESTINATION_TYPE destinationType) throws SystemException, JMSException {
        // Preserve the unbounded-wait contract exactly: 0 means "block indefinitely"
        // per jakarta.jms.MessageConsumer#receive(long), the same as the no-arg
        // receive() this delegation replaced.
        return getResponse(message, destinationName, responseFormat, destinationType,
                UNBOUNDED_RECEIVE_TIMEOUT_MS);
    }

    /**
     * Sends request and gets response for synchronous communication, defaulting to
     * QUEUE destination type, bounded by an explicit receive timeout (GADP-051). See
     * {@link #getResponse(Serializable, String, RESPONSE_FORMAT, DESTINATION_TYPE, long)}
     * for the full timeout semantics.
     *
     * @param message
     *            instance of pnnl.goss.core.Request or any of its subclass.
     * @param destinationName
     *            the destination name (topic or queue)
     * @param responseFormat
     *            the response format
     * @param timeoutMillis
     *            maximum time to wait for a reply, in milliseconds. A value of
     *            {@code 0} blocks indefinitely (matches
     *            {@link jakarta.jms.MessageConsumer#receive(long)} semantics).
     * @return return an Object which could be a pnnl.goss.core.DataResponse,
     *         pnnl.goss.core.UploadResponse or pnnl.goss.core.DataError, or
     *         {@code null} if no reply arrived within {@code timeoutMillis}.
     * @throws IllegalStateException
     *             when GossCLient is initialized with an GossResponseEvent. Cannot
     *             synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    @Override
    public Serializable getResponse(Serializable message, String destinationName,
            RESPONSE_FORMAT responseFormat, long timeoutMillis) throws SystemException, JMSException {
        return getResponse(message, destinationName, responseFormat, DESTINATION_TYPE.QUEUE, timeoutMillis);
    }

    /**
     * Sends request and gets response for synchronous communication with specified
     * destination type, bounded by an explicit receive timeout (GADP-051). Unlike
     * the unbounded overloads, this returns {@code null} once {@code timeoutMillis}
     * elapses without a reply, rather than blocking the calling thread forever.
     *
     * @param message
     *            instance of pnnl.goss.core.Request or any of its subclass.
     * @param destinationName
     *            the destination name (topic or queue)
     * @param responseFormat
     *            the response format
     * @param destinationType
     *            TOPIC or QUEUE
     * @param timeoutMillis
     *            maximum time to wait for a reply, in milliseconds. A value of
     *            {@code 0} blocks indefinitely (matches
     *            {@link jakarta.jms.MessageConsumer#receive(long)} semantics).
     * @return return an Object which could be a pnnl.goss.core.DataResponse,
     *         pnnl.goss.core.UploadResponse or pnnl.goss.core.DataError, or
     *         {@code null} if no reply arrived within {@code timeoutMillis}.
     * @throws IllegalStateException
     *             when GossCLient is initialized with an GossResponseEvent. Cannot
     *             synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    @Override
    public Serializable getResponse(Serializable message, String destinationName,
            RESPONSE_FORMAT responseFormat, DESTINATION_TYPE destinationType, long timeoutMillis)
            throws SystemException, JMSException {
        if (protocol == null) {
            protocol = PROTOCOL.OPENWIRE;
        }

        if (destinationName == null) {
            return new ResponseError("destination cannot be null");
        }
        if (message == null) {
            return new ResponseError("message cannot be null");
        }

        Serializable response = null;

        // GADP-051 (session-isolation): the synchronous receive() below MUST run
        // on a Session that has NO MessageListener attached. The shared `session`
        // may carry async subscribe() listeners (for example FieldBusManager's
        // device-output subscription, which publishDeviceOutput() registers before
        // it issues its synchronous topology request on the same client), and
        // jakarta.jms forbids a synchronous receive() on such a session:
        // ActiveMQSession.checkMessageListener throws IllegalStateException,
        // "Cannot synchronously receive a message when a MessageListener is set".
        // That made every topology getResponse() throw instantly regardless of
        // timing, so the caller's bounded-retry window could never succeed.
        //
        // Fix: derive a dedicated, listener-free Session from the SAME Connection
        // for this request/reply, and close it in the finally block. A JMS
        // Connection supports many Sessions, so this isolates the synchronous
        // path from the async listener path without disturbing the shared
        // session's subscriptions. The request is still SENT via the shared
        // clientPublisher (publishing on a listener-bound session is permitted);
        // only the temporary reply destination, its consumer, and the synchronous
        // receive move onto the dedicated session. The session is always closed,
        // so no session is leaked per call.
        getSession(); // ensure the shared Connection exists before deriving a session from it
        Session syncSession = null;
        DefaultClientConsumer clientConsumer = null;
        try {
            syncSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination replyDestination = getTemporaryDestination(syncSession);
            Destination destination = getDestination(destinationName, destinationType);

            log.debug("Creating consumer for destination " + replyDestination + " (type: " + destinationType + ")");
            clientConsumer = new DefaultClientConsumer(syncSession, replyDestination);

            clientPublisher.sendMessage(message, destination, replyDestination,
                    responseFormat);
            Message responseMessage = clientConsumer.getMessageConsumer()
                    .receive(timeoutMillis);
            if (responseMessage == null) {
                // Timed out waiting for a reply (or the consumer was concurrently
                // closed, per the JMS receive(long) contract). A null Message here
                // means "no reply arrived in time", not "an empty reply": return
                // null explicitly rather than falling through to the TextMessage
                // cast below, which would NPE on a null responseMessage. Callers
                // that retry (e.g. FieldBusManager's bounded topology request) rely
                // on this null to mean "not ready yet, try again".
                log.debug("No response received on " + replyDestination + " within "
                        + timeoutMillis + "ms");
                return response;
            }
            // GADP-051 (message-type dispatch): check instanceof BEFORE casting.
            // The previous code unconditionally cast responseMessage to TextMessage
            // before these checks, so any reply that was NOT a TextMessage (for
            // example ActiveMQBytesMessage, which is how a reply arrives over the
            // STOMP-to-OpenWire bridge, e.g. the topology background service's
            // response) threw ClassCastException immediately. That exception was
            // previously masked by the session-listener IllegalStateException this
            // method used to throw first (GADP-051 session-split fix); once that
            // was fixed the latent bad cast surfaced. Handle each real message type
            // explicitly, matching the decode convention already used in
            // DefaultClientListener.onMessage for the same wire formats, and fail
            // loudly (data-invariants Rule 2) rather than defaulting to null/empty
            // on an unrecognized type: the body is consumed downstream (e.g.
            // TopologyRequestProcess builds its topology map from it), so a wrong
            // or empty body would be silent data corruption, not just a crash.
            if (responseMessage instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) responseMessage;
                if (objectMessage.getObject() instanceof Response) {
                    // The reply carries a first-class pnnl.goss.core.Response
                    // (or subclass, e.g. DataResponse) written as the
                    // ObjectMessage payload by an OpenWire-side sender. Unwrap
                    // it directly rather than treating it as opaque
                    // Serializable, so callers get the typed Response contract
                    // they expect from this method's declared return.
                    response = (Response) objectMessage.getObject();
                } else {
                    // The reply carries some other Serializable payload (not a
                    // Response). This method's contract is "return whatever
                    // was sent", so pass it through unchanged rather than
                    // rejecting it: the sender, not this client, decides what
                    // is a valid payload shape.
                    response = (Serializable) objectMessage.getObject();
                }
            } else if (responseMessage instanceof TextMessage) {
                response = ((TextMessage) responseMessage).getText();
            } else if (responseMessage instanceof BytesMessage) {
                // BytesMessage is used by STOMP clients (Python, JavaScript, etc.)
                // and by replies bridged from STOMP to OpenWire. Decode with the
                // same UTF-8 convention DefaultClientListener.onMessage uses for
                // the equivalent BytesMessage case, so both synchronous and
                // asynchronous receive paths interpret the wire body identically.
                BytesMessage bytesMessage = (BytesMessage) responseMessage;
                long bodyLength = bytesMessage.getBodyLength();
                // Validate the wire-supplied length BEFORE allocating. An
                // unbounded/negative bodyLength allocated directly into `new
                // byte[(int) bodyLength]` is an OOM denial-of-service vector
                // (and the (int) cast is lossy/negative past
                // Integer.MAX_VALUE, which would throw
                // NegativeArraySizeException from inside the allocation
                // rather than a clear diagnostic). A real topology/response
                // JSON reply is a few MB at most, so reject anything outside
                // a sane cap loudly, matching the else-branch's fail-loud
                // contract, rather than allocating from an unvalidated value.
                if (bodyLength < 0 || bodyLength > MAX_BYTES_MESSAGE_BODY_LENGTH) {
                    throw SystemException.wrap(new JMSException(
                            "BytesMessage reply body length " + bodyLength
                                    + " is negative or exceeds the "
                                    + MAX_BYTES_MESSAGE_BODY_LENGTH
                                    + "-byte cap for getResponse replies"))
                            .set("destination", destinationName)
                            .set("message", message);
                }
                byte[] bytes = new byte[(int) bodyLength];
                bytesMessage.readBytes(bytes);
                response = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            } else {
                // An unexpected/unsupported JMS message type. Do not silently
                // return null or an empty body here: that would convert a visible
                // failure into invisible data loss for whatever consumed the
                // response downstream. Fail loudly with the concrete type so the
                // caller (and its logs) can diagnose it.
                throw SystemException.wrap(new JMSException(
                        "Unsupported JMS message type for getResponse reply: "
                                + responseMessage.getClass().getName()))
                        .set("destination", destinationName)
                        .set("message", message);
            }

        } catch (JMSException e) {
            // GOSS-023: a genuine JMSException here (a real transport/provider
            // failure, e.g. the broker or consumer going away mid-receive) must
            // never be silently coerced into the same null result as a genuine
            // timeout. The previous code constructed a SystemException and threw
            // it away without ever calling throw, so this catch block was a dead
            // computation: the failure was swallowed and getResponse fell through
            // to "return response" (still null) after only the few milliseconds
            // it took the provider to report the failure, not the requested
            // timeoutMillis. That made a hard error indistinguishable from
            // "no reply arrived in time" and collapsed callers' bounded-retry
            // windows (see GADP-051's TopologyRequestProcess, whose designed
            // ~19s retry budget collapsed to ~4s because every attempt returned
            // in a few ms instead of blocking). Surface it instead, matching the
            // getTemporaryDestination/getDestination pattern already used
            // elsewhere in this class: throw, don't swallow.
            throw SystemException.wrap(e).set("destination", destinationName).set("message", message);

        } finally {
            // Guard the consumer close the same way the session close below is
            // guarded: if consumer.close() throws, that must not skip the
            // syncSession.close() that follows, which would otherwise leak the
            // dedicated request/reply session. Log with context rather than
            // swallowing silently.
            if (clientConsumer != null) {
                try {
                    clientConsumer.close();
                } catch (Exception e) {
                    log.warn("Failed to close synchronous request/reply consumer for destination {}",
                            destinationName, e);
                }
            }
            // Close the dedicated request/reply session so it is not leaked per
            // call. Guard the close so a failure tearing down this session cannot
            // mask a response already computed above, and log it with context
            // rather than swallowing it silently.
            if (syncSession != null) {
                try {
                    syncSession.close();
                } catch (JMSException e) {
                    log.warn("Failed to close synchronous request/reply session for destination {}",
                            destinationName, e);
                }
            }
        }

        return response;
    }

    /**
     * Lets the client subscribe to a Topic of the given name for event based
     * communication.
     *
     * @param topicName
     *            throws IllegalStateException if GossCLient is not initialized with
     *            an GossResponseEvent. Cannot asynchronously receive a message when
     *            a MessageListener is not set. throws JMSException
     */
    public Client subscribe(String topicName, GossResponseEvent event)
            throws SystemException {
        try {
            if (event == null)
                throw new NullPointerException("event cannot be null");
            Destination destination = null;
            if (this.protocol.equals(PROTOCOL.OPENWIRE) || this.protocol.equals(PROTOCOL.STOMP)) {
                // Both OPENWIRE and STOMP use the same JMS patterns with ActiveMQ
                destination = getDestination(topicName);
                ClientConsumer clientConsumer = new DefaultClientConsumer(
                        new DefaultClientListener(event), session, destination);
                subscriptionConsumers.add(clientConsumer);
            }
        } finally {

        }

        return this;
    }

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
    @Override
    public Client subscribe(String destinationName, GossResponseEvent event, DESTINATION_TYPE destinationType)
            throws SystemException {
        try {
            if (event == null)
                throw new NullPointerException("event cannot be null");
            Destination destination = null;
            if (this.protocol.equals(PROTOCOL.OPENWIRE) || this.protocol.equals(PROTOCOL.STOMP)) {
                // Both OPENWIRE and STOMP use the same JMS patterns with ActiveMQ
                destination = getDestination(destinationName, destinationType);
                ClientConsumer clientConsumer = new DefaultClientConsumer(
                        new DefaultClientListener(event), session, destination);
                subscriptionConsumers.add(clientConsumer);
            }
        } finally {

        }

        return this;
    }

    @Override
    public void publish(String topic, Serializable data) throws SystemException {
        try {
            if (data == null)
                throw new NullPointerException("event cannot be null");

            Destination destination = getDestination(topic);

            if (data instanceof String)
                clientPublisher.publish(destination, data);
            else {
                Gson gson = new Gson();
                clientPublisher.publish(destination, gson.toJson(data));
            }

        } catch (JMSException e) {
            log.error("publish error", e);

            try {
                // Ran into error publishing, reset the session and try again
                log.info("Renewing session");
                session = null;
                getSession();
                Destination destination = getDestination(topic);

                if (data instanceof String) {
                    clientPublisher.publish(destination, data);
                } else {
                    Gson gson = new Gson();
                    clientPublisher.publish(destination, gson.toJson(data));
                }
            } catch (Exception e2) {
                log.error("Failed second attempt to publish ", e2);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Ran into error publishing, reset the session and try again
                log.info("Renewing session");
                session = null;
                getSession();
                Destination destination = getDestination(topic);

                if (data instanceof String) {
                    clientPublisher.publish(destination, data);
                } else {
                    Gson gson = new Gson();
                    clientPublisher.publish(destination, gson.toJson(data));
                }
            } catch (Exception e2) {
                log.error("Failed second attempt to publish ", e2);
                e.printStackTrace();
                throw SystemException.wrap(e);
            }
        }
    }

    /**
     * Publish a message to a destination with specified type.
     *
     * @param destinationName
     *            the destination name
     * @param data
     *            the message to publish
     * @param destinationType
     *            TOPIC or QUEUE
     * @throws SystemException
     */
    @Override
    public void publish(String destinationName, Serializable data, DESTINATION_TYPE destinationType)
            throws SystemException {
        try {
            if (data == null)
                throw new NullPointerException("data cannot be null");

            Destination destination = getDestination(destinationName, destinationType);

            if (data instanceof String)
                clientPublisher.publish(destination, data);
            else {
                Gson gson = new Gson();
                clientPublisher.publish(destination, gson.toJson(data));
            }

        } catch (JMSException e) {
            log.error("publish error", e);

            try {
                // Ran into error publishing, reset the session and try again
                log.info("Renewing session");
                session = null;
                getSession();
                Destination destination = getDestination(destinationName, destinationType);

                if (data instanceof String) {
                    clientPublisher.publish(destination, data);
                } else {
                    Gson gson = new Gson();
                    clientPublisher.publish(destination, gson.toJson(data));
                }
            } catch (Exception e2) {
                log.error("Failed second attempt to publish ", e2);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Ran into error publishing, reset the session and try again
                log.info("Renewing session");
                session = null;
                getSession();
                Destination destination = getDestination(destinationName, destinationType);

                if (data instanceof String) {
                    clientPublisher.publish(destination, data);
                } else {
                    Gson gson = new Gson();
                    clientPublisher.publish(destination, gson.toJson(data));
                }
            } catch (Exception e2) {
                log.error("Failed second attempt to publish ", e2);
                e.printStackTrace();
                throw SystemException.wrap(e);
            }
        }
    }

    @Override
    public void publish(Destination destination, Serializable data) throws SystemException {
        try {
            if (data == null)
                throw new NullPointerException("data cannot be null");

            if (data instanceof String)
                clientPublisher.publish(destination, data);
            else {
                Gson gson = new Gson();
                clientPublisher.publish(destination, gson.toJson(data));
            }

        } catch (JMSException e) {
            log.error("publish error", e);

            try {
                // Ran into error publishing, reset the session and try again
                log.info("Renewing session");
                session = null;
                getSession();
                if (data instanceof String) {
                    clientPublisher.publish(destination, data);
                } else {
                    Gson gson = new Gson();
                    clientPublisher.publish(destination, gson.toJson(data));
                }
            } catch (Exception e2) {
                log.error("Failed second attempt to publish ", e2);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Ran into error publishing, reset the session and try again
                log.info("Renewing session");
                session = null;
                getSession();
                if (data instanceof String) {
                    clientPublisher.publish(destination, data);
                } else {
                    Gson gson = new Gson();
                    clientPublisher.publish(destination, gson.toJson(data));
                }
            } catch (Exception e2) {
                log.error("Failed second attempt to publish ", e2);
                e.printStackTrace();
                throw SystemException.wrap(e);
            }
        }
    }

    /*
     * private void publishTo(Destination destination, Serializable data) throws
     * SystemException { try { clientPublisher.publishTo(destination, data); } catch
     * (JMSException e) { SystemException.wrap(e).set("destination",
     * destination).set("data", data); } }
     */

    /**
     * Closes the GossClient connection with server.
     */
    @Override
    public void close() {
        try {
            log.debug("Client closing!");

            // Close every consumer created by subscribe() before the session closes,
            // so the MessageConsumer (and its listener thread) is deregistered from
            // the broker instead of being abandoned. Each close() is individually
            // guarded: one consumer throwing must not skip the remaining consumers,
            // the list clear below, or the session/connection teardown that follows,
            // otherwise a single bad consumer strands the authenticated session.
            for (ClientConsumer clientConsumer : subscriptionConsumers) {
                try {
                    clientConsumer.close();
                } catch (Exception e) {
                    log.warn("Failed to close subscription consumer {}; continuing to close remaining consumers",
                            clientConsumer, e);
                }
            }
            subscriptionConsumers.clear();

            if (session != null) {
                session.close();
                session = null;
            }

            connection = null;
            clientPublisher = null;
        } catch (JMSException e) {
            log.error("Close Error", e);
        }

    }

    private Session getSession() throws SystemException {
        if (session == null) {
            try {
                // Will throw exceptions if not able to create session.
                if (protocol == PROTOCOL.SSL) {
                    createSslSession();
                } else {
                    createSession();
                }
            } catch (JMSException e) {
                throw SystemException.wrap(e, ConnectionCode.SESSION_ERROR);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw SystemException.wrap(e);
            }
        }

        return session;
    }

    // Create the temporary reply queue on the supplied session. A JMS
    // TemporaryQueue is scoped to the Session (really the Connection) that
    // created it and can only be consumed by that same session, so the
    // synchronous getResponse() path passes its dedicated, listener-free session
    // here (GADP-051) rather than the shared listener-bearing `session`.
    private Destination getTemporaryDestination(Session destinationSession) throws SystemException {
        Destination destination = null;

        try {
            if (protocol.equals(PROTOCOL.SSL)) {
                destination = destinationSession.createTemporaryQueue();
                if (destination == null) {
                    throw new SystemException(ConnectionCode.DESTINATION_ERROR);
                }
            } else {
                if (protocol.equals(PROTOCOL.OPENWIRE) || protocol.equals(PROTOCOL.STOMP)) {
                    // Both OPENWIRE and STOMP use standard JMS with ActiveMQ
                    destination = destinationSession.createTemporaryQueue();
                    if (destination == null) {
                        throw new SystemException(
                                ConnectionCode.DESTINATION_ERROR);
                    }
                }
            }
        } catch (JMSException e) {
            throw SystemException.wrap(e).set("destination", "null");
        }

        return destination;
    }

    private Destination getDestination(String topicName) throws SystemException {
        return getDestination(topicName, DESTINATION_TYPE.TOPIC);
    }

    private Destination getDestination(String destinationName, DESTINATION_TYPE destinationType)
            throws SystemException {
        Destination destination = null;

        try {
            if (protocol.equals(PROTOCOL.OPENWIRE) || protocol.equals(PROTOCOL.STOMP)) {
                // Strip STOMP-style prefixes from destination names.
                // STOMP protocol uses /topic/ and /queue/ prefixes to identify destination
                // type, but JMS uses the raw name. Since this client uses JMS/OpenWire
                // internally (even for PROTOCOL.STOMP), we need to strip the prefixes
                // so JMS topic names match what STOMP clients (browser, Python) send to.
                if (destinationName.startsWith("/topic/")) {
                    destinationName = destinationName.substring(7);
                    destinationType = DESTINATION_TYPE.TOPIC;
                } else if (destinationName.startsWith("/queue/")) {
                    destinationName = destinationName.substring(7);
                    destinationType = DESTINATION_TYPE.QUEUE;
                }

                // Both OPENWIRE and STOMP use standard JMS with ActiveMQ
                if (destinationType == DESTINATION_TYPE.QUEUE) {
                    destination = getSession().createQueue(destinationName);
                } else {
                    destination = getSession().createTopic(destinationName);
                }

                if (destination == null) {
                    throw new SystemException(ConnectionCode.DESTINATION_ERROR);
                }
            }
        } catch (JMSException e) {
            throw SystemException.wrap(e).set("destination", "null");
        }

        return destination;
    }

    public Client setCredentials(Credentials credentials)
            throws SystemException {

        this.credentials = credentials;
        return this;
    }

    @Override
    public PROTOCOL getProtocol() {
        return protocol;
    }

    /**
     * Reset the client to an initial un-connected state. If the client currently
     * has a session, then the session should be closed. If credentials are set then
     * they will be unset after this call. The protocol of the client will not be
     * changed.
     */
    public void reset() {

    }

    /**
     * Returns whether the current instances is being used or if it can be used by
     * another process.
     *
     * @return
     */
    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        if (used == false) {
            if (session != null) {
                throw new IllegalStateException(
                        "Cannot set unused without reset.");
            }
        }
        this.used = used;
    }

    /**
     * An implementation that allows the caching of clients for future use.
     *
     * @return
     */
    public String getClientId() {
        return uuid.toString();
    }

}
