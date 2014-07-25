package pnnl.goss.broker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GOSSActiveMQBrokerActivator implements BundleActivator, ManagedService  {
	private static final Logger log = LoggerFactory.getLogger(GOSSActiveMQBrokerActivator.class);

	private static final String CONFIG_PID = "pnnl.goss.activemq.broker";
	private static final String PROP_USE_AUTH = "useAuthorization";
	private static final String PROP_BROKER_CONFIG = "brokerConfig";
	private static final String PROP_BROKER_CONFIG_NO_SEC = "brokerConfigNoSecurity";
	private Dictionary configProperties;
	private BrokerService amqBroker;
	
	
	public void start(BundleContext context) throws Exception {
		System.out.println(this.getClass().getName()+" started");

		 // Register for updates to the goss.core.security config file.
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, CONFIG_PID);
        context.registerService(ManagedService.class.getName(), this, properties);
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println(this.getClass().getName()+" stopped");
		stopBroker();
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		System.out.println(this.getClass().getName()+" updated");
		this.configProperties = properties;
		
		try{
			stopBroker();
			startBroker();
		}catch(Exception e){
			log.error("Error while restarting broker",e);
			e.printStackTrace();
		}
		
	}
	
	
	private void startBroker() throws Exception {
		System.out.println("Starting broker");
		if(amqBroker==null){
			Boolean useAuth = new Boolean(getProperty(PROP_USE_AUTH));
			if(useAuth==null){
				log.warn("GOSS ActiveMQ Broker useAuthorization not set, defaulting to true");
				useAuth = true;
			}
			System.setProperty("activemq.base",  System.getProperty("user.dir"));
			if(useAuth){
				amqBroker = BrokerFactory.createBroker(getProperty(PROP_BROKER_CONFIG));
			} else { 
				amqBroker = BrokerFactory.createBroker(getProperty(PROP_BROKER_CONFIG_NO_SEC));
			}
			amqBroker.start();
		}
	}
	private void stopBroker() throws Exception {
		if(amqBroker!=null){
			System.out.println("Stopping broker");
			amqBroker.stop();
			amqBroker.waitUntilStopped();
			amqBroker = null;
		}
	}
	
	
	
	protected String getProperty(String propertyName){
		if(configProperties!=null){
			log.info("Goss-activemq-broker retreived property "+propertyName);
			return (String) configProperties.get(propertyName);
		}
		
		try{
			log.info("goss-activemq-broker no configuration set, retrieving from config.properties");
			Properties properties = new Properties();
			InputStream input = GOSSActiveMQBrokerActivator.class.getResourceAsStream("/config.properties");
			if(input!=null)
				properties.load(input);
			else
				properties.load(new FileInputStream("config.properties"));
			return properties.getProperty(propertyName);
		}catch(IOException e){
			e.printStackTrace();
			log.error("Error while getting activemq broker configuration",e);
		}
		
		return null;
	}

}
