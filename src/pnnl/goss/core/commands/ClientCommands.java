package pnnl.goss.core.commands;

import java.util.Iterator;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.service.command.CommandProcessor;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;

@Component(properties={
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gc"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, 
			value= {"makeOpenwire", "makeStomp", "list"})},
		provides=Object.class)
public class ClientCommands {
	
	@ServiceDependency
	private volatile ClientFactory factory;
		
	public void makeOpenwire(){
		System.out.println("Making openwire client");
		Client client = factory.create(PROTOCOL.OPENWIRE);
		System.out.println("Client is null? "+ (client == null));
		client.close();
	}
	
	public void makeStomp(){
		System.out.println("Making stomp client");
		Client client = factory.create(PROTOCOL.STOMP);
		System.out.println("Client is null? "+ (client == null));
		client.close();
	}
	
	public void list(){
		Map<String, PROTOCOL> clientMap = factory.list();
		for(Iterator<String> it=clientMap.keySet().iterator(); it.hasNext();){
			String key = it.next();
			System.out.println("ClientId: "+ key+"; protocol: "+ clientMap.get(key).toString());			
		}
	}

}
