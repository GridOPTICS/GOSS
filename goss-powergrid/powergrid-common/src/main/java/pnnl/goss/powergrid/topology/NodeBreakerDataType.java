package pnnl.goss.powergrid.topology;

public interface NodeBreakerDataType {
	
	public static final String MRID = "MRID";
	public static final String NAME = "NAME";
	public static final String ALIAS = "ALIAS";
	public static final String PATH = "PATH";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String DATA_TYPE = "DATA_TYPE";
			
	public static final String SUBSTATION = "SUBSTATION";
	public static final String SUBSTATION_MRID = "SUBSTATION_MRID";
	
	public static final String VOLTAGE_LEVEL = "VOLTAGE_LEVEL";
	public static final String VOLTAGE_LEVEL_MRID = "VOLTAGE_LEVEL_MRID";
	
	public static final String SYNCHRONOUES_MACHINE = "SYNCHRONOUES_MACHINE";
	
	public static final String MEASUREMENT_TYPE = "MEASUREMENT_TYPE";
	
	public String getDataType();
	
	public void setDataType(String dataType);

}
