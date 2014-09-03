package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class CurveData {
	
	@Id
	private String mrid;
	
	@Embedded
	private IdentifiedObject identifiedObject;

	private Double xvalue;
	private Double y1value;
	private Double y2value;
	
	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
	}

	public Double getXvalue() {
		return xvalue;
	}

	public void setXvalue(Double xvalue) {
		this.xvalue = xvalue;
	}

	public Double getY1value() {
		return y1value;
	}

	public void setY1value(Double y1value) {
		this.y1value = y1value;
	}

	public Double getY2value() {
		return y2value;
	}

	public void setY2value(Double y2value) {
		this.y2value = y2value;
	}
}
