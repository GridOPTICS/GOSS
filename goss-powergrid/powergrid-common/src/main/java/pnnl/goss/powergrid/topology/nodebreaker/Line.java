package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class Line extends IdentifiedObject implements NodeBreakerDataType  {
	
	@Column	
	protected String lineRegion;
	
	public String getLineRegion() {
		return lineRegion;
	}

	public void setLineRegion(String lineRegion) {
		this.lineRegion = lineRegion;
	}

}
