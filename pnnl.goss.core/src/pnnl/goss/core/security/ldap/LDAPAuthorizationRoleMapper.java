package pnnl.goss.core.security.ldap;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.security.AuthorizationRoleMapper;

@Component()
public class LDAPAuthorizationRoleMapper implements AuthorizationRoleMapper {
	private static final String CONFIG_PID = "pnnl.goss.core.security.ldap";
	
	
	@ConfigurationDependency(pid=CONFIG_PID)
	public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
    	
    	if (properties != null) {
    		//TODO
    		//serverurl
    		//username
    		//pw
    		//pattern
    		//....
    		
//    		shouldStartBroker = Boolean.parseBoolean(Optional
//			.ofNullable((String) properties.get(PROP_START_BROKER))
//			.orElse("true"));
//	
//	connectionUri = Optional
//			.ofNullable((String)properties.get(PROP_CONNECTIOn_URI))
//			.orElse("tcp://localhost:61616");
//	
//	openwireTransport = Optional
//			.ofNullable((String) properties.get(PROP_OPENWIRE_TRANSPORT))
//			.orElse("tcp://localhost:61616");
//	
//	stompTransport = Optional
//			.ofNullable((String) properties.get(PROP_STOMP_TRANSPORT))
//			.orElse("tcp://localhost:61613");
//	
//	requestQueue = Optional
//			.ofNullable((String) properties.get(GossCoreContants.PROP_REQUEST_QUEUE))
//			.orElse("Request");	
    		
    	}
    		    	

	    	
	    	//start();
    	}
	
	
	//TODO probably move to it's own project
	volatile Map<String, List<String>> roleMapping = new ConcurrentHashMap<String, List<String>>();
	@Override
	public List<String> getRolesForUser(String identifier) {
		// TODO stuff
		
		
		return new ArrayList<String>();
	}

}
