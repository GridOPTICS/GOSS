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
@Table(name=TERMINAL)
@AttributeOverride(name="mrid", column=@Column(name=TERMINAL_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Terminal extends IdentifiedObject implements NodeBreakerDataType, ConductingEquipment  {

	private static final long serialVersionUID = 121797933715007032L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@Column(name=CONDUCTING_EQUIPMENT)
	protected ConductingEquipment conductingEquipment;
	
	@Column(name=CONNECTIVITY_NODE)
	protected ConnectivityNode connectivityNode;
	
		
	public Terminal(){
		this.dataType = TERMINAL;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public ConductingEquipment getConductingEquipment() {
		return conductingEquipment;
	}

	public void setConductingEquipment(ConductingEquipment conductingEquipment) {
		this.conductingEquipment = conductingEquipment;
	}

	public ConnectivityNode getConnectivityNode() {
		return connectivityNode;
	}

	public void setConnectivityNode(ConnectivityNode connectivityNode) {
		this.connectivityNode = connectivityNode;
	}
	
}
