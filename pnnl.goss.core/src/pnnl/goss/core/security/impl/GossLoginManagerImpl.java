package pnnl.goss.core.security.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.Session;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.northconcepts.exception.SystemException;

import pnnl.goss.core.Client;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.security.GossLoginManager;
import pnnl.goss.core.security.GossSecurityManager;
import pnnl.goss.core.security.SecurityConfig;
import pnnl.goss.core.security.jwt.UserRepository;

@Component
public class GossLoginManagerImpl implements GossLoginManager {
	
	private static final String CONFIG_PID = "pnnl.goss.core.security.userfile";
	private static final Logger log = LoggerFactory.getLogger(GossLoginManagerImpl.class);
	public static final String DEFAULT_SYSTEM_USER = "system";
	
	private final Map<String, String> userMap = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
	private final List<String> tokens = new ArrayList<>();
	
	
	// Depend on this so that the security manager service is loaded before
	// this package.
	@ServiceDependency
	private volatile SecurityManager securityManager;
	@ServiceDependency
	private volatile SecurityConfig securityConfig;
	
	@ServiceDependency
	private volatile UserRepository userRepository;
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	String stompUri = "stomp://0.0.0.0:61613";
	
	@Start
	public void start(){
		System.out.println("STARTING GOSS LOGIN MGR");
		try {
//			Client client = clientFactory.create(PROTOCOL.STOMP,
//					new UsernamePasswordCredentials(securityConfig.getManagerUser(), securityConfig.getManagerPassword()));
			Credentials credentials = new UsernamePasswordCredentials(securityConfig.getManagerUser(), securityConfig.getManagerPassword());
			StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
			factory.setBrokerURI(stompUri.replace("stomp", "tcp"));
			Connection pwConnection = null;
			if (credentials != null) {
				pwConnection = factory.createConnection(credentials
						.getUserPrincipal().getName(), credentials
						.getPassword());
			} else {
				pwConnection = factory.createConnection();
			}
			
			System.out.println("CONN "+pwConnection);
			Session pwSession = pwConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			String loginTopic = "/topic/"+GossSecurityManager.PROP_GOSS_LOGIN_TOPIC.replaceAll("\\.", "/");
			System.out.println("SUBSCRIBING TO LOGIN TOPIC "+loginTopic);
			client.subscribe(loginTopic,  new ResponseEvent(client));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Stop
	public void stop(){
		System.out.println("Stopping ManagementLauncher");
	}
	
	
	@Override
	public boolean login(String username, byte[] password) {
		System.out.println("CALLING LOGIN:"+username);
		if(userMap.containsKey(username)){
			return userMap.get(username).equals(new String(password));
		}
		return false;
	}


	@Override
	public String getToken(String username, byte[] password) {
		System.out.println("CALLING GET TOKEN:"+username);

		if(login(username, password)){
			String token = userRepository.createToken(username);
			tokens.add(token);
			return token;
		}
		return null;
	}
	
	@Override
	public boolean tokenLogin(String token) {
		// TODO Auto-generated method stub
		System.out.println("CALLING TOKEN LOGIN:"+token);

		return false;
	}
    
    @ConfigurationDependency(pid=CONFIG_PID)
	public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
System.out.println("UPDATING PROPERTIES FOR GOSS LOGIN MANAGER");
		if (properties != null){
			log.debug("Updating PropertyBasedRealm");
			userMap.clear();
			userPermissions.clear();
			
			Enumeration<String> keys = properties.keys();
			Set<String> perms = new HashSet<>();
			while(keys.hasMoreElements()){
				String k = keys.nextElement();
				String v = (String)properties.get(k);
				System.out.println("V "+v);
				String[] credAndPermissions = v.split(",");
//				String[] userPW = credAndPermissions[0].split("=");
				SimpleAccount acnt = new SimpleAccount(k, credAndPermissions[0], "gridappsd" );
				for(int i =1; i<credAndPermissions.length; i++){
					acnt.addStringPermission(credAndPermissions[i]);
					perms.add(credAndPermissions[i]);
				}
//				userMap.put(k, acnt);
//				userMap.put(userPW[0], userPW[1]);
				userMap.put(k, credAndPermissions[0]);
				userPermissions.put(k, perms);
				
			}
		}		
	}


    class ResponseEvent implements GossResponseEvent{
		private final Client client;
		private Gson gson = new Gson();

		public ResponseEvent(Client client){
			this.client = client;
		}

		@Override
		public void onMessage(Serializable response) {
			String responseData = "{}";
			if (response instanceof DataResponse){
				String request = (String)((DataResponse) response).getData();
//				if (request.trim().equals("list_handlers")){
//					//responseData = "Listing handlers here!";
//					responseData = gson.toJson(handlerRegistry.list());
//				}
//				else if (request.trim().equals("list_datasources")){
//					//responseData = "Listing Datasources here!";
//					responseData = gson.toJson(datasourceRegistry.getAvailable());
//				}
			}


			System.out.println("On message: "+response.toString());
			client.publish("goss/management/response", responseData);
		}

	}







	
}
