package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
@Entity
@Table(name=DISCRETE)
@AttributeOverride(name="mrid", column=@Column(name=DISCRETE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Discrete extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 2961226119989689890L;

	@Column(name=DATA_TYPE)
	protected String dataType;		
	@Column
	protected MeasurementType measurementType;
	@Column
	protected PowerSystemResource powerSystemResource;
	
	public Discrete(){
		dataType = DISCRETE;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public MeasurementType getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(MeasurementType measurementType) {
		this.measurementType = measurementType;
	}

	public PowerSystemResource getPowerSystemResource() {
		return powerSystemResource;
	}

	public void setPowerSystemResource(PowerSystemResource powerSystemResource) {
		this.powerSystemResource = powerSystemResource;
	}
	
}
