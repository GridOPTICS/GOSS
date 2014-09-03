package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class AnalogLimit {
	@Id
	private String mrid;
	private Double value;
	
	@Embedded
	private IdentifiedObject identifiedObject;

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
