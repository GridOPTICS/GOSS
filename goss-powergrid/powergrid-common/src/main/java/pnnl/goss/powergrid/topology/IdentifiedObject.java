package pnnl.goss.powergrid.topology;

import static pnnl.goss.powergrid.topology.NodeBreakerDataType.ALIAS;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.DESCRIPTION;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.MRID;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.NAME;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.PATH;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class IdentifiedObject implements Serializable {
		
	private static final long serialVersionUID = 7566807338923877903L;

	@Id
	@Column(name=MRID)
	protected String mrid;
	
	@Column(name=NAME)
	protected String name;
	
	@Column(name=ALIAS)
	protected String alias;

	@Column(name=PATH)
	protected String path;
	
	@Column(name=DESCRIPTION)
	protected String description;
	
	public IdentifiedObject(){

	}
	
	public IdentifiedObject(String mrid, String name, String alias, 
			String path, String description){
		this.mrid = mrid;
		this.alias = alias;
		this.description = description;
		this.path =path;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMrid() {
		return mrid;
	}

	public void setMrid(String mrid) {
		this.mrid = mrid;
	}	
	
	@Override
	public String toString() {
		String data = "MRID: "+mrid
				+ " ALIAS: "+alias
				+ " NAME: "+name
				+ " PATH: "+path
				+ " DESCRIPTION: " + description;
		return data;
	}
}
