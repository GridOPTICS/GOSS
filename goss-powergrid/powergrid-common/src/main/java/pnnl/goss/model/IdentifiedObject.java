package pnnl.goss.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

public class IdentifiedObject {
	
	private String identName;
	
	private String identAlias;

	private String identPathName;
	
	private String identDescription;
	
	public IdentifiedObject(){

	}
	
	public IdentifiedObject(String identName, String identAlias, String identPath,
			String identDescription){
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
	
}
