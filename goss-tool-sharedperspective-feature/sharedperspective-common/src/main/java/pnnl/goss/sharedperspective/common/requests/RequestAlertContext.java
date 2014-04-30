package pnnl.goss.sharedperspective.common.requests;

import pnnl.goss.core.Request;

public class RequestAlertContext extends Request {

	private static final long serialVersionUID = 5278867473919738078L;

	private String powergridName;

	/**
	 * @return the powergridName
	 */
	public String getPowergridName() {
		return powergridName;
	}

	/**
	 * @param powergridName the powergridName to set
	 */
	public void setPowergridName(String powergridName) {
		this.powergridName = powergridName;
	}
	
	
}
