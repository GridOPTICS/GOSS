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
package pnnl.goss.core.client.internal;

import static pnnl.goss.core.GossCoreContants.PROP_CORE_CLIENT_CONFIG;
import static pnnl.goss.core.GossCoreContants.PROP_OPENWIRE_URI;
import static pnnl.goss.core.GossCoreContants.PROP_STOMP_URI;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.IllegalStateException;
import java.util.Dictionary;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
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

import pnnl.goss.core.Client;
import pnnl.goss.core.ClientPublishser;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.Response;
import pnnl.goss.util.Utilities;

import com.google.gson.Gson;

public class GossClient implements Client{

    private static final Logger log = LoggerFactory.getLogger(GossClient.class);

    private ClientConfiguration config;
    volatile ClientPublishser clientPublisher;
    private Connection connection;
    private Session session;
    private boolean used;

    private boolean connected;

    private PROTOCOL protocol;
//    private PROTOCOL protocol;
    private Credentials credentials;

    /**
     * Creates GossClient for asynchronous communication.
     */
    public GossClient() {
        this((Credentials)null);
        log.debug("Constructor default!");

    }

    public GossClient(Properties props){
        config= new ClientConfiguration(props);
        assert config.getProperty(PROP_STOMP_URI) != null;
        assert config.getProperty(PROP_OPENWIRE_URI) != null;
        this.protocol = PROTOCOL.OPENWIRE;
    }

    public GossClient(String configFile) throws FileNotFoundException, IOException{
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));
        config = new ClientConfiguration(properties);
        setConfiguration(config);
        this.protocol = PROTOCOL.OPENWIRE;
    }

    public GossClient(PROTOCOL protocol) {
        this((Credentials)null,protocol);
        used = true;
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

        this.protocol = PROTOCOL.OPENWIRE;
        assert config.getProperty(PROP_OPENWIRE_URI) != null;
        assert config.getProperty(PROP_STOMP_URI) != null;
    }


    public void setConfiguration(Dictionary configuration){
        config = new ClientConfiguration(null);
        config.update(configuration);
        assert config.getProperty(PROP_OPENWIRE_URI) != null;
        assert config.getProperty(PROP_STOMP_URI) != null;
    }

    private boolean createSession() throws ConfigurationException{
        assert protocol != null;

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
            clientPublisher = new DefaultClientPublisher(session);
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
    @Override
    public Object getResponse(Request request) throws IllegalStateException,JMSException  {
        if (protocol == null){
            protocol = PROTOCOL.OPENWIRE;
        }
        return getResponse(request,null);
    }

    /**
     * Sends request and gets response for synchronous communication.
     * @param request instance of pnnl.goss.core.Request or any of its subclass.
     * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
     * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
     * @throws JMSException
     */
    @Override
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

            DefaultClientConsumer clientConsumer = new DefaultClientConsumer(session,
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
                        if (session != null){
                            session.close();
                            session = null;
                        }
                    }
                 }
            }

        }
        catch(JMSException e){
            log.error("Subscribe Error", e);
        }
    }

    @Override
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

    @Override
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

    /**
     * Closes the GossClient connection with server.
     */
    @Override
    public void close(){
        try{
            log.debug("Client closing!");
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

    @Override
    public void setCredentials(Credentials credentials) {
        if(credentials == null){
            throw new IllegalArgumentException("Credentials cannot be null!");
        }
        this.credentials = credentials;
    }


    @Override
    public PROTOCOL getProtocol() {
        // TODO Auto-generated method stub
        return protocol;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

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

    @Override
    public String getClientId() {
        return null;
    }
}