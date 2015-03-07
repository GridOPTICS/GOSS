package pnnl.goss.core.server;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

public interface DataSourcePooledJdbc extends DataSourceObject {

	Connection getConnection() throws SQLException;
	
	void setDataSource(ConnectionPoolDataSource datasource);

}
