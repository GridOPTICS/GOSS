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
@Table(name=CONNECTIVITY_NODE)
@AttributeOverride(name="mrid", column=@Column(name=CONNECTIVITY_NODE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class ConnectivityNode extends IdentifiedObject implements NodeBreakerDataType  {
	
	private static final long serialVersionUID = 5009733893786037446L;
	
	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public ConnectivityNode(){
		dataType = CONNECTIVITY_NODE;
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
