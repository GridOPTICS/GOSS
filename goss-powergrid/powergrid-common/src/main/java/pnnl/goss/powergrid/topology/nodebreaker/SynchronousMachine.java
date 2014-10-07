package pnnl.goss.powergrid.topology.nodebreaker;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;


import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
@Table(name=SYNCHRONOUS_MACHINE)
@AttributeOverride(name="mrid", column=@Column(name=SYNCHRONOUS_MACHINE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class SynchronousMachine extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 4572687743938308150L;

	@Column(name=DATA_TYPE)
	protected String dataType;
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
	
	
	public SynchronousMachine(){
		dataType = SYNCHRONOUS_MACHINE;
	}
	
	@Override
	public String getDataType() {
		return dataType;
	}
	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Double getMin0() {
		return min0;
	}
	public void setMin0(Double min0) {
		this.min0 = min0;
	}
	public Double getMaxQ() {
		return maxQ;
	}
	public void setMaxQ(Double maxQ) {
		this.maxQ = maxQ;
	}
	public Integer getReferencePriority() {
		return referencePriority;
	}
	public void setReferencePriority(Integer referencePriority) {
		this.referencePriority = referencePriority;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMemberOfEquipmentContainer() {
		return memberOfEquipmentContainer;
	}
	public void setMemberOfEquipmentContainer(String memberOfEquipmentContainer) {
		this.memberOfEquipmentContainer = memberOfEquipmentContainer;
	}
	public String getMemberOfGeneratingUnit() {
		return memberOfGeneratingUnit;
	}
	public void setMemberOfGeneratingUnit(String memberOfGeneratingUnit) {
		this.memberOfGeneratingUnit = memberOfGeneratingUnit;
	}
	public Double getRated() {
		return rated;
	}
	public void setRated(Double rated) {
		this.rated = rated;
	}
	
}
