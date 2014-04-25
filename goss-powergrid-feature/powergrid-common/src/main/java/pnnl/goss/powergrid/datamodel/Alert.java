package pnnl.goss.powergrid.datamodel;

import java.io.Serializable;

public class Alert implements Serializable{
	
	private static final long serialVersionUID = -5061543953868374068L;
	final AlertType alertType;
	final AlertSeverity alertSeverity;
	final String violatingMrid;
	final double violatingValue;
	final String contingencyName;
	

	public Alert(AlertType alertType, AlertSeverity alertSeverity, String violatingMrid, double violationValue, String contingencyName){
		this.alertType = alertType;
		this.alertSeverity = alertSeverity;
		this.violatingMrid = violatingMrid;
		this.violatingValue = violationValue;
		this.contingencyName = contingencyName;
	}
	
	/**
	 * @return the contingencyName
	 */
	public boolean isContingencyAlert(){
		return contingencyName != null && !contingencyName.isEmpty(); 
	}
	
	/**
	 * @return the contingencyName
	 */
	public String getContingencyName() {
		return contingencyName;
	}
	
	/**
	 * @return the alertType
	 */
	public AlertType getAlertType() {
		return alertType;
	}

	/**
	 * @return the alertSeverity
	 */
	public AlertSeverity getAlertSeverity() {
		return alertSeverity;
	}

	/**
	 * @return the violatingMrid
	 */
	public String getMrid() {
		return violatingMrid;
	}

	/**
	 * @return the violatingValue
	 */
	public double getValue() {
		return violatingValue;
	}

	
}
