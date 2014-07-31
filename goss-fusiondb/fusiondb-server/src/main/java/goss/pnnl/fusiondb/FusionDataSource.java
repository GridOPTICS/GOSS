package goss.pnnl.fusiondb;

import java.sql.Connection;

import org.apache.commons.dbcp.BasicDataSource;

public interface FusionDataSource {
	
	public static String PROP_FUSIONDB_USER = "fusiondb.user";
	public static String PROP_FUSIONDB_PASSWORD = "fusiondb.password";
	public static String PROP_FUSIONDB_URI = "fusiondb.uri";

	public abstract Connection getConnection();

	/**
	 * <p>
	 * Adds a poolable connection using the passed parameters to connect to the datasource.
	 * </p>
	 */
	public abstract BasicDataSource getDataSourceConnection(String url,
			String username, String password, String driver) throws Exception;

}