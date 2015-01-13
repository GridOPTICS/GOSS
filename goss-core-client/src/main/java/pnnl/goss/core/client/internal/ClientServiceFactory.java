package pnnl.goss.core.client.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import pnnl.goss.core.Client;

public class ClientServiceFactory implements ServiceFactory<Client>{

    @Override
    public Client getService(Bundle bundle,
            ServiceRegistration<Client> registration) {

        return new GossClient();
    }

    @Override
    public void ungetService(Bundle bundle,
            ServiceRegistration<Client> registration, Client service) {

        service.close();

    }

}
