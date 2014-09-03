package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class Line {
	
	@Id
	private String mrid;
	
	protected String lineRegion;
	
	@Embedded
	protected IdentifiedObject identifiedObject;

	public String getMrid() {
		return mrid;
	}

	public String getLineRegion() {
		return lineRegion;
	}

	public void setLineRegion(String lineRegion) {
		this.lineRegion = lineRegion;
	}

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		this.mrid = identifiedObject.getIdentMrid();
	}
}
