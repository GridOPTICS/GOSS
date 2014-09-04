package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class SynchronousMachine extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
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
	
	public String getDataType() {
		return dataType;
	}
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
