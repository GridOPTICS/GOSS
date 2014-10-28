package pnnl.goss.core.server;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * The BasicDataSourceCreator allows the creation of an apache BasicDataSource through
 * a service interface. 
 * 
 * @author Craig Allwardt
 *
 */
public interface BasicDataSourceCreator {
	
	/**
	 * Creates a basic datasource with the a mysql database driver.
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	BasicDataSource create(String url, String username, String password) throws Exception;
	
	/**
	 * Creates a basic datasource and allow the database driver to be specified.  Note
	 * that in order for this to work properly the database driver must be on the 
	 * classpath.
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	BasicDataSource create(String url, String username, String password, String driver) throws Exception;
}
