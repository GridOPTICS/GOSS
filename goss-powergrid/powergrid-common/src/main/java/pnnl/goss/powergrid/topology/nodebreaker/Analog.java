package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class Analog extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
	private Double normalValue;
	@Column
	private Boolean positiveFlowIn;
	
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
