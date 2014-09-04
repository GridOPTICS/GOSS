package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class SynchronousMachine extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
	private Double min0;
	@Column
	private Double maxQ;
	@Column
	private Integer referencePriority;
	@Column
	private String type;
	@Column
	private String memberOfEquipmentContainer;
	@Column
	private String memberOfGeneratingUnit;
	@Column
	private Double rated;
	
}
