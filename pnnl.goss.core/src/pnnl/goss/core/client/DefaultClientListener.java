package pnnl.goss.core.client;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.ClientListener;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Response;

public class DefaultClientListener implements ClientListener {
    private static Logger log = LoggerFactory.getLogger(DefaultClientListener.class);

    private GossResponseEvent responseEvent;

    public DefaultClientListener(GossResponseEvent event) {
    	log.debug("Instantiating");
        responseEvent = event;
    }

    public void onMessage(Message message) {

        try {
            if (message instanceof ObjectMessage) {
            	log.debug("message of type: "+message.getClass() + " received");
                ObjectMessage objectMessage = (ObjectMessage) message;
                if (objectMessage.getObject() instanceof pnnl.goss.core.Response) {
                    Response response = (Response) objectMessage.getObject();
                    responseEvent.onMessage(response);
                } else {
                    DataResponse response = new DataResponse(
                            objectMessage.getObject());
                    responseEvent.onMessage(response);
                }
            } else if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                DataResponse response = new DataResponse(textMessage.getText());
                responseEvent.onMessage(response);
            } 
            // TODO Look at implementing these?
            // Other possible types are
            // MapMessage	 -  A set of keyword/value pairs.
            // BytesMessage  -  A block of binary data, represented in Java as a byte array. 
            //					This format is often used to interface with an external messaging system that defines its own binary protocol for message formats.
            // StreamMessage -  A list of Java primitive values. This type can be used to represent certain data types used by existing messaging systems.

        } catch (Exception e) {
        	log.error("ERROR Receiving message", e);
            e.printStackTrace();
        }
    }
}
