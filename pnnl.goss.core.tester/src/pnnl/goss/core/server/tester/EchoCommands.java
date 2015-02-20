package pnnl.goss.core.server.tester;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.service.command.CommandProcessor;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.tester.requests.EchoRequest;

@Component(properties={
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gt"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, value={"echo", "echoOpenwire"})
}, provides=Object.class)
public class EchoCommands {

	@ServiceDependency
	private volatile RequestHandlerRegistry registry;
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	private EchoRequest buildRequest(String message){
		return new EchoRequest(message);
	}
	
	public void echo(String message) {
		Response response = null;
		try {
			response = registry.handle(buildRequest(message));
		} catch (HandlerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
	}
	
	public void echoOpenwire(String message){
		
		Client client = clientFactory.create(PROTOCOL.OPENWIRE);
		
		Response response = client.getResponse(buildRequest(message));
		
		
	}
}
