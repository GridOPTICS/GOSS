package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class AnalogLimitSet extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
	protected String dataType;
	@Column
	private Boolean limitSetIsPercentageLimits;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Boolean getLimitSetIsPercentageLimits() {
		return limitSetIsPercentageLimits;
	}

	public void setLimitSetIsPercentageLimits(Boolean limitSetIsPercentageLimits) {
		this.limitSetIsPercentageLimits = limitSetIsPercentageLimits;
	}
}
