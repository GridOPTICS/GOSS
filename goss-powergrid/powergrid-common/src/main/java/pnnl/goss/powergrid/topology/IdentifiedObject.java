package pnnl.goss.powergrid.topology;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class IdentifiedObject implements Serializable{
	
	private static final long serialVersionUID = -7611977120083071422L;

	protected String identMrid;
	
	protected String identDataType;
	
	protected String identName;
	
	protected String identAlias;

	protected String identPathName;
	
	protected String identDescription;
	
	public IdentifiedObject(){

	}
	
	public IdentifiedObject(String mrid, String dataType, String identName, String identAlias, 
			String identPath, String identDescription){
		this.identMrid = mrid;
		this.identDataType = dataType;
		this.identAlias = identAlias;
		this.identDescription = identDescription;
		this.identPathName =identPath;
		this.identName = identName;
	}
	
	public String getIdentName() {
		return identName;
	}
	public void setIdentName(String identName) {
		this.identName = identName;
	}
	public String getIdentAlias() {
		return identAlias;
	}
	public void setIdentAlias(String identAlias) {
		this.identAlias = identAlias;
	}
	public String getIdentPathName() {
		return identPathName;
	}
	public void setIdentPathName(String identPathName) {
		this.identPathName = identPathName;
	}

	public String getIdentDescription() {
		return identDescription;
	}

	public void setIdentDescription(String identDescription) {
		this.identDescription = identDescription;
	}

	public String getIdentMrid() {
		return identMrid;
	}

	public void setIdentMrid(String identMrid) {
		this.identMrid = identMrid;
	}

	public String getIdentDataType() {
		return identDataType;
	}

	public void setIdentDataType(String identDataType) {
		this.identDataType = identDataType;
	}
	
}
