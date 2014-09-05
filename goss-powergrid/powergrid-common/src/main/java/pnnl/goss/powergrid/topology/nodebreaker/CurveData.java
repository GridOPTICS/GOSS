package pnnl.goss.powergrid.topology.nodebreaker;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.CURVE_DATA;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.CURVE_DATA_MRID;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.DATA_TYPE;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;
@Entity
@Table(name=CURVE_DATA)
@AttributeOverride(name="mrid", column=@Column(name=CURVE_DATA_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class CurveData extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 8351778930059080566L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	@Column
	private Double xvalue;
	@Column
	private Double y1value;
	@Column
	private Double y2value;
	
	public CurveData(){
		dataType = CURVE_DATA;
	}

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
