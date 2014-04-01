package pnnl.goss.fncs.common.datamodel;

import pnnl.goss.core.Event;

public class SimEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1962993549035537429L;
	
	private String contents;
	
	
	public SimEvent(String simName,byte[] data){
		super.setId(500);
		super.setDescription(simName);
	
		contents=new String(data);
		contents=contents.trim();
	}
	
	@Override
	public String toString() {
		return "Event from simulator " + this.description + ", json " + contents;
	}

	public String getContents(){
		return contents;
	}
	
}
