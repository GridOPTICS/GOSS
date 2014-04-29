package pnnl.goss.sharedperspective.common.requests;

import pnnl.goss.core.Request;

public class RequestAlerts extends Request{

	static final long serialVersionUID = -8497717764528999383L;
	
	final String powergridName;
	final String timestep;
		
	public RequestAlerts(String powergridName, String timestep) {
		this.powergridName = powergridName;
		this.timestep = timestep;
	}

	/**
	 * @return the powergridName
	 */
	public String getPowergridName() {
		return powergridName;
	}

	/**
	 * @return the timestep
	 */
	public String getTimestep() {
		return timestep;
	}

}
