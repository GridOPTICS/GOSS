package pnnl.goss.powergrid.topology.nodebreaker;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.DATA_TYPE;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.GEOGRAPHICAL_REGION;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.GEOGRAPHICAL_REGION_MRID;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;
@Entity
@Table(name=GEOGRAPHICAL_REGION)
@AttributeOverride(name="mrid", column=@Column(name=GEOGRAPHICAL_REGION_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class GeographicalRegion extends IdentifiedObject implements NodeBreakerDataType, Region {

	private static final long serialVersionUID = -2019222612455376153L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name=GEOGRAPHICAL_REGION_MRID)
	protected List<SubGeographicalRegion> subGeographicalRegions;
	
	public GeographicalRegion(){
		dataType=GEOGRAPHICAL_REGION;
		subGeographicalRegions = new ArrayList<>();
	}
	
	public void addSubGeographicalRegion(SubGeographicalRegion subGeographicalRegion){
		subGeographicalRegions.add(subGeographicalRegion);
		subGeographicalRegion.setGeographicalRegion(this);
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public List<SubGeographicalRegion> getSubGeographicalRegions() {
		return subGeographicalRegions;
	}

	public void setSubGeographicalRegions(
			List<SubGeographicalRegion> subGeographicalRegions) {
		this.subGeographicalRegions = subGeographicalRegions;
	}
	
	@Override
	public String toString() {
		return getDataType() + " " + super.toString();
	}
	
}
