package pnnl.goss.powergrid.topology.nodebreaker;

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

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
@Entity
@Table(name=BASE_VOLTAGE)
@AttributeOverride(name="mrid", column=@Column(name=BASE_VOLTAGE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class BaseVoltage extends IdentifiedObject implements NodeBreakerDataType, ConductingEquipment  {

	private static final long serialVersionUID = -7397775883382190027L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name=VOLTAGE_LEVEL_MRID)
	private List<VoltageLevel> voltageLevels;
	
	public BaseVoltage(){
		dataType = BASE_VOLTAGE;
		voltageLevels = new ArrayList<>();
	}
	
	public void addVoltageLevel(VoltageLevel voltageLevel){
		voltageLevels.add(voltageLevel);
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public List<VoltageLevel> getVoltageLevels() {
		return voltageLevels;
	}

	public void setVoltageLevels(List<VoltageLevel> voltageLevels) {
		this.voltageLevels = voltageLevels;
	}
		
}
