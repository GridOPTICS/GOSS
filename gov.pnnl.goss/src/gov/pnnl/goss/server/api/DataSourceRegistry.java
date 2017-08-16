package gov.pnnl.goss.server.api;

import java.util.Map;

public interface DataSourceRegistry {
	
	/**
	 * Get a DataSourceObject from the registry.  If a key
	 * does not exist then this call should return null.
	 * 
	 * @param  
	 * @param key
	 * @return
	 */
	public DataSourceObject get(String key);
	
	/**
	 * Adds a DataSourceObject to the registry, making it available for
	 * the entire system.
	 * 
	 * @param key
	 * @param obj
	 */
	public void add(String key, DataSourceObject obj);
	
	/**
	 * Remove DataSourceObject from the registry.  If the object doesn't 
	 * exist this function is silent.
	 * 
	 * @param key
	 */
	public void remove(String key);
	
	/**
	 * Retrieve a map of names-> datasourcetype that can be retrieved
	 * by the user to determine capabilities of datasources.
	 * 
	 * @return
	 */
	public Map<String, DataSourceType> getAvailable();
}
