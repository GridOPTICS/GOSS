package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class Disconnector extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
	protected String dataType;
	@Column
	private Boolean switchNormalOpen;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Boolean getSwitchNormalOpen() {
		return switchNormalOpen;
	}

	public void setSwitchNormalOpen(Boolean switchNormalOpen) {
		this.switchNormalOpen = switchNormalOpen;
	}
}
