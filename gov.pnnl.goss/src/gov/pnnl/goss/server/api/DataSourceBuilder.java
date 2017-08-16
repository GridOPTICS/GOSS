package gov.pnnl.goss.server.api;

import java.util.Properties;

/**
 * An interface for building a datasource and adding it to the datasource registry
 * to make Connection's available for connecting to throughout the platform.
 * 
 * @author C. Allwardt
 *
 */
public interface DataSourceBuilder {
	
	/**
	 * A convienence key that can be used to lookup from jndi or GOSS's
	 * DataSourceRegistry.
	 */
	public static final String DATASOURCE_NAME = "DATASOURCE_NAME";
	
	/**
	 * The user parameter should be mapped to this property name.
	 */
	public static final String DATASOURCE_USER = "username";
	
	/**
	 * The password parameter should be mapped to this property name.
	 */
	public static final String DATASOURCE_PASSWORD = "password";
	
	/**
	 * The url parameter should be mapped to this property name.
	 */
	public static final String DATASOURCE_URL = "url";
	
	/**
	 * The driver parameter parameter should be mapped to this property name.
	 */
	public static final String DATASOURCE_DRIVER = "driverClassName";
	
	/**
	 * Create a datasource and store it for lookup by dsName.
	 * 
	 * @param dsName
	 * @param url
	 * @param user
	 * @param password
	 * @param driver
	 * @throws ClassNotFoundException 
	 * @throws Exception 
	 */
	void create(String dsName, String url, String user, String password, String driver) throws ClassNotFoundException, Exception;
	
	/**
	 * Use properties file creation of the datasource.  The properties should have at minimum
	 * at minimum a DATASOURCE_NAME, DATASOURCE_USER, DATASOURCE_PASSWORD, 
	 * DATASOURCE_URL, and a DATASOURCE_DRIVER or the implementor should throw an
	 * Exception.
	 * 
	 * @param properties
	 * @throws ClassNotFoundException 
	 * @throws Exception 
	 */
	void create(String dsName, Properties properties) throws ClassNotFoundException, Exception;
}
