package pnnl.goss.core.server;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSourcePooledJdbc extends DataSourceObject {

	Connection getConnection() throws SQLException;

}
