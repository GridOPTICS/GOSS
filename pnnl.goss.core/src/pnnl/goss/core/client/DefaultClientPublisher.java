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

import java.io.File;
import java.io.Serializable;
import java.util.Random;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.BlobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.ClientPublishser;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.security.SecurityConstants;

public class DefaultClientPublisher implements ClientPublishser {

    private transient Session session;
    private transient MessageProducer producer;
    private transient String username;
    private static Logger log = LoggerFactory.getLogger(DefaultClientPublisher.class);
    
    public DefaultClientPublisher(Session session){
    	this(null, session);
    }

    public DefaultClientPublisher(String username, Session session){
        try{
            this.session = session;
            this.username = username;
            producer = this.session.createProducer(null);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        try{
            producer.close();
        }
        catch(JMSException e){
            e.printStackTrace();
        }
    }
    
    @Override
	public void sendMessage(Serializable message, Destination destination, 
			Destination replyDestination,
			RESPONSE_FORMAT responseFormat) throws JMSException {
    	
    	Message messageObj = null;
    	
    	if(message instanceof String)
    		messageObj = session.createTextMessage(message.toString());
    	else
    		messageObj = session.createObjectMessage(message);
    	//TODO: throw error in else
    	messageObj.setBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER, username != null);
        if (username != null)
        	messageObj.setStringProperty(SecurityConstants.SUBJECT_HEADER, username);
        messageObj.setJMSReplyTo(replyDestination);
        String correlationId = this.createRandomString();
        messageObj.setJMSCorrelationID(correlationId);
        messageObj.setJMSDestination(destination);
        if(responseFormat!=null)
        	messageObj.setStringProperty("RESPONSE_FORMAT", responseFormat.toString());
        log.debug("Sending: "+ message+ " on destination: " + destination);
        producer.send(destination, messageObj);
		
	}

    public void publish(Destination destination, Serializable data) throws JMSException {
    	Message message= null;
    	if(data instanceof String)
    		 message = session.createTextMessage(data.toString());
    	else
    		message = session.createObjectMessage(data);
    	
    	if(message!=null)
    		message.setBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER, username != null);
		if(username != null)
        	message.setStringProperty(SecurityConstants.SUBJECT_HEADER, username);
        log.debug("Publishing: "+ data.getClass()+ " on destination: " + destination);
        producer.send(destination, message);
    }

    public void publishBlobMessage(Destination destination, File file) throws JMSException {
    	ActiveMQSession activeMQSession = (ActiveMQSession) session;
    	BlobMessage message  = activeMQSession.createBlobMessage(file);
    	message.setBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER, username != null);
		if (username != null)
			message.setStringProperty(SecurityConstants.SUBJECT_HEADER, username);
        log.debug("Publishing on destination: " + destination);
        producer.send(destination, message);
    }
    	
	private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }


}
