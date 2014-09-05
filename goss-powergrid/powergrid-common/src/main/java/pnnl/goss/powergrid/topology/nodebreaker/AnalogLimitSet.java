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
@Table(name=ANALOG_LIMIT_SET)
@AttributeOverride(name="mrid", column=@Column(name=ANALOG_LIMIT_SET_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class AnalogLimitSet extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = -5597074884975084827L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	@Column
	private Boolean limitSetIsPercentageLimits;

	public AnalogLimitSet(){
		dataType = ANALOG_LIMIT_SET;
	}
	
	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Boolean getLimitSetIsPercentageLimits() {
		return limitSetIsPercentageLimits;
	}

	public void setLimitSetIsPercentageLimits(Boolean limitSetIsPercentageLimits) {
		this.limitSetIsPercentageLimits = limitSetIsPercentageLimits;
	}
}
