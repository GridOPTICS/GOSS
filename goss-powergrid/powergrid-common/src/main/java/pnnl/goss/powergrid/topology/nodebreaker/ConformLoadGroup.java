package pnnl.goss.powergrid.topology.nodebreaker;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.CONFORM_LOAD_GROUP;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.CONFORM_LOAD_GROUP_MRID;
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
@Table(name=CONFORM_LOAD_GROUP)
@AttributeOverride(name="mrid", column=@Column(name=CONFORM_LOAD_GROUP_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class ConformLoadGroup extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = -3107463087908232931L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public ConformLoadGroup(){
		dataType = CONFORM_LOAD_GROUP;
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
