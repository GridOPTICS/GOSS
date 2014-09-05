package pnnl.goss.powergrid.topology.nodebreaker;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
@Table(name=ANALOG)
@AttributeOverride(name="mrid", column=@Column(name=ANALOG_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Analog extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 5285780927858065902L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	@Column
	private Double normalValue;
	@Column
	private Boolean positiveFlowIn;

	public Analog(){
		dataType = ANALOG;
	}
	
	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Double getNormalValue() {
		return normalValue;
	}

	public void setNormalValue(Double normalValue) {
		this.normalValue = normalValue;
	}

	public Boolean getPositiveFlowIn() {
		return positiveFlowIn;
	}

	public void setPositiveFlowIn(Boolean positiveFlowIn) {
		this.positiveFlowIn = positiveFlowIn;
	}
}
