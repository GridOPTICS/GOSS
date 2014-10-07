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
@Table(name=DISCONNECTOR)
@AttributeOverride(name="mrid", column=@Column(name=DISCONNECTOR_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Disconnector extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 5099879392767680088L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	@Column
	private Boolean switchNormalOpen;
	

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
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
