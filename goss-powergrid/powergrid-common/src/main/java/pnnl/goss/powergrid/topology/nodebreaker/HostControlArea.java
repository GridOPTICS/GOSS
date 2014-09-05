package pnnl.goss.powergrid.topology.nodebreaker;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
@Entity
public class HostControlArea extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = -6300701230534749L;

	@Column(name=DATA_TYPE)
	protected String dataType;

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
}
