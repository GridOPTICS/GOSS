package pnnl.goss.mdart.server.datasources;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.mdart.util.MDARTDBConfiguration;



public class MDARTDataSource {

//	private BoneCP connectionPool = null; 
	private BasicDataSource connectionPool = null;
	private static MDARTDataSource instance;
	private static final Logger log = LoggerFactory.getLogger(MDARTDataSource.class);
	Dictionary configProperties;
	
	private MDARTDataSource() {
		
		try{
				
			log.info("Connecting to GOSS Metadata store");
			log.debug("Using GOSS Metadata store at "+MDARTDBConfiguration.getProperty(MDARTDBConfiguration.CONFIG_DB_URI));
			System.out.println("Using GOSS Metadata store at "+MDARTDBConfiguration.getProperty(MDARTDBConfiguration.CONFIG_DB_URI));
			connectionPool = getDataSourceConnection(MDARTDBConfiguration.getProperty(MDARTDBConfiguration.CONFIG_DB_URI), MDARTDBConfiguration.getProperty(MDARTDBConfiguration.CONFIG_DB_USER),
					MDARTDBConfiguration.getProperty(MDARTDBConfiguration.CONFIG_DB_PW), null);
				
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	

	public static void resetInstance(){
		log.debug("Resetting MDARTDataSource Instance");
		if(instance!=null){
			try {
				instance.connectionPool.close();
			} catch (SQLException e) {
				log.error("Error closing mdart datasource connection");
			}
			instance = null;
		}
	}
	public static MDARTDataSource getInstance(){
		try{
			if(instance == null){
				System.out.println("Creating new data store connection");
				instance  = new MDARTDataSource();
			}
			return instance;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public Connection getConnection(){
		try{
			
			return connectionPool.getConnection();
		}
		catch(SQLException e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>
	 * Adds a poolable connection using the passed parameters to connect to the datasource.
	 * </p>
	 */
	public BasicDataSource getDataSourceConnection(String url, String username, String password, String driver) throws Exception {
		Properties properties = new Properties();
		
		// Available properties http://commons.apache.org/proper/commons-dbcp/xref-test/org/apache/commons/dbcp/TestBasicDataSourceFactory.html#50
		if (driver == null || driver.trim().equals("")){
			properties.setProperty("driverClassName", "com.mysql.jdbc.Driver"); 
		}
		else{
			properties.setProperty("driverClassName", driver);
		}
		
		Class.forName(properties.getProperty("driverClassName"));
		
		properties.setProperty("url", url);
		properties.setProperty("username", username);
		properties.setProperty("password", password);
		
		properties.setProperty("maxOpenPreparedStatements", "10");
		
		log.info("Connecting datasource to url: "+url+" with user: "+username);
		
		return (BasicDataSource)BasicDataSourceFactory.createDataSource(properties);
		
	}

	
}
