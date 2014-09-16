package pnnl.goss.powergrid.topology.nodebreaker;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.DATA_TYPE;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.SUB_GEOGRAPHICAL_REGION;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.SUB_GEOGRAPHICAL_REGION_MRID;

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
@Table(name=SUB_GEOGRAPHICAL_REGION)
@AttributeOverride(name="mrid", column=@Column(name=SUB_GEOGRAPHICAL_REGION_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class SubGeographicalRegion extends IdentifiedObject implements NodeBreakerDataType, Region {

	private static final long serialVersionUID = 8429110018247304602L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	
	@Column
	protected GeographicalRegion geographicalRegion;
	
	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name=SUB_GEOGRAPHICAL_REGION_MRID)
	protected List<Line> lines;

	public SubGeographicalRegion(){
		dataType = SUB_GEOGRAPHICAL_REGION;
		lines = new ArrayList<>();
	}
	
	public void addLine(Line line){
		lines.add(line);
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public GeographicalRegion getGeographicalRegion() {
		return geographicalRegion;
	}

	public void setGeographicalRegion(GeographicalRegion geographicalRegion) {
		this.geographicalRegion = geographicalRegion;
	}

	public List<Line> getLines() {
		return lines;
	}

	public void setLines(List<Line> lines) {
		this.lines = lines;
	}
		
}
