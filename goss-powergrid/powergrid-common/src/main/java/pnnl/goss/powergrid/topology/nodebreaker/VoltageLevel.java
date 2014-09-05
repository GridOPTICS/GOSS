package pnnl.goss.powergrid.topology.nodebreaker;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.DATA_TYPE;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.SUBSTATION;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.VOLTAGE_LEVEL;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.VOLTAGE_LEVEL_MRID;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.Substation;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
@Table(name=VOLTAGE_LEVEL)
@AttributeOverride(name="mrid", column=@Column(name=VOLTAGE_LEVEL_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class VoltageLevel extends IdentifiedObject {
	
	private static final long serialVersionUID = -3855948715546769934L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@Column(name=SUBSTATION)
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
