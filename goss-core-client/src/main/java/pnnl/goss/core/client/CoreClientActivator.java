package pnnl.goss.core.client;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Client;
import pnnl.goss.core.client.internal.ClientServiceFactory;

public class CoreClientActivator implements BundleActivator {

    private static Logger log = LoggerFactory.getLogger(CoreClientActivator.class);
    ServiceRegistration<?> clientRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, Object> properties = new Hashtable<>();
        log.debug("Starting Bundle");
        clientRegistration = context.registerService(Client.class.getName(),
                new ClientServiceFactory(), properties);


    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug("Stopping Bundle");
        clientRegistration.unregister();
    }

}
