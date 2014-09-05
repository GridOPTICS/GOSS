package pnnl.goss.powergrid.topology.nodebreaker;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class Line extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 891186584111871258L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	@Column	
	protected String lineRegion;

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
		
	public String getLineRegion() {
		return lineRegion;
	}

	public void setLineRegion(String lineRegion) {
		this.lineRegion = lineRegion;
	}

}
