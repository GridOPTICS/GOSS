package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class Analog {
	@Id
	private String mrid;
	
	private Double normalValue;
	private Boolean positiveFlowIn;
	
	@Embedded
	private IdentifiedObject identifiedObject;

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
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
