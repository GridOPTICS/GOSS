package pnnl.goss.core.client.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;

public class ClientServiceFactory implements ClientFactory{

    public static final String CONFIG_PID = "pnnl.goss.core.client";

    List<GossClient> clientInstances = new ArrayList<>();
    Dictionary<String, Object> properties = new Hashtable<String, Object>();//    // Default to openwire.

    public void updated(Dictionary<String, ?> properties)
            throws ConfigurationException {

        if(properties != null){
            synchronized (this.properties) {
                Enumeration<String> keyEnum = properties.keys();
                while(keyEnum.hasMoreElements()){
                    String k = keyEnum.nextElement();
                    this.properties.put(k, properties.get(k));
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
            GossClient client = clientInstances.remove(0);
            client.reset();
            client = null;
        }
    }
}
