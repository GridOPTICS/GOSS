package pnnl.goss.server.core.internal;

import static pnnl.goss.core.GossCoreContants.PROP_DATASOURCES_CONFIG;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Updated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.server.core.GossDataServices;

@Provides
@Instantiate
@Component(immediate=true, managedservice = PROP_DATASOURCES_CONFIG)
public class GossDataServicesImpl implements GossDataServices {
	
	private static final Logger log = LoggerFactory.getLogger(GossDataServicesImpl.class);
	/**
	 * Holds services that have been registered with the system.
	 */
	private Hashtable<String, Object> dataservices;
	/**
	 * Configuration object that is passed to the object
	 */
	private Hashtable<String, String> properties = new Hashtable<String, String>();

	public GossDataServicesImpl(){
		log.debug("Constructing");
		dataservices = new Hashtable<String, Object>();
	}
	
	public GossDataServicesImpl(String configFile){
		this();
		log.debug("Constructing Configuration file: ");
		
	}
	
	@SuppressWarnings("rawtypes")
	public GossDataServicesImpl(Dictionary config){
		this();
		log.debug("Constructing Configuration file: ");
		update(config);
	}
	
	@Updated
	public void update(@SuppressWarnings("rawtypes") Dictionary config){
		properties.clear();
		@SuppressWarnings("rawtypes")
		Enumeration nummer = config.keys();
		
		while(nummer.hasMoreElements()){
			String key = (String)nummer.nextElement();
			log.debug("Adding property key: " + key);
			properties.put(key,  (String)config.get(key));
		}		
	}
	
	@Override
	public void registerData(String serviceName, Object dataservice) {
		log.debug("Registering: " + serviceName);
		dataservices.put(serviceName, dataservice);
	}

	@Override
	public void unRegisterData(String serviceName) {
		log.debug("Unregistering: "+serviceName);
		dataservices.remove(serviceName);		
	}

	@Override
	public Connection getPooledConnection(String serviceName) {
		log.debug("Getting ppoled connection: "+serviceName);
		Object value = dataservices.get(serviceName);
		Connection conn = null;
		try {			
			if(value != null){
				if(value instanceof DataSource){
					conn = ((DataSource)value).getConnection();
					log.debug("connection retrieved");
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return conn;
	}

	@Override
	public Object getDataService(String serviceName) {
		log.debug("Retrieving service: "+serviceName);
		return dataservices.get(serviceName);
	}
	
	@Invalidate
	public void releaseServices(){
		log.debug("Clearing services");
		dataservices.clear();
	}

	@Override
	public boolean contains(String serviceName) {
		return dataservices.contains(serviceName);
	}

	@Override
	public Collection<String> getAvailableDataServices() {
		return Collections.unmodifiableCollection(dataservices.keySet());
	}

	@Override
	public Collection<String> getPropertyKeys() {
		return Collections.unmodifiableCollection(properties.keySet());
	}

	@Override
	public String getPropertyValue(String key) {
		return properties.get(key);
	}
}
