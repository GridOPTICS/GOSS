package gov.pnnl.goss.server.registry;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.pnnl.goss.server.api.DataSourceBuilder;
import gov.pnnl.goss.server.api.DataSourceRegistry;
import gov.pnnl.goss.server.api.DataSourceType;


/**
 * The PooledBasicDataSourceBuilderImpl class implements the DataSourceBuilder inteface.  It
 * creates a DataSourceObject wrapper so that one can easily register datasources without
 * having to create another class.  
 * 
 * The easiest way to do this would be during the loading of a component, either in the
 * service Activator or in the @Start annotated method if using dependencymanager. 
 *  
 * @author Craig Allwardt
 *
 */
@Component
public class PooledBasicDataSourceBuilderImpl implements DataSourceBuilder {
	
	@ServiceDependency
	private DataSourceRegistry registry;
	
	private static final Logger log = LoggerFactory.getLogger(PooledBasicDataSourceBuilderImpl.class);
		
	
	public void createMysql(String dsName, String url, String username, String password) throws Exception{
		create(dsName, url, username, password, "com.mysql.jdbc.Driver");
	}

	@Override
	public void create(String dsName, String url, String username, String password,
			String driver) throws Exception {
		
		Properties propertiesForDataSource = new Properties();
		propertiesForDataSource.setProperty("username", username);
		propertiesForDataSource.setProperty("password", password);
		propertiesForDataSource.setProperty("url", url);
		propertiesForDataSource.setProperty("driverClassName", driver);
		
		create(dsName, propertiesForDataSource);
	}

	@Override
	public void create(String dsName, Properties properties) throws Exception {
		
		List<String> checkItems = Arrays.asList(new String[]{"username", "password", "url", "driverClassName"});
		
		for (String item: checkItems){
			if(properties.containsKey(item)){
				String value = properties.getProperty(item);
				if (value == null || value.isEmpty()){
					throw new IllegalArgumentException(item + " was specified incorrectly!");
				}
			}
			else{
				throw new IllegalArgumentException(item+" must be specified!");
			}
		}
		
		if (!properties.containsKey("maxOpenPreparedStatements")){
			properties.setProperty("maxOpenPreparedStatements", "10");
		}
		
		log.debug("Creating BasicDataSource\n\tURI:"+properties.getProperty("url")+"\n\tUser:\n\t"+properties.getProperty("username"));
		
		Class.forName(properties.getProperty("driverClassName"));
		
		DataSource ds = BasicDataSourceFactory.createDataSource(properties);
		
		
		registry.add(dsName, new DataSourceObjectImpl(dsName, DataSourceType.DS_TYPE_JDBC, ds));		
	}
}
