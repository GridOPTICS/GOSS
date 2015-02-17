package pnnl.goss.core.client.internal;

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
        responseEvent = event;
    }

    public void onMessage(Message message) {

        try {
            if (message instanceof ObjectMessage) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
