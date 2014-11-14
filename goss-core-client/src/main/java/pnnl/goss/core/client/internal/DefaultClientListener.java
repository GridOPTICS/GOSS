package pnnl.goss.core.client.internal;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.GossResponseEvent;

public class DefaultClientListener {
	GossResponseEvent responseEvent;
	
	public DefaultClientListener(GossResponseEvent event){
		responseEvent = event;
	}
	
	public void onMessage(Message message) {
		
		try {
			if(message instanceof ObjectMessage){
				ObjectMessage objectMessage = (ObjectMessage) message;
					if(objectMessage.getObject() instanceof pnnl.goss.core.Response){
						Response response = (Response) objectMessage.getObject();
						responseEvent.onMessage(response);
					}
					else{
						DataResponse response = new DataResponse(objectMessage.getObject());
						responseEvent.onMessage(response);
					}
			}
			else if(message instanceof TextMessage){
				TextMessage textMessage = (TextMessage)message;
				DataResponse response = new DataResponse(textMessage.getText());
				responseEvent.onMessage(response);
			}
			
			//Log file:client.log, Description: After getting PMUData from server, Timestamp:T8
			//StatusLoggerFactory.getLogger(new File("client.log")).addLogEntry(getEntryString(pmuData.getRequestId(),System.currentTimeMillis(), "T8"));
			//StatusLoggerFactory.getLogger(new File("client.log")).addLogEntry(getEntryString(System.currentTimeMillis(), "T8"));
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
