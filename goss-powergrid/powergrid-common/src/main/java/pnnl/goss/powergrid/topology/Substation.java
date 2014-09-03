package pnnl.goss.powergrid.topology;

import javax.persistence.Id;

public class Substation {
	
	@Id
	private String mrid;
	
	private IdentifiedObject identifiedObject;

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
	}
}
