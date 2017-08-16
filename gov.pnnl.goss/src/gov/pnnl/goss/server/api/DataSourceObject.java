package gov.pnnl.goss.server.api;

/**
 * The DataSourceObject interface allows the creation of arbitrary objects
 * to be retrieved by name from the DataSourceRegistry.
 * 
 * @author Craig Allwardt
 *
 */
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
		
}
