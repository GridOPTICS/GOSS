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
@Table(name=POWER_TRANSFORMER)
@AttributeOverride(name="mrid", column=@Column(name=POWER_TRANSFORMER_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class PowerTransformer extends IdentifiedObject implements NodeBreakerDataType, ConductingEquipment  {

	private static final long serialVersionUID = 5637062568160333708L;
	
	@Column(name=DATA_TYPE)
	protected String dataType;

	public PowerTransformer(){
		dataType = POWER_TRANSFORMER;
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
