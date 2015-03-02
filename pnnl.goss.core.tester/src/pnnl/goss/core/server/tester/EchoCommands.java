package pnnl.goss.core.server.tester;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.authc.UsernamePasswordToken;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.tester.requests.EchoBlacklistCheckRequest;
import pnnl.goss.core.server.tester.requests.EchoRequest;

@Component(properties={
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gt"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, value={"echo", "echoOpenwire", 
																	"echoBlacklist", "connect"})
}, provides=Object.class)
public class EchoCommands {

	@ServiceDependency
	private volatile RequestHandlerRegistry registry;
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	private Client client;
	
	public void connect(String uname, String pass) {
		if (client != null){
			client.close();
		}
		client = clientFactory.create(PROTOCOL.OPENWIRE);
		client.setCredentials(new UsernamePasswordCredentials(uname, pass));
		System.out.println("Setup to use connection: "+uname+":"+pass);
	}
	
	
	public void echo(String message) {
		Response response = null;
		try {
			response = registry.handle(new EchoRequest(message));
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
	
	public void echoBlacklist(String message){
		getClient();
		
		Response response = client.getResponse(new EchoBlacklistCheckRequest(message));
		
		
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
		
	}
	
	public void echoOpenwire(String message){
		
		getClient();
		
		Response response = client.getResponse(new EchoRequest(message));
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
	}

	private void getClient() {
		if (client == null){
			client = clientFactory.create(PROTOCOL.OPENWIRE);
			client.setCredentials(new UsernamePasswordCredentials("darkhelmet", "ludicrousspeed"));
		}
	}
	
	@Stop
	public void stop(){
		if (client != null){
			client.close();
		}
	}
}
