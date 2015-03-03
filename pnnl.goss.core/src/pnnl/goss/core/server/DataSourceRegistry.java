package pnnl.goss.core.server;

public interface DataSourceRegistry {
	
	/**
	 * Get a Datasourceobject from the registry.  If a key
	 * does not exist then this call should return null.
	 * 
	 * @param key
	 * @return
	 */
	public DataSourceObject get(String key);
	
}
