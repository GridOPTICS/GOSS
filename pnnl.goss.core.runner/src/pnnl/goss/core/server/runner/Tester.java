package pnnl.goss.core.server.runner;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client;
import pnnl.goss.core.client.ClientServiceFactory;
//import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.security.SecurityConfig;

@Component
public class Tester {

	@ServiceDependency
	private volatile ClientFactory clientFactory;
	@ServiceDependency
	private volatile SecurityConfig securityConfig;

	
	public static void main(String[] args) {
		
		new Tester();
	}
	
	
	public Tester(){
		System.out.println("TESTER STARTING UP "+clientFactory);
				Client client;
				try {
					String systemUser = "system";
					String systemPW = "manager";
//					ClientFactory cf= new ClientServiceFactory();
					Dictionary<String, Object> properties = new Hashtable<String, Object>();
					properties.put("goss.ssl.uri", "ssl://localhost:61611");
					properties.put("goss.start.broker", "true");
					properties.put("server.keystore", "resources/keystores/mybroker.ks");
					properties.put("server.keystore.password", "GossServerTemp");
					properties.put("server.truststore", "");
					properties.put("server.truststore.password", "");
					properties.put("client.truststore", "resources/keystores/myclient.ts");
					properties.put("client.truststore.password", "GossClientTrust");
					properties.put("client.keystore", "resources/keystores/myclient.ks");
					properties.put("client.keystore.password", "GossClientTemp");
					properties.put("ssl.enabled", "true");
					
//
//					cf.updated(properties);
//					client = cf.create(PROTOCOL.STOMP,
//							new UsernamePasswordCredentials(systemUser, systemPW));
//
//					client.subscribe("/topic/goss/management/request", new ResponseEvent(client));
//					client.publish("test", "testing");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
	@Start
	public void start(){
		System.out.println("TESTER STARTING UP "+clientFactory);

	}

	
	
}
