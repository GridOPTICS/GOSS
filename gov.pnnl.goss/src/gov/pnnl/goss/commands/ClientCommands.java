package gov.pnnl.goss.commands;

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
		try{
			System.out.println("Making openwire client");
			Client client = factory.create(PROTOCOL.OPENWIRE, null);
			System.out.println("Client is null? "+ (client == null));
			client.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void makeStomp(){
		try{
			System.out.println("Making stomp client");
			Client client = factory.create(PROTOCOL.STOMP, null);
			System.out.println("Client is null? "+ (client == null));
			client.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void list(){
		Map<String, PROTOCOL> clientMap = factory.list();
		for(Iterator<String> it=clientMap.keySet().iterator(); it.hasNext();){
			String key = it.next();
			System.out.println("ClientId: "+ key+"; protocol: "+ clientMap.get(key).toString());			
		}
	}

}
