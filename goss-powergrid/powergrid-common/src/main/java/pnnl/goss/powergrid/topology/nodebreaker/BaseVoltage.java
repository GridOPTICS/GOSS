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
@Table(name=BASE_VOLTAGE)
@AttributeOverride(name="mrid", column=@Column(name=BASE_VOLTAGE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class BaseVoltage extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = -7397775883382190027L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public BaseVoltage(){
		dataType = BASE_VOLTAGE;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
		
}
