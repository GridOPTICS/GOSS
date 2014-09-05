package pnnl.goss.powergrid.topology.nodebreaker;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class Analog extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 5285780927858065902L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	@Column
	private Double normalValue;
	@Column
	private Boolean positiveFlowIn;

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Double getNormalValue() {
		return normalValue;
	}

	public void setNormalValue(Double normalValue) {
		this.normalValue = normalValue;
	}

	public Boolean getPositiveFlowIn() {
		return positiveFlowIn;
	}

	public void setPositiveFlowIn(Boolean positiveFlowIn) {
		this.positiveFlowIn = positiveFlowIn;
	}
}
