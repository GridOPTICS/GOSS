package pnnl.goss.core.server;

public interface DataSourceObject {

	/**
	 * The name of the datasource is how the registry will be able to
	 * retrieve it from the datastore.  
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Some special handling is available for datasources that are
	 * jdbc compliant.  For instance they can have pooled connections
	 * by default.
	 * 
	 * @return
	 */
	DataSourceType getDataSourceType();
	
	/**
	 * This trigger is fired whenever the datasource is retrieved
	 * from the registry.
	 */
	void onGet();
	
	/**
	 * This trigger happens when the datasource is removed from the
	 * registry.  Any internal cleanup should happen in this method
	 * as it is the last thing that is called before it is removed
	 * from the datasource registry.
	 */
	void onRemoved();
}
