package pnnl.goss.fusiondb.requests;

import pnnl.goss.core.Request;

public class RequestGeneratorData extends Request{
	
	private static final long serialVersionUID = 1777823668031781390L;
	
	int busNum;
	int genId;
	
	public RequestGeneratorData(int busNum, int genId) {
		this.busNum = busNum;
		this.genId = genId;
	}
	
	public int getBusNum() {
		return busNum;
	}
	public void setBusNum(int busNum) {
		this.busNum = busNum;
	}
	public int getGenId() {
		return genId;
	}
	public void setGenId(int genId) {
		this.genId = genId;
	}
	
}
