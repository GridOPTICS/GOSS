package pnnl.goss.core;

import javax.jms.MessageConsumer;

public interface ClientConsumer {

	public void close();

	public MessageConsumer getMessageConsumer();	
	
}