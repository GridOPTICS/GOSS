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

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ConfigurationException;
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

import pnnl.goss.core.Data;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.internal.ClientConfiguration;
import pnnl.goss.util.Utilities;
import static pnnl.goss.core.GossCoreContants.*;

import com.google.gson.Gson;

import static pnnl.goss.core.GossCoreContants.PROP_CORE_CONFIG;

public class GossClient implements Client{

	private static final Logger log = LoggerFactory.getLogger(GossClient.class);
	public enum PROTOCOL {OPENWIRE, STOMP};
	
	private ClientConfiguration config;
	volatile ClientPublisher clientPublisher;
	private Connection connection;
	private Session session;
	private PROTOCOL protocol;
	private Credentials credentials;
	
	
	
	/**
	 * Creates GossClient for asynchronous communication.
	 */
	public GossClient() {
		this((Credentials)null);
	}
	
	public GossClient(PROTOCOL protocol) {
		this((Credentials)null,protocol);
	}
	
	/**
	 * Creates GossClient for synchronous communication.
	 * @param cred is credentials containing username and password
	 */
	public GossClient(Credentials cred) {
		this.credentials = cred;
		this.protocol = PROTOCOL.OPENWIRE;
	}
	
	public GossClient(Credentials credentials, PROTOCOL protocol) {
		this.credentials = credentials;
		this.protocol = protocol;
	}
	
	
	public void setConfiguration(ClientConfiguration configuration){
		config = configuration;		
	}
	
	
	public void setConfiguration(Dictionary configuration){
		config = new ClientConfiguration(null);
		config.update(configuration);
	}
	
	private boolean createSession() throws ConfigurationException{
		if(config == null){
			config = new ClientConfiguration(Utilities.toProperties(Utilities.loadProperties(PROP_CORE_CLIENT_CONFIG)));
			
			if (config == null){
				throw new ConfigurationException("Invalid ClientConfiguration object!");
			}
		}
		
		if(session == null){
			setUpSession(this.credentials, this.protocol);
		}
		
		return session != null;
	}
		
	private void setUpSession(Credentials cred,PROTOCOL protocol){
		try{
			this.protocol = protocol;
			if(protocol.equals(PROTOCOL.OPENWIRE)){
				ConnectionFactory factory = new ActiveMQConnectionFactory(config.getProperty(PROP_OPENWIRE_URI));
				((ActiveMQConnectionFactory)factory).setUseAsyncSend(true);
				if(cred!=null){
					((ActiveMQConnectionFactory)factory).setUserName(cred.getUserPrincipal().getName());
					((ActiveMQConnectionFactory)factory).setPassword(cred.getPassword());
				}
				connection = factory.createConnection();
			}
			else if(protocol.equals(PROTOCOL.STOMP)){
				StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
				factory.setBrokerURI(config.getProperty(PROP_STOMP_URI));
				if(cred!=null)
					connection = factory.createConnection(cred.getUserPrincipal().getName(), cred.getPassword());	
				else
					connection = factory.createConnection();	
			}
			
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			clientPublisher = new ClientPublisher(session);
		}
		catch(Exception e){
			log.error("Error creating goss-client session", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends request and gets response for synchronous communication.
	 * @param request instance of pnnl.goss.core.Request or any of its subclass.
	 * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
	 * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
	 * @throws JMSException
	 */
	public Object getResponse(Request request) throws IllegalStateException,JMSException  {
		return getResponse(request,null);
	}

	/**
	 * Sends request and gets response for synchronous communication.
	 * @param request instance of pnnl.goss.core.Request or any of its subclass.
	 * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
	 * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
	 * @throws JMSException
	 */
	public Object getResponse(Request request, RESPONSE_FORMAT responseFormat) {
		Object response = null;
		try {
			createSession();
			Destination replyDestination = null;
			if (this.protocol.equals(PROTOCOL.OPENWIRE))
				replyDestination = session.createTemporaryQueue();
			else if (this.protocol.equals(PROTOCOL.STOMP)) {
				replyDestination = new StompJmsTempQueue();
			}

			ClientConsumer clientConsumer = new ClientConsumer(session,
					replyDestination);
			clientPublisher.sendMessage(request, replyDestination,
					responseFormat);
			Object message = clientConsumer.getMessageConsumer().receive();
			if (message instanceof ObjectMessage) {
				ObjectMessage objectMessage = (ObjectMessage) message;
				if (objectMessage.getObject() instanceof Response) {
					response = (Response) objectMessage.getObject();
				}
			} else if (message instanceof TextMessage) {
				response = ((TextMessage) message).getText();
			}

			clientConsumer.close();
		} catch (JMSException e) {
			log.error("getResponse Error", e);
		}

		return response;
	}
	
	/**
	 * Sends request and initializes listener for asynchronous communication
	 * To get data, request object should extend gov.pnnl.goss.communication.RequestData. 
	 * To upload data, request object should extend gov.pnnl.goss.communication.RequestUpload.
	 * @param request gov.pnnl.goss.communication.Request.
	 * @param instance of GossResponseEvent
	 * @return the replyDestination topic
	 */
	public String sendRequest(Request request,GossResponseEvent event,RESPONSE_FORMAT responseFormat) throws NullPointerException{
		try{
			createSession();
			Destination replyDestination=null;
			if(this.protocol.equals(PROTOCOL.OPENWIRE))
				replyDestination = session.createTemporaryQueue();
			else if(this.protocol.equals(PROTOCOL.STOMP)){
				replyDestination = new StompJmsTempQueue();
			}
			if(event!=null){
				new ClientConsumer(new ClientListener(event),session,replyDestination);}
			else
				throw new NullPointerException("event cannot be null");
			clientPublisher.sendMessage(request,replyDestination,responseFormat);
			if(replyDestination!=null){
				return replyDestination.toString();
			}
		}
		catch(JMSException e){
			log.error("sendRequest Error", e);
		}
		return null;
	}

	/**
	 * Lets the client subscribe to a Topic of the given name for event based communication.
	 * @param topicName
	 * throws IllegalStateException if GossCLient is not initialized with an GossResponseEvent. Cannot asynchronously receive a message when a MessageListener is not set.
	 * throws JMSException
	 */
	public void subscribeTo(String topicName, GossResponseEvent event) throws NullPointerException{
		try{
			createSession();
			if(event==null)
				throw new NullPointerException("event cannot be null");
			Destination destination = null;
			if(this.protocol.equals(PROTOCOL.OPENWIRE)){
				destination = session.createTopic(topicName);
				new ClientConsumer(new ClientListener(event),session,destination);
			}
			else if(this.protocol.equals(PROTOCOL.STOMP)){
				destination = new StompJmsDestination(topicName);
				ClientConsumer consumer  = new ClientConsumer(session,destination);
				//TODO change this
				 while(true) {
			            Message msg = consumer.getMessageConsumer().receive();
			            if(msg instanceof StompJmsBytesMessage){
			            	StompJmsBytesMessage stompMessage = (StompJmsBytesMessage)msg;
			            	org.fusesource.hawtbuf.Buffer buffer = stompMessage.getContent();
			            	//System.out.println(buffer.toString().substring(buffer.toString().indexOf(":")+1));
			            	String message = buffer.toString().substring(buffer.toString().indexOf(":")+1);
			            	event.onMessage(new DataResponse(message));
			            }
			            if(msg instanceof StompJmsTextMessage){
			            	StompJmsTextMessage stompMessage = (StompJmsTextMessage)msg;
			            	org.fusesource.hawtbuf.Buffer buffer = stompMessage.getContent();
			            	//System.out.println(buffer.toString().substring(buffer.toString().indexOf(":")+1));
			            	String message = buffer.toString().substring(buffer.toString().indexOf(":")+1);
			            	event.onMessage(new DataResponse(message));
			            }
				 }
			}
			
		}
		catch(JMSException e){
			log.error("Subscribe Error", e);
		}
	}
	
	public void publish(String topicName, Serializable data, RESPONSE_FORMAT responseFormat) throws NullPointerException{
		try{
			createSession();
			if(data==null)
				throw new NullPointerException("event cannot be null");
			
			Destination destination = null;
			if(this.protocol.equals(PROTOCOL.OPENWIRE))
				destination = session.createTopic(topicName);
			else if(this.protocol.equals(PROTOCOL.STOMP))
				destination = new StompJmsTopic((StompJmsConnection)connection,topicName);
			
			if(responseFormat==null)
				clientPublisher.publishTo(destination, data);
			else if(responseFormat.equals(RESPONSE_FORMAT.JSON)){
				Gson gson = new Gson();
				clientPublisher.publishTo(destination, gson.toJson(data));
			}
				
		}
		catch(JMSException e){
			log.error("publish error", e);
		}
	}
	
	@Deprecated
	/**
	 * Instead should use publishString
	 * @param topicName
	 * @param data
	 * @throws NullPointerException
	 */
	public void publish(String topicName, String data) throws NullPointerException{
		publishString(topicName, data);
	}
	public void publishString(String topicName, String data) throws NullPointerException{
		try{
			createSession();
			if(data==null)
				throw new NullPointerException("event cannot be null");
			Destination destination = null;
			if(this.protocol.equals(PROTOCOL.OPENWIRE))
				destination = session.createTopic(topicName);
			else if(this.protocol.equals(PROTOCOL.STOMP))
				destination = new StompJmsTopic((StompJmsConnection)connection,topicName);

			clientPublisher.publishTo(destination, data);
		}
		catch(JMSException e){
			log.error("publishString", e);
		}
	}
	
	@Override
	public void publish(String topicName, Data data,
			RESPONSE_FORMAT responseFormat) throws NullPointerException {
		try{
			createSession();
			if(data==null)
				throw new NullPointerException("event cannot be null");
			Destination destination = null;
			if(this.protocol.equals(PROTOCOL.OPENWIRE))
				destination = session.createTopic(topicName);
			else if(this.protocol.equals(PROTOCOL.STOMP))
				destination = new StompJmsTopic((StompJmsConnection)connection,topicName);

			clientPublisher.publishTo(destination, data);
		}
		catch(JMSException e){
			log.error("publish topic error", e);
		}
		
	}
	
	/**
	 * Closes the GossClient connection with server.
	 */
	public void close(){
		try{
			if(session!=null)
			session.close();
			if(connection!=null)
			connection.close();
			if(clientPublisher!=null)
			this.clientPublisher.close();
		}
		catch(JMSException e){
			log.error("Close Error", e);
		}
		
	}

	@Override
	public void publish(String topicName, Serializable data)
			throws NullPointerException {
		try{
			createSession();
			if(data==null)
				throw new NullPointerException("data cannot be null");
			Destination destination = null;
			if(this.protocol.equals(PROTOCOL.OPENWIRE))
				destination = session.createTopic(topicName);
			else if(this.protocol.equals(PROTOCOL.STOMP))
				destination = new StompJmsTopic((StompJmsConnection)connection,topicName);

			clientPublisher.publishTo(destination, data);
		}
		catch(JMSException e){
			log.error("publish data error", e);
		}
		
	}


	
	


}

