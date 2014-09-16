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
@Table(name=BUS_BAR_SECTION)
@AttributeOverride(name="mrid", column=@Column(name=BUS_BAR_SECTION_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class BusBarSection extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 1983837579028685823L;

	@Column(name=DATA_TYPE)
	protected String dataType;

	public BusBarSection(){
		dataType = BUS_BAR_SECTION;
	}
	
	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
}
