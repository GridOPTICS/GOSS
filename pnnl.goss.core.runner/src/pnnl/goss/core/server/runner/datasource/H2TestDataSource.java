package pnnl.goss.core.server.runner.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.h2.util.OsgiDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceType;

@Component
public class H2TestDataSource implements DataSourcePooledJdbc, DataSourceObject {
	private static final Logger log = LoggerFactory.getLogger(H2TestDataSource.class);
	
	// Use an osgi connection factory.
	@ServiceDependency(name="org.h2.util.OsgiDataSourceFactory")
	private volatile DataSourceFactory factory;
	
	private ConnectionPoolDataSource pooledDataSource;
	
	@Start
	public void start() {
		Properties properties = new Properties();
		
		properties.setProperty("url", "jdbc:h2:mem:fusion");
		properties.setProperty(OsgiDataSourceFactory.JDBC_USER, "sa");
		properties.setProperty(OsgiDataSourceFactory.JDBC_PASSWORD, "sa");
		
		try {
			pooledDataSource = factory.createConnectionPoolDataSource(properties);
			log.debug("Connection pool datasource created for: " + properties.getProperty("url"));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("factory is? "+factory);
	}
	
	@Stop
	public void stop(){
		pooledDataSource = null;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public DataSourceType getDataSourceType() {
		return DataSourceType.DS_TYPE_JDBC;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return pooledDataSource.getPooledConnection().getConnection();
	}
	
}
