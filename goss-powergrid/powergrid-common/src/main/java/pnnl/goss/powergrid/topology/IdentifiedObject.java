package pnnl.goss.powergrid.topology;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class IdentifiedObject {
		
	@Id
	protected String mrid;
	
	@Column
	protected String name;
	
	@Column
	protected String alias;

	@Column
	protected String path;
	
	@Column
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
}
