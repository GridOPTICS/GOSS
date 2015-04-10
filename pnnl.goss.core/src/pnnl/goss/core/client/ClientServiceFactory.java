package pnnl.goss.core.client;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.osgi.service.cm.ConfigurationException;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.GossCoreContants;

@Component(provides={ClientFactory.class})
public class ClientServiceFactory implements ClientFactory {

    private volatile List<GossClient> clientInstances = new ArrayList<>();
    private volatile Dictionary<String, Object> properties = new Hashtable<String, Object>();
    private boolean sslEnabled = false;
    
    public void setOpenwireUri(String brokerToConnectTo){
    	this.properties.put(GossCoreContants.PROP_OPENWIRE_URI, brokerToConnectTo);
    }
    
    boolean exists(String value){
    	return !(value == null || value.isEmpty());
    }
    
    @ConfigurationDependency(pid=CONFIG_PID)
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {

    	if (properties != null) {
	        synchronized (this.properties) {
	            Enumeration<String> keyEnum = properties.keys();
	            while(keyEnum.hasMoreElements()){
	                String k = keyEnum.nextElement();
	                this.properties.put(k, properties.get(k));
	            }
	        }
	        
	        sslEnabled = Boolean.parseBoolean((String)this.properties.get(GossCoreContants.PROP_SSL_ENABLED));
	        
	        if (sslEnabled){
	        	String uri = (String)this.properties.get(GossCoreContants.PROP_SSL_URI);
	        	String trustStore = (String)this.properties.get(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE);
	        	String trustPassword = (String)this.properties.get(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD);

	        	if (!exists(trustStore)){
	        		throw new ConfigurationException(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE, "Wasn't set");
	        	}
	        	if (!exists(trustPassword)){
	        		throw new ConfigurationException(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD, "Wasn't set");
	        	}
	        	if (!exists(uri)){
	        		throw new ConfigurationException(GossCoreContants.PROP_SSL_URI, "Wasn't set");
	        	}
	        	
	        	
	        	this.properties.put(DEFAULT_BROKER_URI, uri);
	        }
	        else{
	        
		        String value = (String) this.properties.get(GossCoreContants.PROP_OPENWIRE_URI); 
		        
		        if (!exists(value)){
		        	throw new ConfigurationException(GossCoreContants.PROP_OPENWIRE_URI, "Not found in configuration file: " + CONFIG_PID);
		        }
		        
		        value = (String) this.properties.get(GossCoreContants.PROP_STOMP_URI);
		        if (!exists(value)){
		        	throw new ConfigurationException(GossCoreContants.PROP_STOMP_URI, "Not found in configuration file: " + CONFIG_PID);
		        }
	        }

    	}
    }

    @Override
    public synchronized Client create(PROTOCOL protocol) {
        GossClient client = null;
        for(GossClient c: clientInstances){
        	
            if(!c.isUsed() && c.getProtocol().equals(protocol)){
                client = c;
                client.setUsed(true);
                break;
            }
        }

        if(client == null){
        	if (sslEnabled){
        		client = new GossClient()
        					.setProtocol(PROTOCOL.SSL)
        					.setUri((String)properties.get(ClientFactory.DEFAULT_BROKER_URI))
        					.setClientTrustStorePassword((String)properties.get(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD))
        					.setClientTrustStore((String)properties.get(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE));        					
        	}
        	else{
	            client = new GossClient()
	            				.setProtocol(protocol)
	            				.setUri((String)properties.get(GossCoreContants.PROP_OPENWIRE_URI));
        	}
            clientInstances.add(client);
        }

        return client;
    }

    @Override
    public Client get(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized void destroy() {
        while (clientInstances.size() > 0){
            GossClient client = (GossClient) clientInstances.remove(0);
            client.reset();
            client = null;
        }
    }

	@Override
	public Map<String, PROTOCOL> list() {
		Map<String, PROTOCOL> map = new HashMap<>();
		for(GossClient c: clientInstances){
			map.put(c.getClientId(), c.getProtocol());
		}
		return map;
	}
}
