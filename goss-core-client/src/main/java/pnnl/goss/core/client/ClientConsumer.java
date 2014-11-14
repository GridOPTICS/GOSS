package pnnl.goss.core.client;

import javax.jms.MessageConsumer;

public interface ClientConsumer {

	public void close();

	public MessageConsumer getMessageConsumer();	
	
}