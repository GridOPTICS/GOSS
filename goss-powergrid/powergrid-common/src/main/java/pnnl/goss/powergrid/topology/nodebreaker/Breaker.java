package pnnl.goss.powergrid.topology.nodebreaker;

import java.util.ArrayList;
import java.util.List;

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
@Table(name=BREAKER)
@AttributeOverride(name="mrid", column=@Column(name=BREAKER_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Breaker extends IdentifiedObject implements NodeBreakerDataType, Switch, PowerSystemResource  {

	private static final long serialVersionUID = -7019934471143148743L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	private String memberOfEquipmentContainer;
	private String conductingEquipmentBaseVoltage;	
	
	private Double ratedCurrent;
	private Boolean switchNormalOpen;
	
	//private List<Switch> switches;
	
	public Breaker(){
		dataType = BREAKER;
		//switches = new ArrayList<>();
	}
		
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
	
	@Override
	public Boolean getSwitchNormalOpen() {
		return switchNormalOpen;
	}
	
	@Override
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
