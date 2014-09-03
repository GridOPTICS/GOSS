package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class AnalogLimitSet {
	@Id
	private String mrid;
	
	private Boolean limitSetIsPercentageLimits;
	
	@Embedded
	private IdentifiedObject identifiedObject;

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
	}

	public Boolean getLimitSetIsPercentageLimits() {
		return limitSetIsPercentageLimits;
	}

	public void setLimitSetIsPercentageLimits(Boolean limitSetIsPercentageLimits) {
		this.limitSetIsPercentageLimits = limitSetIsPercentageLimits;
	}
}
