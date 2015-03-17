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
import static pnnl.goss.core.GossCoreContants.PROP_OPENWIRE_URI;
import static pnnl.goss.core.GossCoreContants.PROP_STOMP_URI;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.IllegalStateException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;







import javax.jms.Topic;






//import org.apache.activemq.ActiveMQConnectionFactory;
//import org.apache.activemq.ConfigurationException;
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
import pnnl.goss.core.ClientErrorCode;
import pnnl.goss.core.ClientPublishser;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Request;
import pnnl.goss.core.ResponseError;
import pnnl.goss.core.ResponseText;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.Response;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;











import org.apache.activemq.ActiveMQSslConnectionFactory;

import com.google.gson.Gson;
import com.northconcepts.exception.ConnectionCode;
import com.northconcepts.exception.SystemException;
import com.northconcepts.exception.ValidationCode;

public class GossClient implements Client{

    private static final Logger log = LoggerFactory.getLogger(GossClient.class);

    private UUID uuid = null;
    private String brokerUri = null;
    private ClientConfiguration config;
    private volatile ClientPublishser clientPublisher;
    private Connection connection = null;
    private Session session = null;
    private boolean used;
    private String clientTrustStore;
    private String clientTrustStorePassword;
    
    private boolean connected;
    

    private PROTOCOL protocol;
//    private PROTOCOL protocol;
    private Credentials credentials = null;

    public GossClient setProtocol(PROTOCOL protocol){
    	this.protocol = protocol;
    	return this;
    }
    
    
    public String getClientTrustStore() {
		return clientTrustStore;
	}



	public GossClient setClientTrustStore(String clientTrustStore) {
		this.clientTrustStore = clientTrustStore;
		return this;
	}



	public String getClientTrustStorePassword() {
		return clientTrustStorePassword;
	}



	public GossClient setClientTrustStorePassword(String clientTrustStorePassword) {
		this.clientTrustStorePassword = clientTrustStorePassword;
		return this;
	}



	private void createSslSession() throws Exception {
		ActiveMQSslConnectionFactory cf = new ActiveMQSslConnectionFactory(brokerUri);
        
		cf.setTrustStore(clientTrustStore);
        cf.setTrustStorePassword(clientTrustStorePassword);
        
        if (credentials != null){
        	cf.setUserName(credentials.getUserPrincipal().getName());
        	cf.setPassword(credentials.getPassword());
        }
        
        connection = (ActiveMQConnection)cf.createConnection();
        if (connection == null){
        	throw new SystemException(ConnectionCode.CONNECTION_ERROR);
        }
        
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (session == null){
        	throw new SystemException(ConnectionCode.SESSION_ERROR);
        }
        
        if (credentials != null){
        	clientPublisher = new DefaultClientPublisher(credentials.getUserPrincipal().getName(), session);
        }
        else {
        	clientPublisher = new DefaultClientPublisher(session);
        }
    }

    private void createSession() throws Exception{
               
        config = new ClientConfiguration()
        				.set("TCP_BROKER", brokerUri);
        
                
        if (credentials != null){
        	config.set("CREDENTIALS", credentials);
        }
        
        
        if (protocol.equals(PROTOCOL.SSL)){
        	createSslSession();
        }
        
        else if(protocol.equals(PROTOCOL.OPENWIRE)){
        	if (credentials != null){
        		log.debug("Creating OPENWIRE client session for "+credentials.getUserPrincipal());
        	}
        	else{
        		log.debug("Creating OPENWIRE client session without credentials");
        	}
        	
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUri);
//            factory.setUserName("system");
//            factory.setPassword("manager");
//            factory.setUseAsyncSend(true);
            
            if (credentials != null){
            	factory.setUserName(credentials.getUserPrincipal().getName());
            	factory.setPassword(credentials.getPassword());
            }
            	
            connection = factory.createConnection();
        }
        else if(protocol.equals(PROTOCOL.STOMP)){
            StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
            factory.setBrokerURI(brokerUri);
            
            if (credentials != null){
            	connection = factory.createConnection(credentials.getUserPrincipal().getName(), credentials.getPassword());
            }
            else{
            	connection = factory.createConnection();
            }
        }
             
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (credentials != null){
        	clientPublisher = new DefaultClientPublisher(credentials.getUserPrincipal().getName(), session);
        }
        else {
        	clientPublisher = new DefaultClientPublisher(session);
        }
    }
    
    /**
     * Sends request and gets response for synchronous communication.
     * @param request instance of pnnl.goss.core.Request or any of its subclass.
     * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
     * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    @Override
    public Response getResponse(Request request) throws SystemException  {
        if (protocol == null){
            protocol = PROTOCOL.OPENWIRE;
        }
        return getResponse(request,null);
    }

    /**
     * Sends request and gets response for synchronous communication.
     * @param request instance of pnnl.goss.core.Request or any of its subclass.
     * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
     * @throws JMSException
     */
    @Override
    public Response getResponse(Request request, RESPONSE_FORMAT responseFormat) throws SystemException {
        Response response = null;
        
        if (request == null){
        	// TODO handle with a ErrorCode lookup!
        	return new ResponseError("Cannot route a null request");
        }

        Destination replyDestination = getTemporaryDestination();
                
        DefaultClientConsumer clientConsumer = new DefaultClientConsumer(session, replyDestination);
        try {
			clientPublisher.sendMessage(request, replyDestination,
			        responseFormat);
			Object message = clientConsumer.getMessageConsumer().receive();
	        if (message instanceof ObjectMessage) {
	            ObjectMessage objectMessage = (ObjectMessage) message;
	            if (objectMessage.getObject() instanceof Response) {
	                response = (Response) objectMessage.getObject();
	            }
	        } else if (message instanceof TextMessage) {
	            response = new ResponseText(((TextMessage) message).getText());
	        }
		} catch (JMSException e) {
			SystemException.wrap(e)
				.set("request", request.getClass())
				.set("responseFormat", responseFormat);
							
		} finally {
			if (clientConsumer != null){
				clientConsumer.close();
			}
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
    public String sendRequest(Request request, GossResponseEvent event, RESPONSE_FORMAT responseFormat) throws SystemException {
        try{
            createSession();
            Destination replyDestination=getTemporaryDestination();
            
            if(event!=null){
                new DefaultClientConsumer(new DefaultClientListener(event),session,replyDestination);}
            else
                throw new NullPointerException("event cannot be null");
            
            clientPublisher.sendMessage(request,replyDestination,responseFormat);
            if(replyDestination!=null){
                return replyDestination.toString();
            }
        }
        catch(JMSException e){
            log.error("sendRequest Error", e);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw SystemException.wrap(e);
		}
        return null;
    }

    /**
     * Lets the client subscribe to a Topic of the given name for event based communication.
     * @param topicName
     * throws IllegalStateException if GossCLient is not initialized with an GossResponseEvent. Cannot asynchronously receive a message when a MessageListener is not set.
     * throws JMSException
     */
    public Client subscribeTo(String topicName, GossResponseEvent event) throws SystemException {
        try{
            createSession();
            if(event==null)
                throw new NullPointerException("event cannot be null");
            Destination destination = null;
            if(this.protocol.equals(PROTOCOL.OPENWIRE)){
                destination = getDestination(topicName); 
                new DefaultClientConsumer(new DefaultClientListener(event),session,destination);
            }
            else if(this.protocol.equals(PROTOCOL.STOMP)){
                destination = new StompJmsDestination(topicName);
                DefaultClientConsumer consumer  = new DefaultClientConsumer(session,destination);

                 while(session != null) {
                    try {
                        Message msg = consumer.getMessageConsumer().receive(10000);
                        if (msg instanceof StompJmsBytesMessage) {
                            StompJmsBytesMessage stompMessage = (StompJmsBytesMessage) msg;
                            org.fusesource.hawtbuf.Buffer buffer = stompMessage
                                    .getContent();
                            // System.out.println(buffer.toString().substring(buffer.toString().indexOf(":")+1));
                            String message = buffer.toString().substring(
                                    buffer.toString().indexOf(":") + 1);
                            event.onMessage(new DataResponse(message));
                        }
                        if (msg instanceof StompJmsTextMessage) {
                            StompJmsTextMessage stompMessage = (StompJmsTextMessage) msg;
                            org.fusesource.hawtbuf.Buffer buffer = stompMessage
                                    .getContent();
                            // System.out.println(buffer.toString().substring(buffer.toString().indexOf(":")+1));
                            String message = buffer.toString().substring(
                                    buffer.toString().indexOf(":") + 1);
                            event.onMessage(new DataResponse(message));
                        }
                    } catch (javax.jms.IllegalStateException ex) {
                        // Happens when a timeout occurs.
                        //log.debug("Illegal state? "+ ex.getMessage());
                        if (session != null){
                            log.debug("Closing session");
                            session.close();
                            session = null;
                        }
                    }
                 }
            }

        }
        catch(JMSException e){
            log.error("Subscribe Error", e);
            SystemException.wrap(e)
            	.set("topicName", topicName)
            	.set("event", event);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw SystemException.wrap(e);
		}
        
        return this;
    }

    @Override
    public void publish(String topicName, Serializable data, RESPONSE_FORMAT responseFormat) throws SystemException {
        try{
            createSession();
            if(data==null)
                throw new NullPointerException("event cannot be null");

            Destination destination = getDestination(topicName);
            
            if(responseFormat==null)
                clientPublisher.publishTo(destination, data);
            else if(responseFormat.equals(RESPONSE_FORMAT.JSON)){
                Gson gson = new Gson();
                clientPublisher.publishTo(destination, gson.toJson(data));
            }

        }
        catch(JMSException e){
            log.error("publish error", e);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw SystemException.wrap(e);
		}
    }

    @Override
    public void publishString(String topicName, String data) throws SystemException {
    	
    	Destination destination = getDestination(topicName);
    	publishTo(destination, data);
    	
    }
        
    private void publishTo(Destination destination, Serializable data) throws SystemException {
    	try {
			clientPublisher.publishTo(destination, data);
		} catch (JMSException e) {
			SystemException.wrap(e).set("destination", destination).set("data", data);
		}
    }

    /**
     * Closes the GossClient connection with server.
     */
    @Override
    public void close(){
        try{
            log.debug("Client closing!");
            if(session != null){
            	session.close();
            	session =null;
            }
            
            connection = null;
            clientPublisher = null;
        }
        catch(JMSException e){
            log.error("Close Error", e);
        }

    }
    
    private Session getSession() throws SystemException {
    	if (session == null){
    		try {
    			// Will throw exceptions if not able to create session.
    			if (protocol == PROTOCOL.SSL){
    				createSslSession();
    			}
    			else{
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
    		if (protocol.equals(PROTOCOL.SSL)){
    			destination = getSession().createTemporaryQueue();
    			if (destination ==  null){
    				throw new SystemException(ConnectionCode.DESTINATION_ERROR);
    			}
    		}
    		else{
		    	if (protocol.equals(PROTOCOL.OPENWIRE)){
		    		
					destination = getSession().createTemporaryQueue();
					if (destination == null) {
						throw new SystemException(ConnectionCode.DESTINATION_ERROR);
					}
				}
				else if(protocol.equals(PROTOCOL.STOMP)){
					destination = new StompJmsTempQueue();
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
	    	if (protocol.equals(PROTOCOL.OPENWIRE)){
	    		
				destination = getSession().createTopic(topicName);
				
				if (destination == null){
					throw new SystemException(ConnectionCode.DESTINATION_ERROR);
				}
			}
			else if(protocol.equals(PROTOCOL.STOMP)){
				if (connection == null) {
					throw new SystemException(ConnectionCode.CONNECTION_ERROR).set("topicName", topicName);
				}
				
				destination = new StompJmsTopic((StompJmsConnection)connection,topicName);
			}
    	} catch (JMSException e) {
    		throw SystemException.wrap(e).set("destination", "null");
    	}
    	
    	return destination;
    }

    @Override
    public void publish(String topicName, Serializable data)
            throws SystemException {
    	    	
    	Destination destination = null;
    	
    	try {
    		destination = getDestination(topicName);    		
    		clientPublisher.publishTo(destination, data);
    		
    	} catch (JMSException e) {
    		    		
    		throw SystemException.wrap(e)
    			.set("destination", destination)
    			.set("data", data);
		}
    }

    @Override
    public Client setCredentials(Credentials credentials) 
    		throws SystemException {
    	
    	this.credentials = credentials;
        return this;
    }


    @Override
    public PROTOCOL getProtocol() {
        // TODO Auto-generated method stub
        return protocol;
    }

    /**
     * Reset the client to an initial un-connected state.  If the client currently
     * has a session, then the session should be closed.  If credentials are set
     * then they will be unset after this call. The protocol of the client
     * will not be changed.
     */
    public void reset() {
        

    }

    /**
     * Returns whether the current instances is being used or if it can be
     * used by another process.
     * @return
     */
    public boolean isUsed(){
        return used;
    }

    public void setUsed(boolean used){
        if (used == false){
            if (session != null){
                throw new IllegalStateException("Cannot set unused without reset.");
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


	public GossClient setUri(String uri) {
		brokerUri = uri;
		return this;
	}
}