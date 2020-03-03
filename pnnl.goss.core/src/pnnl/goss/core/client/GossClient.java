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
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.http.auth.Credentials;
import org.fusesource.stomp.jms.StompJmsConnection;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.StompJmsTempQueue;
import org.fusesource.stomp.jms.StompJmsTopic;
import org.fusesource.stomp.jms.message.StompJmsBytesMessage;
import org.fusesource.stomp.jms.message.StompJmsTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Client;
import pnnl.goss.core.ClientConsumer;
import pnnl.goss.core.ClientPublishser;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.security.GossSecurityManager;
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
	private PROTOCOL protocol;
	private Credentials credentials = null;
	private String token = null;

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

			System.out.println("CLIENT GETTING TOKEN");
			String token = getToken(credentials);
			System.out.println("TOKEN IS "+token);
		
		} else {
			System.out.println("NO CREDENTIALS");
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
			StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
			factory.setBrokerURI(stompUri.replace("stomp", "tcp"));

			if (credentials != null) {
				connection = factory.createConnection(credentials
						.getUserPrincipal().getName(), credentials
						.getPassword());
			} else {
				connection = factory.createConnection();
			}
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
	 * Sends request and gets response for synchronous communication.
	 *
	 * @param request
	 *            instance of pnnl.goss.core.Request or any of its subclass.
	 * @return return an Object which could be a pnnl.goss.core.DataResponse,
	 *         pnnl.goss.core.UploadResponse or pnnl.goss.core.DataError.
	 * @throws IllegalStateException
	 *             when GossCLient is initialized with an GossResponseEvent.
	 *             Cannot synchronously receive a message when a MessageListener
	 *             is set.
	 * @throws JMSException
	 */
	@Override
	public Serializable getResponse(Serializable message, String topic,
			RESPONSE_FORMAT responseFormat) throws SystemException, JMSException {
		if (protocol == null) {
			protocol = PROTOCOL.OPENWIRE;
		}

		if (topic == null) {
			// TODO handle with a ErrorCode lookup!
			return new ResponseError("topic cannot be null");
		}
		if (message == null) {
			// TODO handle with a ErrorCode lookup!
			return new ResponseError("message cannot be null");
		}

		Serializable response = null;
		Destination replyDestination = getTemporaryDestination();
		Destination destination = session.createQueue(topic);
		
		log.debug("Creating consumer for destination "+replyDestination);
		DefaultClientConsumer clientConsumer = new DefaultClientConsumer(
				session, replyDestination);
		try {
			clientPublisher.sendMessage(message, destination, replyDestination,
					responseFormat);
			Message responseMessage = clientConsumer.getMessageConsumer()
					.receive();
			response = ((TextMessage) responseMessage).getText();
			if (responseMessage instanceof ObjectMessage) {
				ObjectMessage objectMessage = (ObjectMessage) responseMessage;
				if (objectMessage.getObject() instanceof Response) {
					response = (Response) objectMessage.getObject();
				}
			} else if (responseMessage instanceof TextMessage) {
				response = ((TextMessage) responseMessage).getText();
			}

		} catch (JMSException e) {
			SystemException.wrap(e).set("topic", topic).set("message", message);

		} finally {
			if (clientConsumer != null) {
				clientConsumer.close();
			}
		}

		return response;
	}

	/**
	 * Lets the client subscribe to a Topic of the given name for event based
	 * communication.
	 *
	 * @param topicName
	 *            throws IllegalStateException if GossCLient is not initialized
	 *            with an GossResponseEvent. Cannot asynchronously receive a
	 *            message when a MessageListener is not set. throws JMSException
	 */
	public Client subscribe(String topicName, GossResponseEvent event)
			throws SystemException {
		try {
			if (event == null)
				throw new NullPointerException("event cannot be null");
			Destination destination = null;
			if (this.protocol.equals(PROTOCOL.OPENWIRE)) {
				destination = getDestination(topicName);
				new DefaultClientConsumer(new DefaultClientListener(new ResponseEvent(this)),
						session, destination);
			} else if (this.protocol.equals(PROTOCOL.STOMP)) {
				Thread thread = new Thread(new Runnable() {
					Destination destination = new StompJmsDestination(topicName);
					DefaultClientConsumer consumer = new DefaultClientConsumer(
							session, destination);

					@Override
					public void run() {
						while (session != null) {
							try {
								Message msg = consumer.getMessageConsumer()
										.receive(10000);
								if (msg instanceof StompJmsBytesMessage) {
									StompJmsBytesMessage stompMessage = (StompJmsBytesMessage) msg;
									org.fusesource.hawtbuf.Buffer buffer = stompMessage
											.getContent();
									// System.out.println(buffer.toString().substring(buffer.toString().indexOf(":")+1));
									String message = buffer.toString()
											.substring(
													buffer.toString().indexOf(
															":") + 1);
									
									DataResponse dataResponse = new DataResponse(message);
									dataResponse.setDestination(msg.getJMSDestination().toString());
									if(msg.getJMSReplyTo() != null) {
										dataResponse.setReplyDestination(msg.getJMSReplyTo());
									}

									if(msg.getBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER)) {
										String username = msg.getStringProperty(SecurityConstants.SUBJECT_HEADER);
										dataResponse.setUsername(username);
									} else {
										log.warn("No username received in stomp message");
									}
									event.onMessage(dataResponse);
								}
								else if (msg instanceof StompJmsTextMessage) {
									StompJmsTextMessage stompMessage = (StompJmsTextMessage) msg;
									
									org.fusesource.hawtbuf.Buffer buffer = stompMessage
											.getContent();
									// System.out.println(buffer.toString().substring(buffer.toString().indexOf(":")+1));
									String message = buffer.toString()
											.substring(
													buffer.toString().indexOf(
															":") + 1);

									Gson gson = new Gson();
									DataResponse dataResponse;
									try{
										try {
											// don't fail if the message isn't already in data response format
											dataResponse = DataResponse.parse(message);
										} catch(JsonSyntaxException e){
											dataResponse = new DataResponse();
											dataResponse.setData(message);
										}
										dataResponse.setDestination(stompMessage.getStompJmsDestination().toString());
										if(msg.getJMSReplyTo() != null) {
											dataResponse.setReplyDestination(msg.getJMSReplyTo());
										}
										if(msg.getBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER)) {
											String username = msg.getStringProperty(SecurityConstants.SUBJECT_HEADER);
											dataResponse.setUsername(username);
										} else {
											log.warn("No username received in stomp message");
										}
										event.onMessage(dataResponse);
									}
									catch(JsonSyntaxException e){
										e.printStackTrace();
										dataResponse = new DataResponse(message);
										dataResponse.setDestination(stompMessage.getStompJmsDestination().toString());
										if(msg.getJMSReplyTo() != null)
											dataResponse.setReplyDestination(msg.getJMSReplyTo());
										if(msg.getBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER))
											dataResponse.setUsername(msg.getStringProperty(SecurityConstants.SUBJECT_HEADER));
										event.onMessage(dataResponse);
									}
									
								} else {
									//TODO warn of unknown message type???
								}
							} catch (JMSException ex) {
								// Happens when a timeout occurs.
								// log.debug("Illegal state? "+
								// ex.getMessage());
								if (session != null) {
									log.debug("Closing session");
									try {
										session.close();
									} catch (JMSException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									session = null;
								}
							}
						}
					}
				});

				thread.start();
				threads.add(thread);
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw SystemException.wrap(e);
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw SystemException.wrap(e);
		}
	}

	/*
	 * private void publishTo(Destination destination, Serializable data) throws
	 * SystemException { try { clientPublisher.publishTo(destination, data); }
	 * catch (JMSException e) { SystemException.wrap(e).set("destination",
	 * destination).set("data", data); } }
	 */

	/**
	 * Closes the GossClient connection with server.
	 */
	@Override
	public void close() {
		try {
			log.debug("Client closing!");
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

	private Destination getTemporaryDestination() throws SystemException {
		Destination destination = null;

		try {
			if (protocol.equals(PROTOCOL.SSL)) {
				destination = getSession().createTemporaryQueue();
				if (destination == null) {
					throw new SystemException(ConnectionCode.DESTINATION_ERROR);
				}
			} else {
				if (protocol.equals(PROTOCOL.OPENWIRE)) {

					destination = getSession().createTemporaryQueue();
					if (destination == null) {
						throw new SystemException(
								ConnectionCode.DESTINATION_ERROR);
					}
				} else if (protocol.equals(PROTOCOL.STOMP)) {
					destination = new StompJmsTempQueue("/queue/", UUID.randomUUID().toString());
				}
			}
		} catch (JMSException e) {
			throw SystemException.wrap(e).set("destination", "null");
		}

		return destination;
	}

	private Destination getDestination(String topicName) throws SystemException {
		Destination destination = null;

		try {
			if (protocol.equals(PROTOCOL.OPENWIRE)) {

				destination = getSession().createTopic(topicName);

				if (destination == null) {
					throw new SystemException(ConnectionCode.DESTINATION_ERROR);
				}
			} else if (protocol.equals(PROTOCOL.STOMP)) {
				if (connection == null) {
					throw new SystemException(ConnectionCode.CONNECTION_ERROR)
							.set("topicName", topicName);
				}
				destination = new StompJmsTopic(
						(StompJmsConnection) connection, topicName);
			}
		} catch (JMSException e) {
			throw SystemException.wrap(e).set("destination", "null");
		}

		return destination;
	}

	
	protected String getToken(Credentials credentials) throws JMSException{
		System.out.println("IN GET TOKEN");
		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI(stompUri.replace("stomp", "tcp"));
		Connection pwConnection = null;
		if (credentials != null) {
			pwConnection = factory.createConnection(credentials
					.getUserPrincipal().getName(), credentials
					.getPassword());
		} else {
			pwConnection = factory.createConnection();
		}
		
		System.out.println("CONN "+pwConnection);
		Session pwSession = pwConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination replyDestination = getTemporaryDestination();

		Destination destination = getDestination(SecurityManagerImpl.PROP_GOSS_LOGIN_TOPIC);
		ClientConsumer pwClientConsumer = new DefaultClientConsumer(new DefaultClientListener(new ResponseEvent(this)),
				pwSession, destination);
		ClientPublishser pwClientPublisher = new DefaultClientPublisher(credentials
				.getUserPrincipal().getName(), pwSession);
		pwClientPublisher.sendMessage("", destination, replyDestination,
				RESPONSE_FORMAT.JSON);
		Message responseMessage = pwClientConsumer.getMessageConsumer()
				.receive();
		Object response = ((TextMessage) responseMessage).getText();
		if (responseMessage instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage) responseMessage;
			if (objectMessage.getObject() instanceof Response) {
				response = (Response) objectMessage.getObject();
			}
		} else if (responseMessage instanceof TextMessage) {
			response = ((TextMessage) responseMessage).getText();
		}
		System.out.println("CLIENT GOT RESPONSE "+response);

		return null;
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
	 * Reset the client to an initial un-connected state. If the client
	 * currently has a session, then the session should be closed. If
	 * credentials are set then they will be unset after this call. The protocol
	 * of the client will not be changed.
	 */
	public void reset() {

	}

	/**
	 * Returns whether the current instances is being used or if it can be used
	 * by another process.
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
	
	class ResponseEvent implements GossResponseEvent{
		private final Client client;
		private Gson gson = new Gson();

		public ResponseEvent(Client client){
			this.client = client;
		}

		@Override
		public void onMessage(Serializable response) {
			String responseData = "{}";
			if (response instanceof DataResponse){
//				String request = (String)((DataResponse) response).getData();
//				if (request.trim().equals("list_handlers")){
//					//responseData = "Listing handlers here!";
//					responseData = gson.toJson(handlerRegistry.list());
//				}
//				else if (request.trim().equals("list_datasources")){
//					//responseData = "Listing Datasources here!";
//					responseData = gson.toJson(datasourceRegistry.getAvailable());
//				}
			}


			System.out.println("On message: "+response.toString());
			client.publish("goss/management/response", responseData);
		}

	}


}