package gov.pnnl.goss.client.api;


import javax.jms.MessageConsumer;

public interface ClientConsumer {

	public void close();

	public MessageConsumer getMessageConsumer();	
	
}