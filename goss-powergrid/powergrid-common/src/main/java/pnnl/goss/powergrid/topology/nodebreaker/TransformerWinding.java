package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class TransformerWinding {
	
	@Id
	private String mrid;
	
	@Embedded
	private IdentifiedObject identifiedObject;
	
	private Double ratedU;
	private Double ratedS;
	private Double x;
	private Double b;
	private Double r;
	private Double g;

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
	}

	public Double getRatedU() {
		return ratedU;
	}

	public void setRatedU(Double ratedU) {
		this.ratedU = ratedU;
	}

	public Double getRatedS() {
		return ratedS;
	}

	public void setRatedS(Double ratedS) {
		this.ratedS = ratedS;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getB() {
		return b;
	}

	public void setB(Double b) {
		this.b = b;
	}

	public Double getR() {
		return r;
	}

	public void setR(Double r) {
		this.r = r;
	}

	public Double getG() {
		return g;
	}

	public void setG(Double g) {
		this.g = g;
	}
}
