package pnnl.goss.mdart.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.mdart.server.datasources.MDARTDataSource;


@SuppressWarnings("rawtypes")
public class MDARTDBConfiguration {
	/**
	 * <p>
	 * The configuration file in $SMX_HOME/etc will be CONFIG_PID.cfg
	 * </p>
	 */
	public static final String CONFIG_PID = "pnnl.goss.mdart.server";
	private static Logger log = LoggerFactory.getLogger(MDARTDBConfiguration.class);
	private static Dictionary configProperties;
	
	private static final String CONFIG_FILENAME = "/goss-mdart-server.properties";
	public static final String CONFIG_DB_URI = "databaseURI";
	public static final String CONFIG_DB_USER = "databaseUser";
	public static final String CONFIG_DB_PW = "databasePassword";
	

	public static String getProperty(String propertyName){
		if(configProperties!=null){
			return (String)configProperties.get(propertyName);
		} else {
			log.warn("MDART server Configuration not found, loading from file");
			Properties props = loadFromFile();
			return props.getProperty(propertyName);
		}
		
	}

	public static void setConfigProperties(Dictionary props){
		configProperties = props;
	}
	private static Properties loadFromFile(){
		Properties configProperties = new Properties();
		
		// Grabs the config file from the resources path which is on the class path.
		InputStream input = MDARTDataSource.class.getResourceAsStream(CONFIG_FILENAME);
		try {
			configProperties.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Error occurred retreiving  mdart server config from file",e);
			e.printStackTrace();
		}
		
		return configProperties;
	}
}
