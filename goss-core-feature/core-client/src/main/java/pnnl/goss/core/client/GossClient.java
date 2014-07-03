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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.Credentials;
import org.fusesource.stomp.jms.StompJmsConnection;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.StompJmsTempQueue;
import org.fusesource.stomp.jms.StompJmsTopic;
import org.fusesource.stomp.jms.message.StompJmsBytesMessage;

import pnnl.goss.core.Data;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.internal.ClientConfiguration;

import com.google.gson.Gson;

public class GossClient {

	private static final Log log = LogFactory.getLog(GossClient.class);
	private static final String BROKER_URI_PROPERTY = "brokerURI";
	private static final String BROKER_STOMP_URI_PROPERTY = "brokerStompURI";
	public enum PROTOCOL {OPENWIRE, STOMP};
	
	volatile ClientPublisher clientPublisher;
	Connection connection;
	Session session;
	PROTOCOL protocol;
	
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
		try{
			setUpSession(cred, PROTOCOL.OPENWIRE);			
		}
		catch(Exception e ){
			log.error(e);
		}
	}
	
	public GossClient(Credentials cred, PROTOCOL protocol) {
		try{
			setUpSession(cred, protocol);			
		}
		catch(Exception e ){
			log.error(e);
		}
	}
	
	private void setUpSession(Credentials cred,PROTOCOL protocol){
		try{
			this.protocol = protocol;
			if(protocol.equals(PROTOCOL.OPENWIRE)){
				ConnectionFactory factory = new ActiveMQConnectionFactory(ClientConfiguration.getProperty(BROKER_URI_PROPERTY));
				((ActiveMQConnectionFactory)factory).setUseAsyncSend(true);
				if(cred!=null){
					((ActiveMQConnectionFactory)factory).setUserName(cred.getUserPrincipal().getName());
					((ActiveMQConnectionFactory)factory).setPassword(cred.getPassword());
				}
				connection = factory.createConnection();
			}
			else if(protocol.equals(PROTOCOL.STOMP)){
				StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
				factory.setBrokerURI(ClientConfiguration.getProperty(BROKER_STOMP_URI_PROPERTY));
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
	public Object getResponse(Request request, RESPONSE_FORMAT responseFormat){
		Object response=null;
		try{
		Destination replyDestination=null;
		if(this.protocol.equals(PROTOCOL.OPENWIRE))
			replyDestination = session.createTemporaryQueue();
		else if(this.protocol.equals(PROTOCOL.STOMP)){
			replyDestination = new StompJmsTempQueue();
		}
			
		ClientConsumer clientConsumer = new ClientConsumer(session,replyDestination);
		clientPublisher.sendMessage(request,replyDestination,responseFormat);
		Object message = clientConsumer.getMessageConsumer().receive();
		if(message instanceof ObjectMessage){
			ObjectMessage objectMessage = (ObjectMessage)message;
			if(objectMessage.getObject() instanceof Response){
				response = (Response) objectMessage.getObject();
			}
		}
		else if(message instanceof TextMessage){
			response = 	((TextMessage)message).getText();
		}
		
		clientConsumer.close();
		}
		catch(JMSException e){
			log.error(e);
		}
		
		
		return  response;
	}
	
	/**
	 * Sends request and initializes listener for asynchronous communication
	 * To get data, request object should extend gov.pnnl.goss.communication.RequestData. 
	 * To upload data, request object should extend gov.pnnl.goss.communication.RequestUpload.
	 * @param request gov.pnnl.goss.communication.Request.
	 * @param instance of GossResponseEvent
	 */
	public void sendRequest(Request request,GossResponseEvent event,RESPONSE_FORMAT responseFormat) throws NullPointerException{
		try{
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
		}
		catch(JMSException e){
			log.error(e);
		}
	}

	/**
	 * Lets the client subscribe to a Topic of the given name for event based communication.
	 * @param topicName
	 * throws IllegalStateException if GossCLient is not initialized with an GossResponseEvent. Cannot asynchronously receive a message when a MessageListener is not set.
	 * throws JMSException
	 */
	public void subscribeTo(String topicName, GossResponseEvent event) throws NullPointerException{
		try{
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
				 }
			}
			
		}
		catch(JMSException e){
			log.error(e);
		}
	}
	
	public void publish(String topicName, Serializable data, RESPONSE_FORMAT responseFormat) throws NullPointerException{
		try{
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
			log.error(e);
		}
	}
	
	public void publish(String topicName, String data) throws NullPointerException{
		try{
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
			log.error(e);
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
			log.error(e);
		}
		
	}
	
	


}

