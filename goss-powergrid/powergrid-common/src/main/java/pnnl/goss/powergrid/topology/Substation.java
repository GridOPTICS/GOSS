package pnnl.goss.powergrid.topology;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.SUBSTATION;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.nodebreaker.VoltageLevel;

@Entity
@Table(name=SUBSTATION)
@IndexCollection(columns={@Index(name="dataType")})
public class Substation extends IdentifiedObject implements NodeBreakerDataType {
	
	@Column
	protected String dataType;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name="Substation.mrid")
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
