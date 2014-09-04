package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class Disconnector extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
	private Boolean switchNormalOpen;

	public Boolean getSwitchNormalOpen() {
		return switchNormalOpen;
	}

	public void setSwitchNormalOpen(Boolean switchNormalOpen) {
		this.switchNormalOpen = switchNormalOpen;
	}
}
