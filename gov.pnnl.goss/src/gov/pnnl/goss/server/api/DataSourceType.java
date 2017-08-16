package gov.pnnl.goss.server.api;


public enum DataSourceType {
	DS_TYPE_JDBC(10),
	DS_TYPE_REST(20),
	DS_TYPE_OTHER(1000);
	
	@SuppressWarnings("unused")
	private final int number;

	private DataSourceType(int number) {
		this.number = number;
	}	
}