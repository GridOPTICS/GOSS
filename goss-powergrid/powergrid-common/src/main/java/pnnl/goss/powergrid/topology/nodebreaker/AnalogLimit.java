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
@Table(name=ANALOG_LIMIT)
@AttributeOverride(name="mrid", column=@Column(name=ANALOG_LIMIT_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class AnalogLimit extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 7988664681572437467L;
	
	@Column
	protected String dataType;
	@Column
	private Double value;
	
	public AnalogLimit(){
		dataType = ANALOG_LIMIT;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
