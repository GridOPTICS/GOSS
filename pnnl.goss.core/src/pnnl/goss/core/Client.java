package pnnl.goss.core;

import java.io.Serializable;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;


//import org.apache.activemq.ConfigurationException;
import org.apache.http.auth.Credentials;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.Request.RESPONSE_FORMAT;

public interface Client {

    public enum PROTOCOL {OPENWIRE, STOMP, SSL};
    
    /**
     * Perform synchronous call to the server.
     * 
     * @param request instance of pnnl.goss.core.Request or any of its subclass.
     * @return a Response object.
     * @throws SystemException
     */
    public Response getResponse(Request request) throws SystemException;
  
    /**
     * Perform synchronous call to the server and format the response in a specific
     * manner.
     * 
     * @param request instance of pnnl.goss.core.Request or any of its subclass.
     * @param responseFormat 
     * @return a Response object.
     * @throws SystemException
     */
    public Response getResponse(Request request, RESPONSE_FORMAT responseFormat) 
    		throws SystemException;

    /**
     * Sends request and initializes listener for asynchronous communication
     * To get data, request object should extend gov.pnnl.goss.communication.RequestData.
     * To upload data, request object should extend gov.pnnl.goss.communication.RequestUpload.
     * @param request gov.pnnl.goss.communication.Request.
     * @param event of GossResponseEvent
     * @return the replyDestination topic
     */
    public String sendRequest(Request request, GossResponseEvent event,
            RESPONSE_FORMAT responseFormat) throws SystemException;

    /**
     * Lets the client subscribe to a Topic of the given name for event based communication.
     * @param topicName
     * throws IllegalStateException if GossCLient is not initialized with an GossResponseEvent. Cannot asynchronously receive a message when a MessageListener is not set.
     * throws JMSException
     */
    public Client subscribeTo(String topicName, GossResponseEvent event)
    		throws SystemException;

    public void publish(String topicName, Serializable data,
            RESPONSE_FORMAT responseFormat)  throws SystemException;

    public void publish(String topicName, Serializable data)
    		 throws SystemException;

    public void publishString(String topicName, String data)
    		 throws SystemException;

    /**
     * Close a connection with the server.
     */
    public void close();

    /**
     * Set the credentials on the client before sending traffic to the server.
     * 
     * @param credentials
     * @return
     * @throws SystemException
     */
    public Client setCredentials(Credentials credentials)
    		throws SystemException;;

    /**
     * Gets the type of protocol that the client will use to connect with.
     *
     * @return
     */
    public PROTOCOL getProtocol();

}