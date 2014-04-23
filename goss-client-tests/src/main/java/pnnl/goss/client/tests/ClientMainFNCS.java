package pnnl.goss.client.tests;

import pnnl.goss.client.tests.util.ClientAuthHelper;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossClient.PROTOCOL;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.fncs.common.datamodel.SimEvent;
import pnnl.goss.fncs.common.datamodel.SteerEvent;

import com.google.gson.Gson;

public class ClientMainFNCS {

	public static void main(String[] args){
		try{
			
			//Publish SimEvent
			byte[] byteArray = new byte[10];
			SimEvent simEvent = new SimEvent("testName", byteArray);
			GossClient client = new GossClient(ClientAuthHelper.getGCACredentials(),PROTOCOL.STOMP);
			client.publish("SimEvent", simEvent, RESPONSE_FORMAT.JSON);
			
			//Subcribe to SteerEvent
			GossResponseEvent event = new GossResponseEvent() {
				@Override
				public void onMessage(Response response) {
					String message = (String)((DataResponse)response).getData(); 
					System.out.println(message);
					Gson gson = new Gson();
					SteerEvent steerEvent = gson.fromJson(message, SteerEvent.class);
					System.out.println(steerEvent.getContents());
				}
			};
			client.subscribeTo("/topic/SteerEvent", event);
			
			//client.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


}
