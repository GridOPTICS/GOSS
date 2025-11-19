package pnnl.goss.core;

import jakarta.jms.MessageConsumer;

public interface ClientConsumer {

    public void close();

    public MessageConsumer getMessageConsumer();

}
