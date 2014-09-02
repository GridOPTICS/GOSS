package pnnl.goss.powergrid.topology.nodebreaker;

import java.util.List;

import javax.persistence.CascadeType;
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

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
public class Breaker {
	public final String OBJECT_TYPE = "Breaker";
	
	@Id
	private String mrid;
	private String memberOfEquipmentContainer;
	private String conductingEquipmentBaseVoltage;	
	
	private Double ratedCurrent;
	private Boolean switchNormalOpen;
	
	@Embedded
	private IdentifiedObject identifiedObject;
	
	public Breaker(){
		
	}
	
	
	
	public Breaker(String mrid, String dataType, String identName, String identAlias,
			String identPath, String identDescription, Double ratedCurrent, Boolean switchNormalOpen,
			String memberOfEquipmentContainer, String conductingEquipmentBaseVoltage){
		
		this.identifiedObject = new IdentifiedObject(mrid, OBJECT_TYPE,  identName, identAlias, identPath, identDescription);
		
		this.ratedCurrent = ratedCurrent;
		
		this.switchNormalOpen = switchNormalOpen;
		this.memberOfEquipmentContainer = memberOfEquipmentContainer;
		this.conductingEquipmentBaseVoltage = conductingEquipmentBaseVoltage;
		
	}
	
	/*private void createIdentifiedObject(){
		identifiedObject = new IdentifiedObject(this.identName,
				this.identAlias, this.identPathName, this.identDescription);
	}*/
		
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


	public IdentifiedObject getIdentifiedObject() {
//		if (identifiedObject == null){
//			createIdentifiedObject();
//		}
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identObject) {
		this.identifiedObject = identObject;
		this.mrid = identObject.getIdentMrid();
		
//		if(identObject != null){
//			this.identAlias = identObject.getIdentAlias();
//			this.identDescription = identObject.getIdentDescription();
//			this.identName = identObject.getIdentName();
//			this.identPathName = identObject.getIdentPathName();
//		}
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

	public String getMrid() {
		return mrid;
	}
}
