package pnnl.goss.core.client.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import pnnl.goss.core.Client;

public class ClientServiceFactory implements ServiceFactory<Client>{

    Properties properties = null;

    @Override
    public Client getService(Bundle bundle,
            ServiceRegistration<Client> registration) {

        Client client = new GossClient(properties);

        return client;
    }

    @Override
    public void ungetService(Bundle bundle,
            ServiceRegistration<Client> registration, Client service) {

        service.close();

    }

    public void updateConfiguration(Dictionary<String, ?> props){
        properties = new Properties();

        Enumeration<String> keys = props.keys();

        while(keys.hasMoreElements()){
            String k=keys.nextElement();
            properties.setProperty(k, (String)props.get(k));
        }
    }
}
