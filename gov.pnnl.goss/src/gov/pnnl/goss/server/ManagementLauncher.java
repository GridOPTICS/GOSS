package gov.pnnl.goss.server;


import java.io.Serializable;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.google.gson.Gson;

import gov.pnnl.goss.client.GossDataResponse;
import gov.pnnl.goss.client.api.Client;
import gov.pnnl.goss.client.api.ClientFactory;
import gov.pnnl.goss.client.api.GossProtocol;
import gov.pnnl.goss.client.api.GossResponseEvent;
import gov.pnnl.goss.server.api.DataSourceRegistry;
import gov.pnnl.goss.server.api.RequestHandlerRegistry;
import gov.pnnl.goss.server.api.ServerControl;

@Component
public class ManagementLauncher {

	@ServiceDependency
	private volatile ClientFactory clientFactory;

	@ServiceDependency
	private volatile ServerControl serverControl;

	@ServiceDependency
	private volatile RequestHandlerRegistry handlerRegistry;

	@ServiceDependency
	private volatile DataSourceRegistry datasourceRegistry;

	class ResponseEvent implements GossResponseEvent{
		private final Client client;
		private Gson gson = new Gson();

		public ResponseEvent(Client client){
			this.client = client;
		}

		@Override
		public void onMessage(Serializable response) {
			String responseData = "{}";
			if (response instanceof GossDataResponse){
				String request = (String)((GossDataResponse) response).getData();
				if (request.trim().equals("list_handlers")){
					//responseData = "Listing handlers here!";
					responseData = gson.toJson(handlerRegistry.list());
				}
				else if (request.trim().equals("list_datasources")){
					//responseData = "Listing Datasources here!";
					responseData = gson.toJson(datasourceRegistry.getAvailable());
				}
			}


			System.out.println("On message: "+response.toString());
			client.publish("goss/management/response", responseData);
		}

	}

	@Start
	public void start(){
		try {
			Client client = clientFactory.create(GossProtocol.STOMP,
					new UsernamePasswordCredentials("system", "manager"));
			client.subscribe("/topic/goss/management/request", new ResponseEvent(client));
			client.subscribe("/topic/goss/management/go", new ResponseEvent(client));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Stop
	public void stop(){
		System.out.println("Stopping ManagementLauncher");
	}
}
