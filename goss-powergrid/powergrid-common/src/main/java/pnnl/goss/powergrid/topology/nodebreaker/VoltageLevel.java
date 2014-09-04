package pnnl.goss.powergrid.topology.nodebreaker;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.VOLTAGE_LEVEL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.Substation;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
@Table(name=VOLTAGE_LEVEL)
@IndexCollection(columns={@Index(name="dataType")})
public class VoltageLevel extends IdentifiedObject {
	
	@Column
	protected String dataType;
	
	@ManyToOne
	@JoinColumn(name="mrid")
	protected Substation substation;
	
	public VoltageLevel(){
		this.dataType = VOLTAGE_LEVEL;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Substation getSubstation() {
		return substation;
	}

	public void setSubstation(Substation substation) {
		this.substation = substation;
	}
}
