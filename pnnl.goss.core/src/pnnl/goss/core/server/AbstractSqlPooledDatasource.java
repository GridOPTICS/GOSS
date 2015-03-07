package pnnl.goss.core.server;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSqlPooledDatasource extends AbstractDataSourceObject implements DataSourcePooledJdbc, DataSourceObject {

	protected ConnectionPoolDataSource dataSource;
	private static final Logger log = LoggerFactory.getLogger(AbstractSqlPooledDatasource.class);

	public void setDataSource(ConnectionPoolDataSource datasource){
		this.dataSource = datasource;
	}

	public ConnectionPoolDataSource getDataSource(){
		return this.dataSource;
	}
	
	public Connection getConnection() throws SQLException{
		return dataSource.getPooledConnection().getConnection();
	}

	@Override
	public DataSourceType getDataSourceType() {
		return DataSourceType.DS_TYPE_JDBC;
	}
	
	@Override
	public void onRemoved() {
		dataSource = null;
	}

}
