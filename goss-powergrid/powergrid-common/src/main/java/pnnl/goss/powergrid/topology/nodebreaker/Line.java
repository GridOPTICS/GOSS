package pnnl.goss.powergrid.topology.nodebreaker;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;

import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
@Table(name=LINE)
@AttributeOverride(name="mrid", column=@Column(name=LINE_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class Line extends IdentifiedObject implements NodeBreakerDataType, EquipmentContainer  {

	private static final long serialVersionUID = 891186584111871258L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@Column
	protected SubGeographicalRegion subGeographicalRegion;
	
	public Line(){
		dataType = LINE;
	}
		
	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public SubGeographicalRegion getSugGeographicalRegion() {
		return subGeographicalRegion;
	}

	public void setSubGeographicalRegion(SubGeographicalRegion subGeographicalRegion) {
		this.subGeographicalRegion = subGeographicalRegion;
	}

}
