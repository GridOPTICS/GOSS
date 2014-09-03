package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class RegularTimePoint {
	
	@Id
	private String mrid;
	
	@Embedded
	private IdentifiedObject identifiedObject;
	
	private String intervalSchedule;
	private Double value1;
	private Double value2;

	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
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
