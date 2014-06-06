package pnnl.goss.kairosdb.requests;

import pnnl.goss.core.Request;

public class RequestPMUKairos extends Request{
	
	String channel;
	long startTime;
	long endTime;
	
	public RequestPMUKairos(String channel, long startTime, long endTime){
		this.channel = channel;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	
	

}
