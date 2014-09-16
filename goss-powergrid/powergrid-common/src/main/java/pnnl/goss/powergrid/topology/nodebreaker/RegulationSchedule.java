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
@Table(name=REGULATION_SCHEDULE)
@AttributeOverride(name="mrid", column=@Column(name=REGULATION_SCHEDULE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class RegulationSchedule extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 8509171609446008124L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public RegulationSchedule(){
		dataType = REGULATION_SCHEDULE;
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
