package pnnl.goss.core.server;

import java.util.Map;

public interface DataSourceRegistry {
	
	/**
	 * Get a Datasourceobject from the registry.  If a key
	 * does not exist then this call should return null.
	 * 
	 * @param key
	 * @return
	 */
	public DataSourceObject get(String key);
	
	/**
	 * Retrieve a map of names-> datasourcetype that can be retrieved
	 * by the user to determine capabilities of datasources.
	 * 
	 * @return
	 */
	public Map<String, DataSourceType> getAvailable();
}
