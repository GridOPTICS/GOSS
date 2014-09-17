package pnnl.goss.powergrid.topology.nodebreaker;

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
@Table(name=REGULAR_TIMEPOINT)
@AttributeOverride(name="mrid", column=@Column(name=REGULAR_TIMEPOINT_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class RegularTimePoint extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = -1005140752411823468L;

	@Column(name=DATA_TYPE)
	protected String dataType;	
	@Column
	private String intervalSchedule;
	@Column
	private Double value1;
	@Column
	private Double value2;

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public String getIntervalSchedule() {
		return intervalSchedule;
	}

	public void setIntervalSchedule(String intervalSchedule) {
		this.intervalSchedule = intervalSchedule;
	}

	public Double getValue1() {
		return value1;
	}

	public void setValue1(Double value1) {
		this.value1 = value1;
	}

	public Double getValue2() {
		return value2;
	}

	public void setValue2(Double value2) {
		this.value2 = value2;
	}
}
