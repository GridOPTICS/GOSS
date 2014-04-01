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
package pnnl.goss.client;

/**
 * GridOPTICSClient class is used to create client for sending synchronous and asynchronous request to receive data and to upload data.
 */

import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.Request.RESPONSE_FORMAT;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import pnnl.goss.client.internal.ClientConsumer;
import pnnl.goss.client.internal.ClientListener;
import pnnl.goss.client.internal.ClientPublisher;

public class GossClient{


	ClientPublisher clientPublisher;
	ClientConsumer clientConsumer;
	Connection connection;
	Session session;
	GossResponseEvent event;
	
	public GossClient(String brokerURI){
		setUpSession(brokerURI);
	}
	
	private void setUpSession(String brokerURI){
		try{
			ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURI);
			((ActiveMQConnectionFactory)factory).setUseAsyncSend(true);		
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}

	/**
	 * Creates GridOPTICSClient for asynchronous communication.
	 * @param event implementation of pnnl.goss.client.GossResponseEvent interface.
	 */
	public GossClient(GossResponseEvent event) {
		try{
			
			String brokerURI = getBrokerUriFromConfig();
			setUpSession(brokerURI);
			this.event = event;
			
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates GridOPTICSClient for synchronous communication.
	 */
	public GossClient() {
		try{
			
			String brokerURI = getBrokerUriFromConfig();
			
			setUpSession(brokerURI);			
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}

	private String getBrokerUriFromConfig() throws IOException,
			FileNotFoundException {
		Properties properties = new Properties();
		InputStream input = GossClient.class.getResourceAsStream("/config.properties");
		if(input!=null)
			properties.load(input);
		else
			properties.load(new FileInputStream("config"+File.separatorChar+"config.properties"));
		String brokerURI = properties.getProperty("brokerURI");
		return brokerURI;
	}
	
	/**
	 * Receives response in synchronous communication
	 * @return gov.pnnl.goss.communication.Response  
	 * @throws IllegalStateException: If GridOPTICSClient is created in asynchronous way but is trying receive response using this method.
	 * @throws JMSException
	 */
/*	public Response getResponse() throws IllegalStateException,JMSException  {
		
		Response response=null;
		
		if( clientConsumer.messageConsumer.receive() instanceof ObjectMessage){
			ObjectMessage objectMessage = (ObjectMessage) clientConsumer.messageConsumer.receive();
			if(objectMessage.getObject() instanceof Response)
				response = (Response) objectMessage.getObject();
		}
		
		return  response;
	
	}*/
	
	public Object getResponse() throws IllegalStateException,JMSException  {
		
		Object message = clientConsumer.getMessageConsumer().receive();
		Object response=null;
		
		
		
		if(message instanceof ObjectMessage){
			ObjectMessage objectMessage = (ObjectMessage)message;
			if(objectMessage.getObject() instanceof Response){
				response = (Response) objectMessage.getObject();
			}
		}
		else if(message instanceof TextMessage){
			response = 	((TextMessage)message).getText();
			
		}
		
		return  response;
	
	}
	
	public Object getResponse(Request request) throws IllegalStateException, JMSException{
		this.sendRequest(request);
		return getResponse();
	}
	
	/**
	 * Allows the sending of uris through the existing request mechanism.  This is a patch
	 * until we get everything moved over to the new system.
	 * 
	 * @param uri
	 * @return
	 * @throws IllegalStateException
	 * @throws JMSException
	 */
	public Object getResponse(String uri) throws IllegalStateException, JMSException{
		Request request = new Request();
		request.setUrl(uri);
		sendRequest(request);
		return getResponse();
	}
	
	/**
	 * Sends request either to get data or to upload data.
	 * To get data, request object should extend gov.pnnl.goss.communication.RequestData. 
	 * To upload data, request object should extend gov.pnnl.goss.communication.RequestUpload.
	 * @param request gov.pnnl.goss.communication.Request.
	 */
	public void sendRequest(Request request){
		try{
			Destination destination = session.createTemporaryQueue();
			clientPublisher = new ClientPublisher(session, destination);
			if(event!=null)
				clientConsumer = new ClientConsumer(new ClientListener(event),session,destination);
			else
				clientConsumer = new ClientConsumer(session,destination);
			clientPublisher.sendMessage(request);
			
		}
		catch(JMSException e){
			e.printStackTrace();
		}
	}
	
	public void sendRequest(Request request, RESPONSE_FORMAT responseFormat){
		try{
			Destination destination = session.createTemporaryQueue();
			clientPublisher = new ClientPublisher(session, destination);
			if(event!=null)
				clientConsumer = new ClientConsumer(new ClientListener(event),session,destination);
			else
				clientConsumer = new ClientConsumer(session,destination);
			clientPublisher.sendMessage(request, responseFormat);
			
		}
		catch(JMSException e){
			e.printStackTrace();
		}
	}

	/**
	 * Sends request to upload data.
	 * @param request gov.pnnl.goss.communication.UploadRequest
	 */
	/*public void upload(UploadRequest uploadRequest){
		try{
			Destination destination = session.createTemporaryQueue();
			clientPublisher = new ClientPublisher(session, destination);
			clientConsumer = new ClientConsumer(new ClientListener(event),session,destination);
			clientPublisher.sendMessage(uploadRequest);
		}
		catch(JMSException e){
			e.printStackTrace();
		}

	}*/
	
	/**
	 * Let client subscribe to a Topic of the given name.
	 * @param topicName
	 */
	public void subscribeTo(String topicName){
		try{
			Destination destination = session.createTopic(topicName);
			clientConsumer = new ClientConsumer(new ClientListener(event),session,destination);
		}
		catch(JMSException e){
			e.printStackTrace();
		}
		
	}

	/**
	 * Closes the GridOPTICSClient.
	 */
	public void close(){
		try{
			session.close();
			connection.close();
			this.clientConsumer.close();
			this.clientPublisher.close();
		}
		catch(JMSException jms){
			jms.printStackTrace();
		}
	}

}

