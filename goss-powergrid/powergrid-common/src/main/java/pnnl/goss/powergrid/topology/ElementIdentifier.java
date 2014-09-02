package pnnl.goss.powergrid.topology;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ElementIdentifier  implements Serializable{
	
	private String mrid;
	
	private String dataType;
	
	public ElementIdentifier(){
		
	}
	
	public ElementIdentifier(String mrid, String dataType){
		this.mrid = mrid;
		this.dataType = dataType;
	}
	
	public String getMrid() {
		return mrid;
	}
	public void setMrid(String mrid) {
		this.mrid = mrid;
	}
	public String  getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
