package pnnl.goss.core.server.internal;

import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.BasicDataSourceCreator;

@Provides
@Component
@Instantiate
public class BasicDataSourceCreatorImpl implements BasicDataSourceCreator {
	
	private static final Logger log = LoggerFactory.getLogger(BasicDataSourceCreator.class);

	public BasicDataSource create(String url, String username, String password) throws Exception {
		return create(url, username, password, "com.mysql.jdbc.Driver");
	}
	
	public BasicDataSource create(String url, String username, String password, String driver) throws Exception {
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
		
		log.debug("Creating BasicDataSource\n\tURI:"+url+"\n\tUser:\n\t"+username);
		
		return (BasicDataSource)BasicDataSourceFactory.createDataSource(properties);

	}
}
