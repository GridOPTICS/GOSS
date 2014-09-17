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
@Table(name=MEASUREMENT_TYPE)
@AttributeOverride(name="mrid", column=@Column(name=MEASUREMENT_TYPE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class MeasurementType extends IdentifiedObject implements NodeBreakerDataType   {
	
	private static final long serialVersionUID = 317614572981213820L;
	
	@Column(name=DATA_TYPE)
	protected String dataType;
	
	public MeasurementType(){
		dataType = MEASUREMENT_TYPE;
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
