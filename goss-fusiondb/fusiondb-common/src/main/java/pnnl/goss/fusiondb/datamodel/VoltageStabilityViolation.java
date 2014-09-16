package pnnl.goss.fusiondb.datamodel;

import java.io.Serializable;

public class VoltageStabilityViolation implements Serializable {

	private static final long serialVersionUID = -2364245662049846190L;
	
	String timestamp;
	int intervalId;
	int busId;
	double probability;
	
	public VoltageStabilityViolation(String timestamp, int intervalId, int busId, double probability) {
		this.timestamp = timestamp;
		this.intervalId = intervalId;
		this.busId = busId;
		this.probability = probability;
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
	public int getBusId() {
		return busId;
	}
	public void setBusId(int busId) {
		this.busId = busId;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}

}
