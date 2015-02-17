package pnnl.goss.core.client.activemq;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.Start;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.GossCoreContants;

@Component(provides={ClientFactory.class})
public class ClientServiceFactory implements ClientFactory {

    protected static final String CONFIG_PID = "pnnl.goss.core.client";

    private volatile List<Client> clientInstances = new ArrayList<>();
    private volatile Dictionary<String, Object> properties = new Hashtable<String, Object>();//    // Default to openwire.
    
    @Start
    public void start(){
    	System.out.println("Starting client service factory.");
    }

    @ConfigurationDependency(pid=CONFIG_PID)
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {

        synchronized (this.properties) {
            Enumeration<String> keyEnum = properties.keys();
            while(keyEnum.hasMoreElements()){
                String k = keyEnum.nextElement();
                this.properties.put(k, properties.get(k));
            }
        }
        
        String value = (String) this.properties.get(GossCoreContants.PROP_OPENWIRE_URI); 
        
        if ( value == null || value.trim().isEmpty()){
        	throw new ConfigurationException(GossCoreContants.PROP_OPENWIRE_URI, "Not found in configuration file: " + CONFIG_PID);
        }
        
        value = (String) this.properties.get(GossCoreContants.PROP_STOMP_URI);
        if ( value == null || value.trim().isEmpty()){
        	throw new ConfigurationException(GossCoreContants.PROP_STOMP_URI, "Not found in configuration file: " + CONFIG_PID);
        }
    }

    @Override
    public synchronized Client create(PROTOCOL protocol) {
        GossClient client = null;
        for(Client c: clientInstances){
        	
            if(!((GossClient)c).isUsed() && c.getProtocol().equals(protocol)){
                client = (GossClient)c;
                client.setUsed(true);
                break;
            }
        }

        if(client == null){
            client = new GossClient(protocol);
            client.setConfiguration(properties);
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
		for(Client c: clientInstances){
			map.put(c.getClientId(), c.getProtocol());
		}
		return map;
	}
}
