package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
public class MeasurementType extends IdentifiedObject implements NodeBreakerDataType   {
	
	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public MeasurementType(){
		dataType = MEASUREMENT_TYPE;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
