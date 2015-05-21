package pnnl.goss.core.server;


public enum DataSourceType {
	DS_TYPE_JDBC(10),
	DS_TYPE_REST(20),
	DS_TYPE_OTHER(1000);
	
	private final int number;

	private DataSourceType(int number) {
		this.number = number;
	}	
}