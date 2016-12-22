package pnnl.goss.core;

import java.io.Serializable;

import pnnl.goss.core.Request.RESPONSE_FORMAT;

import com.northconcepts.exception.SystemException;

//import org.apache.activemq.ConfigurationException;

public interface Client {

	public enum PROTOCOL {
		OPENWIRE, STOMP, SSL
	};

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
			RESPONSE_FORMAT responseFormat) throws SystemException;

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