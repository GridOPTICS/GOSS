package pnnl.goss.powergrid.topology.nodebreaker;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
@Table(name=ACCUMULATOR)
@AttributeOverride(name="mrid", column=@Column(name=ACCUMULATOR_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Accumulator extends IdentifiedObject implements NodeBreakerDataType  {
	
	private static final long serialVersionUID = 2173096405492358853L;
	
	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public Accumulator(){
		dataType = ACCUMULATOR;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
