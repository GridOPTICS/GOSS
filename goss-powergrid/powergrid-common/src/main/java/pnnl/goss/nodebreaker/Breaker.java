package pnnl.goss.nodebreaker;

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

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
public class Breaker extends IdentifiedObject {
	
	@EmbeddedId
	private ElementIdentifier elementIdentifier;
	
	private String identName;
	private String identAlias;
	private String identPath;
	private String identDescription;
	
	private String memberOfEquipmentContainer;
	private String conductingEquipmentBaseVoltage;	
	
	private Double ratedCurrent;
	private Boolean switchNormalOpen;
	
	@Transient
	private IdentifiedObject identifiedObject;
	
	public Breaker(){
		
	}
	
	public Breaker(String mrid, String dataType, String identName, String identAlias,
			String identPath, String identDescription, Double ratedCurrent, Boolean switchNormalOpen,
			String memberOfEquipmentContainer, String conductingEquipmentBaseVoltage){
		
		elementIdentifier = new ElementIdentifier(mrid, dataType);
		this.identAlias = identAlias;
		this.identName = identName;
		this.identDescription = identDescription;
		this.identPath = identPath;
		
		this.createIdentifiedObject();
		
		
		this.ratedCurrent = ratedCurrent;
		
		this.switchNormalOpen = switchNormalOpen;
		this.memberOfEquipmentContainer = memberOfEquipmentContainer;
		this.conductingEquipmentBaseVoltage = conductingEquipmentBaseVoltage;
		
	}
	
	private void createIdentifiedObject(){
		identifiedObject = new IdentifiedObject(this.identName,
				this.identAlias, this.identPath, this.identDescription);
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

	public ElementIdentifier getElementIdentifier() {
		return elementIdentifier;
	}

	public void setElementIdentifier(ElementIdentifier elementIdentifier) {
		this.elementIdentifier = elementIdentifier;
	}

	public IdentifiedObject getIdentifiedObject() {
		if (identifiedObject == null){
			createIdentifiedObject();
		}
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identObject) {
		this.identifiedObject = identObject;
		
		if(identObject != null){
			this.identAlias = identObject.getIdentAlias();
			this.identDescription = identObject.getIdentDescription();
			this.identName = identObject.getIdentName();
			this.identPath = identObject.getIdentPathName();
		}
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
