package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class CurveData extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 8351778930059080566L;

	@Column
	protected String dataType;
	@Column
	private Double xvalue;
	@Column
	private Double y1value;
	@Column
	private Double y2value;

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
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
