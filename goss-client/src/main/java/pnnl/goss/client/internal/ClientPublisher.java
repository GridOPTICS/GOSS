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
package pnnl.goss.client.internal;


import pnnl.goss.core.Data;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQObjectMessage;

public class ClientPublisher {

	private transient Session session;
	private transient MessageProducer producer;
	private Destination replyDestination;

	/*public ClientPublisher() throws JMSException {
		try {
			System.out.println(new File(System.getProperty("user.dir")).getPath());
			Properties properties = new Properties();
			properties.load(new FileInputStream("config"+File.separatorChar+"config.properties"));
			String brokerURI = properties.getProperty("brokerURI");
			factory = new ActiveMQConnectionFactory(brokerURI);
			((ActiveMQConnectionFactory)factory).setUseAsyncSend(true);
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(null);
		} catch (Exception e) {
			System.out.println(new File(System.getProperty("user.dir")).getPath());
			e.printStackTrace();
		}
	}*/
	
	
	public ClientPublisher(Session session,Destination replyDestination){
		try{
		this.replyDestination = replyDestination;
		this.session = session;
		producer = session.createProducer(null);
		
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

	public void sendMessage(Request request) throws JMSException {
		Destination destination = session.createQueue("Request");
		//ObjectMessage message = createMessage(session, request);
		ObjectMessage message = session.createObjectMessage(request);
		//Destination replyDestination = null;
		//if(request.getDataType().startsWith("PMU")){
			//replyDestination = session.createTopic("Response.DATA_CLEANING");
			//replyDestination = session.createTemporaryQueue();
			
		//}
	
		//else
		//	replyDestination = session.createTopic("Response.PJM");
		message.setJMSReplyTo(replyDestination);
		
		
		System.out.println("Sending: "+ ((ActiveMQObjectMessage) message).toString()+ " on destination: " + destination);
		//StatusLoggerFactory.getLogger(new File("client.log")).addLogEntry(Utilities.getEntryString(request.getRequestId(), System.currentTimeMillis(), "T1"));
		producer.send(destination, message);
		//StatusLoggerFactory.getLogger(new File("client.log")).addLogEntry(Utilities.getEntryString(request.getRequestId(), System.currentTimeMillis(), "T2"));
	}
	
	public void sendMessage(Request request, RESPONSE_FORMAT responseFormat) throws JMSException {
		Destination destination = session.createQueue("Request");
		ObjectMessage message = session.createObjectMessage(request);
		message.setJMSReplyTo(replyDestination);
		message.setStringProperty("RESPONSE_FORMAT", responseFormat.toString());
		System.out.println("Sending: "+ ((ActiveMQObjectMessage) message).toString()+ " on destination: " + destination);
		producer.send(destination, message);
	}
	
	protected void sendMessage(Data data) throws JMSException {
		
		Destination destination = session.createQueue("Request");
		ObjectMessage message = session.createObjectMessage(data);
		message.setObject(data);
		
		//Destination replyDestination = null;
		//if(data.getDataType().startsWith("PMU"))
		//	replyDestination = session.createTopic("Upload.DATA_CLEANING");
		//else
		//	replyDestination = session.createTopic("Upload.PJM");
		//message.setJMSReplyTo(replyDestination);
		message.setJMSReplyTo(replyDestination);
		
		System.out.println("Sending: "+ ((ActiveMQObjectMessage) message).toString()+ " on destination: " + destination);
		producer.send(destination, message);
	
	}

	protected ObjectMessage createMessage(Session session, Request request) throws JMSException {
		ObjectMessage message = session.createObjectMessage(request);
		message.setObject(request);
		return message;
	}

}
