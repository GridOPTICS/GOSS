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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import pnnl.goss.core.Data;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;

public class ClientPublisher {

	private transient Session session;
	private transient MessageProducer producer;
	private transient MessageProducer publishingProducer;
	Destination destination;

	public ClientPublisher(Session session){
		try{
			this.session = session;
			destination = this.session.createQueue("Request");
			producer = this.session.createProducer(destination);
			publishingProducer = this.session.createProducer(null);
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

	public void sendMessage(Request request, Destination replyDestination, RESPONSE_FORMAT responseFormat) throws JMSException {
		ObjectMessage message = session.createObjectMessage(request);
		message.setJMSReplyTo(replyDestination);
		if(responseFormat!=null)
			message.setStringProperty("RESPONSE_FORMAT", responseFormat.toString());
		System.out.println("Sending: "+ request.getClass()+ " on destination: " + destination);
		producer.send(destination, message);
	}
	
	public void publishTo(Destination destination, Data data) throws JMSException {
		ObjectMessage message = session.createObjectMessage(data);
		System.out.println("Publishing: "+ data.getClass()+ " on destination: " + destination);
		publishingProducer.send(destination, message);
	}
	
	public void publishTo(Destination destination, String data) throws JMSException {
		TextMessage message = session.createTextMessage(data);
		System.out.println("Publishing on destination: " + destination);
		publishingProducer.send(destination, message);
	}
	
}
