package pnnl.goss.powergrid.topology.nodebreaker;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
public class Breaker extends IdentifiedObject implements NodeBreakerDataType  {

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

	public String getDataType() {
		return dataType;
	}

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
