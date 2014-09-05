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
@Table(name=ACLINESEGMENT)
@AttributeOverride(name="mrid", column=@Column(name=ACLINESEGMENT_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class ACLineSegment extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = 6263933162623230597L;
	
	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public ACLineSegment(){
		dataType=ACLINESEGMENT;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
