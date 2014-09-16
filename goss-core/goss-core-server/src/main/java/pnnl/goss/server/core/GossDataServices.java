package pnnl.goss.server.core;

import java.sql.Connection;
import java.util.Collection;

/**
 * The purpose of the GossDataServices is to allow management of multiple
 * types of data through an easy interface.
 * 
 */
public interface GossDataServices {
	
	/**
	 * Registers a new data service with the component.  The serviceName should
	 * be a non-rooted directory like string.
	 * 
	 * Ex:
	 * 		registerData("goss/fusion", new FusionDataSource());
	 *  
	 * @param serviceName
	 * @param dataservice
	 */
	void registerData(String serviceName, Object dataservice);
	
	/**
	 * Unregisters the service coresponding to serverName.
	 * 
	 * @param serviceName
	 */
	void unRegisterData(String serviceName);
	
	/**
	 * This method is a short cut for getting at a pooled connection.
	 * 
	 * @param serviceName
	 * @return
	 */
	Connection getPooledConnection(String serviceName);
	
	/**
	 * Retrieve the dataservice.
	 * 
	 * @param serviceName
	 * @return
	 */
	Object getDataService(String serviceName);
	
	/**
	 * Determine whether or not a service has been registerd under the passed
	 * name.
	 * 
	 * @param serviceName
	 * @return
	 */
	boolean contains(String serviceName);
	
	/**
	 * An unmodifyable collection of data service names that have been registerd
	 * with the data services object.
	 * 
	 * @return a collection of services.
	 */
	Collection<String> getAvailableDataServices();
	
	/**
	 * An unmodifyable collection of properties that have been loaded from
	 * a configuration file.
	 *  
	 * @return
	 */
	Collection<String> getPropertyKeys();
	
	/**
	 * Returns a value associated with the specified key.  The key and value are
	 * populated from a configuration file.
	 * 
	 * @param key
	 * @return
	 */
	String getPropertyValue(String key);
}
