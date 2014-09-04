package pnnl.goss.fusiondb.requests;

import pnnl.goss.core.Request;

public class RequestVoltageStabilityViolation extends Request {

	private static final long serialVersionUID = -7868024161030897814L;
	
	String timestamp;
	int intervalId;
	
	public RequestVoltageStabilityViolation(String timestamp, int intervalId){
		this.timestamp = timestamp;
		this.intervalId = intervalId;
	}
	
	public RequestVoltageStabilityViolation(String timestamp){
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getIntervalId() {
		return intervalId;
	}

	public void setIntervalId(int intervalId) {
		this.intervalId = intervalId;
	}

}
