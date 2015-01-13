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

import pnnl.goss.core.Client;
import pnnl.goss.core.client.internal.ClientServiceFactory;

public class CoreClientActivator implements BundleActivator, ManagedService {
    private  static final String CONFIG_PID = "pnnl.goss.core.client";
    private static Logger log = LoggerFactory.getLogger(CoreClientActivator.class);
    private ServiceRegistration<?> clientRegistration;
    private ServiceRegistration<?> configService;
    private ClientServiceFactory clientFactory = new ClientServiceFactory();

    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, Object> properties = new Hashtable<>();
        log.debug("Starting Bundle");
        properties.put(Constants.SERVICE_PID, CONFIG_PID);

        context.registerService(ManagedService.class.getName(), this, properties);
        clientRegistration = context.registerService(Client.class.getName(),
                clientFactory, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug("Stopping Bundle");
        clientRegistration.unregister();
        configService.unregister();
    }

    @Override
    public void updated(Dictionary<String, ?> properties)
            throws ConfigurationException {
        log.debug("Updating Configuration");
        if (properties != null){
            clientFactory.updateConfiguration(properties);
        }
    }

}
