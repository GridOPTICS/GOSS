package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class RegularTimePoint extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
	protected String dataType;	
	@Column
	private String intervalSchedule;
	@Column
	private Double value1;
	@Column
	private Double value2;

	public String getDataType() {
		return dataType;
	}

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
