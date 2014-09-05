package pnnl.goss.powergrid.topology.nodebreaker;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
@Table(name=LOAD_BREAK_SWITCH)
@AttributeOverride(name="mrid", column=@Column(name=LOAD_BREAK_SWITCH_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class LoadBreakSwitch extends IdentifiedObject implements NodeBreakerDataType, Switch  {

	private static final long serialVersionUID = -6349522758230835302L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@Column
	protected Boolean switchNormalOpen;

	@Column
	protected Double ratedCurrent;
	
	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	@Override
	public Boolean getSwitchNormalOpen() {
		return switchNormalOpen;
	}
	
	@Override
	public void setSwitchNormalOpen(Boolean normalOpen) {
		this.switchNormalOpen = normalOpen;
	}

	@Override
	public Double getRatedCurrent() {
		return ratedCurrent;
	}

	@Override
	public void setRatedCurrent(Double ratedCurrent) {
		this.ratedCurrent = ratedCurrent;
	}
	
}
