package pnnl.goss.core;

import java.io.File;
import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;

import pnnl.goss.core.Request.RESPONSE_FORMAT;

public interface ClientPublishser {

	void close();
	
	void sendMessage(Serializable message, Destination replyDestination, RESPONSE_FORMAT responseFormat) throws JMSException;
	
	void publishTo(Destination destination, Serializable data) throws JMSException;
	
	void publishTo(Destination destination, String data) throws JMSException;
	
	void publishBlobMessage(Destination destination, File file) throws JMSException;
}
