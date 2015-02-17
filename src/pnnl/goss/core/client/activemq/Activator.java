package pnnl.goss.core.client;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.client.internal.ClientServiceFactory;

public class CoreClientActivator implements BundleActivator, ManagedService {
    private  static final String CONFIG_PID = "pnnl.goss.core.client";
    private static Logger log = LoggerFactory.getLogger(CoreClientActivator.class);


    private ServiceRegistration<?> registration;
    private ClientServiceFactory clientFactory;


    @Override
    public void start(BundleContext context) throws Exception {
        log.debug("Starting");
        clientFactory = new ClientServiceFactory();
        Dictionary<String, Object> properties = new Hashtable<>();

        properties.put(Constants.SERVICE_PID, CONFIG_PID);
        context.registerService(ManagedService.class.getName(),
                this, properties);

        registration = context.registerService(ClientFactory.class.getName(),
                this.clientFactory, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug("Stopping");
        clientFactory.destroy();
        registration.unregister();
        registration = null;
        clientFactory = null;
    }

    @Override
    public void updated(Dictionary<String, ?> properties)
            throws ConfigurationException {
        if (properties != null){
            clientFactory.updated(properties);
        }

    }
}
