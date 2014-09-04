package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class AnalogLimit extends IdentifiedObject implements NodeBreakerDataType  {
	
	@Column
	private Double value;
	
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
