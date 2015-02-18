package pnnl.goss.core;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;

import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;

public interface ClientPublishser {

	void close();
	
	void sendMessage(Request request, Destination replyDestination, RESPONSE_FORMAT responseFormat) throws JMSException;
	
	void publishTo(Destination destination, Serializable data) throws JMSException;
	
	void publishTo(Destination destination, String data) throws JMSException;
}
