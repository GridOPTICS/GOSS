package pnnl.goss.powergrid.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import pnnl.goss.powergrid.topology.nodebreaker.VoltageLevel;

@Entity
//@Table(name="SUBSTATION")
public class Substation extends IdentifiedObject {
	
	@Column
	protected String dataType;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name="Substation.mrid")
	private List<VoltageLevel> voltageLevels;
	
	public Substation(){
		this.dataType = "SUBSTATION";
		voltageLevels = new ArrayList<>();
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	
	
	
	
//	public Substation(){
//		voltageLevels = new ArrayList<VoltageLevel>();
//	}
//	
	public void addVoltageLevel(VoltageLevel obj){
		voltageLevels.add(obj);
		//obj.setSubstation(this);
	}

	public List<VoltageLevel> getVoltageLevels() {
		return voltageLevels;
	}

	public void setVoltageLevels(List<VoltageLevel> voltageLevels) {
		this.voltageLevels = voltageLevels;
	}
}
