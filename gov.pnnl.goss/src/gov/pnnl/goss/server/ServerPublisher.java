package gov.pnnl.goss.server;
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



import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import gov.pnnl.goss.client.GossDataError;
import gov.pnnl.goss.client.GossDataResponse;
import gov.pnnl.goss.client.api.Response;
import gov.pnnl.goss.client.api.ResponseFormat;

public class ServerPublisher {

	private final Session session;
		
	private static final Logger log = LoggerFactory.getLogger(ServerPublisher.class);

	public ServerPublisher(Session session) {
		this.session = session;
	}
	
	public void sendErrror(String errorString, Destination destination) throws JMSException{
		GossDataResponse errResp = new GossDataResponse(new GossDataError(errorString));
		errResp.setResponseComplete(true);
		sendResponse(errResp, destination);		
	}

	public void sendResponse(Response response, Destination destination)
			throws JMSException {

		ObjectMessage message = session.createObjectMessage(response);
		//System.out.println("Sending response for QueryId: " + response.getId() + " on destination: " + destination);
		log.debug("Sending response for QueryId: " + response.getId() + " on destination: " + destination);
		session.createProducer(destination).send(message); //producer.send(destination, message);

	}

	public void sendResponse(Response response, Destination destination,
			ResponseFormat responseFormat) throws JMSException {

		Message message = null;

		if (responseFormat == null)
			message = session.createObjectMessage(response);
		else if (responseFormat == ResponseFormat.XML) {
			XStream xStream = new XStream();
			String xml = xStream.toXML(((GossDataResponse) response).getData());
			message = session.createTextMessage(xml);
		}

		//System.out.println("Sending response for QueryId: " + response.getId() + " on destination: " + destination);
		log.debug("Sending response for QueryId: " + response.getId() + " on destination: " + destination);
		//producer.send(destination, message);
		session.createProducer(destination).send(message);

	}

	public void sendEvent(Response response, String destinationName)
			throws JMSException {
		Destination destination = session.createTopic(destinationName);
		ObjectMessage message = session.createObjectMessage(response);
		//System.out.println("Sending response for QueryId: on destination: "+ destination);
		log.debug("Sending response for QueryId: on destination: "+ destination);
		//producer.send(destination, message);
		session.createProducer(destination).send(message);
	}
	
	public void sendEvent(String message, String destinationName)
			throws JMSException {
		Destination destination = session.createTopic(destinationName);
		TextMessage response = session.createTextMessage(message);
		
		//System.out.println("Sending response for QueryId: on destination: "+ destination);
		//producer.send(destination, response);
		session.createProducer(destination).send(response);
	}

	public void close() {
		throw new UnsupportedOperationException("Not Implemented!");
//		try {
//			session.close();
//		} catch (JMSException e) {
//			e.printStackTrace();
//		}
	}

}
