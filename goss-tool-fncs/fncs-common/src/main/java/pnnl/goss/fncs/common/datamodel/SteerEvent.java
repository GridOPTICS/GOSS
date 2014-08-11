package pnnl.goss.fncs.common.datamodel;

import pnnl.goss.core.Event;



public class SteerEvent extends Event{

	/**
	 * 
	 */
	private static final long serialVersionUID = 12231L;

	private String contents;
	
	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public SteerEvent(){
		contents="";
	}
	
	public SteerEvent(String given){
		super.setId(501);
		super.setDescription("SteerEvent");
	
		contents=contents.trim();
	}
	
	@Override
	public String toString() {
		return "Steer event"+contents;
	}
}
