package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;
import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class Breaker extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = -7019934471143148743L;

	@Column
	protected String dataType;
	private String memberOfEquipmentContainer;
	private String conductingEquipmentBaseVoltage;	
	
	private Double ratedCurrent;
	private Boolean switchNormalOpen;
		
	public Breaker(String mrid, String dataType, String identName, String identAlias,
			String identPath, String identDescription, Double ratedCurrent, Boolean switchNormalOpen,
			String memberOfEquipmentContainer, String conductingEquipmentBaseVoltage){
		
		this.ratedCurrent = ratedCurrent;
		
		this.switchNormalOpen = switchNormalOpen;
		this.memberOfEquipmentContainer = memberOfEquipmentContainer;
		this.conductingEquipmentBaseVoltage = conductingEquipmentBaseVoltage;
		
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Double getRatedCurrent() {
		return ratedCurrent;
	}
	public void setRatedCurrent(Double ratedCurrent) {
		this.ratedCurrent = ratedCurrent;
	}
	public Boolean getSwitchNormalOpen() {
		return switchNormalOpen;
	}
	public void setSwitchNormalOpen(Boolean normalOpen) {
		this.switchNormalOpen = normalOpen;
	}

	public String getMemberOfEquipmentContainer() {
		return memberOfEquipmentContainer;
	}

	public void setMemberOfEquipmentContainer(String memberOfEquipmentContainer) {
		this.memberOfEquipmentContainer = memberOfEquipmentContainer;
	}

	public String getConductingEquipmentBaseVoltage() {
		return conductingEquipmentBaseVoltage;
	}

	public void setConductingEquipmentBaseVoltage(
			String conductingEquipmentBaseVoltage) {
		this.conductingEquipmentBaseVoltage = conductingEquipmentBaseVoltage;
	}
}
