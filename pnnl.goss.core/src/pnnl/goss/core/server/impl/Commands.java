package pnnl.goss.core.server.impl;

import java.util.Optional;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.service.command.CommandProcessor;

import pnnl.goss.core.server.AuthorizationHandler;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.RequestHandlerInterface;
import pnnl.goss.core.server.RequestHandlerRegistry;
//import pnnl.goss.core.server.tester.requests.EchoRequest;
import pnnl.goss.core.server.RequestUploadHandler;

@Component(properties = {
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gs"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, value={"list"})}, //, "echo", "getEchoHandler"})},
		provides=Object.class
)
public class Commands {
	
	@ServiceDependency
	private volatile RequestHandlerRegistry registry;
	
	public void list(){
		for(RequestHandlerInterface rh: registry.list()){
			if (rh instanceof RequestHandler){
				RequestHandler handler = (RequestHandler) rh;
				handler.getHandles().forEach((k, v)->{
					System.out.println("RequestHandler: "+handler.getClass().getName() + " handles: " + k + " authorized by:" + v);
				});
			}
			else if (rh instanceof RequestUploadHandler) {
				RequestUploadHandler handler = (RequestUploadHandler) rh;
				handler.getHandlerDataTypes().forEach((k, v)->{
					System.out.println("RequestUploadHandler: "+handler.getClass().getName() + " handles data: " + k + " authorized by:" + v);
				});
			}
			else if (rh instanceof AuthorizationHandler) {
				AuthorizationHandler handler = (AuthorizationHandler) rh;
				System.out.println("AuthorizationHandler registered: " + handler.getClass().getName());
			}
			
		}
	}
	
//	public void echo(String message) {
//		EchoRequest request = new EchoRequest(message);
//		registry.handle(request);
//	}
//	
//	public void getEchoHandler() {
//		Optional<RequestHandler> handler = registry.getHandler(EchoRequest.class);
//		System.out.println("handler is null: "+ handler.isPresent());
//		handler.ifPresent(p-> {
//			System.out.println("Found handler: " + p.getClass().getName());	
//		});
//		
//	}

}
