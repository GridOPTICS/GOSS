package pnnl.goss.core.server.runner.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.h2.util.OsgiDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.AbstractDataSourceObject;
import pnnl.goss.core.server.AbstractSqlPooledDatasource;
import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceType;

@Component
public class H2TestDataSource extends AbstractSqlPooledDatasource implements DataSourceObject, DataSourcePooledJdbc {
	private static final Logger log = LoggerFactory.getLogger(H2TestDataSource.class);
	
	@ServiceDependency(name="org.h2.util.OsgiDataSourceFactory")
	private  volatile DataSourceFactory factory;
	
	@Start
	public void start() {
		Properties properties = new Properties();
		
		System.out.println("Starting service!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("factory is: "+ factory.toString());
		properties.setProperty("url", "jdbc:h2:mem:fusion");
		properties.setProperty(OsgiDataSourceFactory.JDBC_USER, "sa");
		properties.setProperty(OsgiDataSourceFactory.JDBC_PASSWORD, "sa");
		
		try {
			setDataSource(factory.createConnectionPoolDataSource(properties));
			log.debug("getConnection().isClosed(): " + getConnection().isClosed());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("factory is? "+factory);
	}

	@Override
	public void onRemoved() {
		// TODO Auto-generated method stub
		
	}
	
}
