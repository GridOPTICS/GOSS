package pnnl.goss.core.server.impl;

import java.util.Optional;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.service.command.CommandProcessor;

import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.tester.requests.EchoRequest;

@Component(properties = {
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gs"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, value={"list", "echo", "getEchoHandler"})},
		provides=Object.class
)
public class Commands {
	
	@ServiceDependency
	private volatile RequestHandlerRegistry registry;
	
	public void list(){
		for(RequestHandler rh: registry.list()){
			for(String r: rh.getHandles()){
				System.out.println(rh.getClass().getName() + " handles: " + r);
			}
			
		}
	}
	
	public void echo(String message) {
		EchoRequest request = new EchoRequest(message);
		registry.handle(request);
	}
	
	public void getEchoHandler() {
		Optional<RequestHandler> handler = registry.getHandler(EchoRequest.class);
		System.out.println("handler is null: "+ handler.isPresent());
		handler.ifPresent(p-> {
			System.out.println("Found handler: " + p.getClass().getName());	
		});
		
	}

}
