package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class TransformerWinding extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 4941755759033279919L;

	@Column
	protected String dataType;
	@Column
	private Double ratedU;
	@Column
	private Double ratedS;
	@Column
	private Double x;
	@Column
	private Double b;
	@Column
	private Double r;
	@Column
	private Double g;

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
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
