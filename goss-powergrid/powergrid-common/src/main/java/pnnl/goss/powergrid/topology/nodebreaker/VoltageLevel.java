package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import pnnl.goss.powergrid.topology.Identified;
import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.Substation;

@Entity
public class VoltageLevel extends IdentifiedObject {
	
	@Column
	protected String dataType;
	
	public VoltageLevel(){
		this.dataType = "VOLTAGE_LEVEL";
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
