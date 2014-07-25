package pnnl.goss.fusiondb.datamodel;

import java.io.Serializable;

public class GeneratorData implements Serializable{

	private static final long serialVersionUID = -8815426950174955745L;
	
    int busNum;
    double genMW;
    double genMVR;
    double genMVRMax;
    double genMVRMin;
    double genVoltSet;
    String genId;
    String genStatus;
    double genMWMax;
    double genMWMin;
    
    public GeneratorData(int busNum, double genMW, double genMVR,
			double genMVRMax, double genMVRMin, double genVoltSet, String genId,
			String genStatus, double genMWMax, double genMWMin) {
    	
		this.busNum = busNum;
		this.genMW = genMW;
		this.genMVR = genMVR;
		this.genMVRMax = genMVRMax;
		this.genMVRMin = genMVRMin;
		this.genVoltSet = genVoltSet;
		this.genId = genId;
		this.genStatus = genStatus;
		this.genMWMax = genMWMax;
		this.genMWMin = genMWMin;
	}
    
	public int getBusNum() {
		return busNum;
	}
	public void setBusNum(int busNum) {
		this.busNum = busNum;
	}
	public double getGenMW() {
		return genMW;
	}
	public void setGenMW(double genMW) {
		this.genMW = genMW;
	}
	public double getGenMVR() {
		return genMVR;
	}
	public void setGenMVR(double genMVR) {
		this.genMVR = genMVR;
	}
	public double getGenMVRMax() {
		return genMVRMax;
	}
	public void setGenMVRMax(double genMVRMax) {
		this.genMVRMax = genMVRMax;
	}
	public double getGenMVRMin() {
		return genMVRMin;
	}
	public void setGenMVRMin(double genMVRMin) {
		this.genMVRMin = genMVRMin;
	}
	public double getGenVoltSet() {
		return genVoltSet;
	}
	public void setGenVoltSet(double genVoltSet) {
		this.genVoltSet = genVoltSet;
	}
	public String getGenId() {
		return genId;
	}
	public void setGenId(String genId) {
		this.genId = genId;
	}
	public String getGenStatus() {
		return genStatus;
	}
	public void setGenStatus(String genStatus) {
		this.genStatus = genStatus;
	}
	public double getGenMWMax() {
		return genMWMax;
	}
	public void setGenMWMax(double genMWMax) {
		this.genMWMax = genMWMax;
	}
	public double getGenMWMin() {
		return genMWMin;
	}
	public void setGenMWMin(double genMWMin) {
		this.genMWMin = genMWMin;
	}
    
}
