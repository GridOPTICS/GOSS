package pnnl.goss.powergrid.topology;

public interface NodeBreakerDataType {
	
	public static final String SUBSTATION = "Substation";
	
	public static final String VOLTAGE_LEVEL = "VoltageLevel";
	
	public String getDataType();
	
	public void setDataType(String dataType);

}
