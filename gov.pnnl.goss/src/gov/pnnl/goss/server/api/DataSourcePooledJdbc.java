package gov.pnnl.goss.server.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSourcePooledJdbc extends DataSourceObject {

	Connection getConnection() throws SQLException;

}
