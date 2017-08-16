package gov.pnnl.goss.client.api;


import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;

import com.northconcepts.exception.SystemException;

import gov.pnnl.goss.client.api.ResponseFormat;

//import org.apache.activemq.ConfigurationException;

@SuppressWarnings("restriction")
public interface Client {
	
	/**
	 * Makes synchronous call to the server
	 * 
	 * @param request
	 * @param topic
	 * @param responseFormat
	 * @return
	 * @throws SystemException
	 */
	public Serializable getResponse(Serializable request, String topic,
			ResponseFormat responseFormat) throws SystemException, JMSException;

	/**
	 * Lets the client subscribe to a Topic of the given name for event based
	 * communication.
	 * 
	 * @param topicName
	 *            throws IllegalStateException if GossCLient is not initialized
	 *            with an GossResponseEvent. Cannot asynchronously receive a
	 *            message when a MessageListener is not set. throws JMSException
	 */
	public Client subscribe(String topic, GossResponseEvent event)
			throws SystemException;

	public void publish(String topicName, Serializable message)
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
	public GossProtocol getProtocol();

}