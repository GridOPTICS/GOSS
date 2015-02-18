package pnnl.goss.core.server.tester;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.service.command.CommandProcessor;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.tester.requests.EchoRequest;

@Component(properties={
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gt"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, value={"echo"})
}, provides=Object.class)
public class EchoCommands {

	@ServiceDependency
	private volatile RequestHandlerRegistry registry;
	
	public void echo(String message) {
		EchoRequest request = new EchoRequest(message);
		Response response = registry.handle(request);
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
	}
}
