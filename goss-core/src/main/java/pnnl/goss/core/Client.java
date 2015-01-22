package pnnl.goss.core;

import java.io.Serializable;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.apache.activemq.ConfigurationException;
import org.apache.http.auth.Credentials;

import pnnl.goss.core.Request.RESPONSE_FORMAT;

public interface Client {

    public enum PROTOCOL {OPENWIRE, STOMP};


    /**
     * Sends request and gets response for synchronous communication.
     * @param request instance of pnnl.goss.core.Request or any of its subclass.
     * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
     * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    public Object getResponse(Request request)
            throws IllegalStateException, JMSException;

    /**
     * Sends request and gets response for synchronous communication.
     * @param request instance of pnnl.goss.core.Request or any of its subclass.
     * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
     * @throws ConfigurationException
     * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    public Object getResponse(Request request,
            RESPONSE_FORMAT responseFormat) throws ConfigurationException, JMSException;

    /**
     * Sends request and initializes listener for asynchronous communication
     * To get data, request object should extend gov.pnnl.goss.communication.RequestData.
     * To upload data, request object should extend gov.pnnl.goss.communication.RequestUpload.
     * @param request gov.pnnl.goss.communication.Request.
     * @param event of GossResponseEvent
     * @return the replyDestination topic
     */
    public String sendRequest(Request request, GossResponseEvent event,
            RESPONSE_FORMAT responseFormat) throws NullPointerException;

    /**
     * Lets the client subscribe to a Topic of the given name for event based communication.
     * @param topicName
     * throws IllegalStateException if GossCLient is not initialized with an GossResponseEvent. Cannot asynchronously receive a message when a MessageListener is not set.
     * throws JMSException
     */
    public void subscribeTo(String topicName, GossResponseEvent event)
            throws NullPointerException;

    public void publish(String topicName, Serializable data,
            RESPONSE_FORMAT responseFormat) throws NullPointerException;

    public void publish(String topicName, Serializable data)
            throws NullPointerException;

    public void publishString(String topicName, String data)
            throws NullPointerException;

    /**
     * Closes the GossClient connection with server.
     */
    public void close();

    /**
     * Reset the client to an initial un-connected state.  If the client currently
     * has a session, then the session should be closed.  If credentials are set
     * then they will be unset after this call. The protocol of the client
     * will not be changed.
     */
    public void reset();

    /**
     * A unique identifier for this client.
     *
     * @return A UUID string.
     */
    public String getClientId();

    public void setCredentials(Credentials credentials);

    /**
     * Gets the type of protocol that the client will use to connect with the
     * message bus.
     *
     * @return
     */
    public PROTOCOL getProtocol();

}