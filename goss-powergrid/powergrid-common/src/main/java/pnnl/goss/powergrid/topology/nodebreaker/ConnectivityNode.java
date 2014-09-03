package pnnl.goss.powergrid.topology.nodebreaker;

import java.util.HashMap;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class ConnectivityNode {
	
	@Id
	private String mrid;
	
	@Embedded
	private IdentifiedObject identifiedObject;

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
	}
}
