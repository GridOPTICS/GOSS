package pnnl.goss.core.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.http.auth.Credentials;

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
    	System.out.println("Updating configuration properties");
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
	        		throw new ConfigurationException(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE + " Wasn't set");
	        	}
	        	if (!exists(trustPassword)){
	        		throw new ConfigurationException(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD + " Wasn't set");
	        	}
	        	if (!exists(uri)){
	        		throw new ConfigurationException(GossCoreContants.PROP_SSL_URI + " Wasn't set");
	        	}
	        	
	        	
	        	this.properties.put(DEFAULT_OPENWIRE_URI, uri);
	        	this.properties.put(DEFAULT_STOMP_URI, uri);
	        }
	        else{
	        
		        String value = (String) this.properties.get(GossCoreContants.PROP_OPENWIRE_URI); 
		        
		        if (!exists(value)){
		        	throw new ConfigurationException(GossCoreContants.PROP_OPENWIRE_URI + " Not found in configuration file: " + CONFIG_PID);
		        }
		        
		        value = (String) this.properties.get(GossCoreContants.PROP_STOMP_URI);
		        if (!exists(value)){
		        	throw new ConfigurationException(GossCoreContants.PROP_STOMP_URI + " Not found in configuration file: " + CONFIG_PID);
		        }
	        }

    	}
    }

    @Override
    public synchronized Client create(PROTOCOL protocol, Credentials credentials) throws Exception {
    	
    	Properties configProperties = new Properties();
		try {
			if(this.properties.isEmpty()){
				System.out.println("Reading configuration properties");
				configProperties.load(new FileInputStream("conf"+File.separatorChar+"pnnl.goss.core.client.cfg"));
				Dictionary<String, String> dictionary = new Hashtable<String, String>();
				dictionary.put(GossCoreContants.PROP_OPENWIRE_URI, configProperties.getProperty("goss.openwire.uri"));
				dictionary.put(GossCoreContants.PROP_STOMP_URI, configProperties.getProperty("goss.stomp.uri"));
				this.updated(dictionary);
			}
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
    	
    	GossClient client = null;
        for(GossClient c: clientInstances){
        	
            if(!c.isUsed() && c.getProtocol().equals(protocol)){
                client = c;
                client.setUsed(true);
                break;
            }
        }

        if(client == null){
        	
        	String openwireUri = (String)properties.get(ClientFactory.DEFAULT_OPENWIRE_URI);
    		String stompUri = (String)properties.get(ClientFactory.DEFAULT_STOMP_URI);
    		
    		if (sslEnabled){
        		protocol = PROTOCOL.SSL;
        		String trustStorePassword = (String)properties.get(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD);
        		String trustStore = (String)properties.get(GossCoreContants.PROP_SSL_CLIENT_TRUSTSTORE);
        		
        		client = new GossClient(protocol, credentials, openwireUri, stompUri, trustStorePassword, trustStore);
        		
        	}
        	else{
        		client = new GossClient(protocol, credentials, openwireUri, stompUri);
	            
	        }
        	
        	client.setUsed(true);
        	client.createSession();
            clientInstances.add(client);
        }

        return client;
    }

    @Override
    public Client get(String uuid) {
    	Client client = null;
    	
    	for(int i=0; i<clientInstances.size(); i++){
    		GossClient c =  clientInstances.get(i);
    		if (c.getClientId().equals(uuid)){
    			client = c;
    			break;
    		}
    	}    		
    	
        return client;
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
