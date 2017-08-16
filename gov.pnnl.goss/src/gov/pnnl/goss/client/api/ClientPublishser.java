package gov.pnnl.goss.client.api;


import java.io.File;
import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;

import gov.pnnl.goss.client.api.ResponseFormat;

public interface ClientPublishser {

	void close();
	
	void sendMessage(Serializable message, Destination destination, Destination replyDestination, ResponseFormat responseFormat) throws JMSException;
	
	void publish(Destination destination, Serializable data) throws JMSException;
	
	void publishBlobMessage(Destination destination, File file) throws JMSException;
}
