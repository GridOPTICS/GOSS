package pnnl.goss.core.server.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.DataSourceBuilder;
import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceType;

public class PooledSqlServiceImpl implements DataSourceObject, DataSourcePooledJdbc {
	private static final Logger log = LoggerFactory.getLogger(PooledSqlServiceImpl.class);
	private final String username;
	private final String url;
	private final String password;
	private final String driverClass;
	private final String name;
	private final Map<String, String> customizations;
	private DataSource dataSource;


	public PooledSqlServiceImpl(String datasource_name, String url, String username, String password, String driver, Map<String, String> otherProperties) {
		this.name = datasource_name;
		this.url = url;
		this.password = password;
		this.driverClass = driver;
		this.username = username;
		this.customizations = otherProperties;
		this.createDataSource();
	}

	private void createDataSource(){
		Properties propertiesForDataSource = new Properties();
		propertiesForDataSource.setProperty("username", username);
		propertiesForDataSource.setProperty("password", password);
		propertiesForDataSource.setProperty("url", url);
		propertiesForDataSource.setProperty("driverClassName", driverClass);

		propertiesForDataSource.putAll(customizations);


		if (!propertiesForDataSource.containsKey("maxOpenPreparedStatements")){
			propertiesForDataSource.setProperty("maxOpenPreparedStatements", "10");
		}

		log.debug(String.format("Creating datasource: %s, User: %s, URL: %s)", this.name, username, url));

		try {
			Class.forName(propertiesForDataSource.getProperty("driverClassName"));
			dataSource = BasicDataSourceFactory.createDataSource(propertiesForDataSource);
		} catch (ClassNotFoundException e) {
			dataSource = null;
			e.printStackTrace();
		} catch (Exception e) {
			dataSource = null;
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DataSourceType getDataSourceType() {
		return DataSourceType.DS_TYPE_JDBC;
	}

	@Override
	public Connection getConnection() throws SQLException {

		if (dataSource == null){
			throw new SQLException("Invalid datasource.");
		}

		return dataSource.getConnection();
	}

}
