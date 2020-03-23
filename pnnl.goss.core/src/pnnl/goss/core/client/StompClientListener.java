package pnnl.goss.core.client;

import javax.jms.Message;

import org.fusesource.stomp.jms.message.StompJmsBytesMessage;
import org.fusesource.stomp.jms.message.StompJmsTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.ClientListener;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.security.SecurityConstants;

public class StompClientListener implements ClientListener {
	private static Logger log = LoggerFactory
			.getLogger(StompClientListener.class);

	private GossResponseEvent responseEvent;
	private DataResponse dataResponse = new DataResponse();

	public StompClientListener(GossResponseEvent event) {
		log.debug("Instantiating");
		responseEvent = event;
	}

	public void onMessage(Message msg) {

		String buffer;
		String message = "";
		StompJmsBytesMessage stompByteMessage;
		StompJmsTextMessage stompTextMessage;

		try {
			if (msg instanceof DataResponse) {
				dataResponse = DataResponse.parse(msg.toString());
			}
			if (msg instanceof StompJmsBytesMessage) {
				log.debug("Received StompJmsBytesMessage on destination: "+ msg.getJMSDestination().toString());

				stompByteMessage = (StompJmsBytesMessage) msg;
				buffer = stompByteMessage.getContent().toString();
				message = buffer.substring(buffer.indexOf(":") + 1);
				dataResponse.setData(message);
				dataResponse.setDestination(msg.getJMSDestination().toString());
				if (msg.getJMSReplyTo() != null)
					dataResponse.setReplyDestination(msg.getJMSReplyTo());

			} else if (msg instanceof StompJmsTextMessage) {

				stompTextMessage = (StompJmsTextMessage) msg;
				buffer = stompTextMessage.getText();//.getContent().toString();
				message = buffer.substring(buffer.indexOf(":") + 1);
				dataResponse.setData(message);
				dataResponse.setDestination(stompTextMessage
						.getStompJmsDestination().toString());
				if (msg.getJMSReplyTo() != null)
					dataResponse.setReplyDestination(msg.getJMSReplyTo());

			}

			if (msg.getBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER)) {
				String username = msg
						.getStringProperty(SecurityConstants.SUBJECT_HEADER);
				dataResponse.setUsername(username);
			} else {
				log.warn("No username received in stomp message"+ message);
			}
			responseEvent.onMessage(dataResponse);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
