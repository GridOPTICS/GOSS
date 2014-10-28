package pnnl.goss.core.server;

import org.apache.commons.dbcp.BasicDataSource;

public interface BasicDataSourceCreator {
	BasicDataSource create(String url, String username, String password) throws Exception;
	BasicDataSource create(String url, String username, String password, String driver) throws Exception;
}
