package pnnl.goss.powergrid.topology;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.DATA_TYPE;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.SUBSTATION;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.SUBSTATION_MRID;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import pnnl.goss.powergrid.topology.nodebreaker.VoltageLevel;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
@Table(name=SUBSTATION)
@AttributeOverride(name="mrid", column=@Column(name=SUBSTATION_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Substation extends IdentifiedObject implements NodeBreakerDataType {
	
	private static final long serialVersionUID = 3657810744633660312L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name=SUBSTATION_MRID)
	private List<VoltageLevel> voltageLevels;
	
	public Substation(){
		this.dataType = SUBSTATION;
		voltageLevels = new ArrayList<>();
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public void addVoltageLevel(VoltageLevel voltageLevel){
		voltageLevels.add(voltageLevel);
		voltageLevel.setSubstation(this);
	}

	public List<VoltageLevel> getVoltageLevels() {
		return voltageLevels;
	}

	public void setVoltageLevels(List<VoltageLevel> voltageLevels) {
		this.voltageLevels = voltageLevels;
	}
}
