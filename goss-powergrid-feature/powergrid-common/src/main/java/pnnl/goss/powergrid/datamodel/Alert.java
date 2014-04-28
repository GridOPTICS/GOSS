package pnnl.goss.powergrid.datamodel;

import java.io.Serializable;

public class Alert implements Serializable{
	
	private static final long serialVersionUID = -5061543953868374068L;
	private AlertType alertType;
	private AlertSeverity alertSeverity;
	private String violationMrid;
	private double violationValue;
	private String contingencyName;
	
	public Alert(){
		
	}
	
	public Alert(AlertType alertType, AlertSeverity alertSeverity, String violatingMrid, double violationValue, String contingencyName){
		this.alertType = alertType;
		this.alertSeverity = alertSeverity;
		this.violationMrid = violatingMrid;
		this.violationValue = violationValue;
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
	 * @param alertType the alertType to set
	 */
	public void setAlertType(AlertType alertType) {
		this.alertType = alertType;
	}

	/**
	 * @param alertSeverity the alertSeverity to set
	 */
	public void setAlertSeverity(AlertSeverity alertSeverity) {
		this.alertSeverity = alertSeverity;
	}


	/**
	 * @param contingencyName the contingencyName to set
	 */
	public void setContingencyName(String contingencyName) {
		this.contingencyName = contingencyName;
	}

	/**
	 * @return the violationMrid
	 */
	public String getViolationMrid() {
		return violationMrid;
	}

	/**
	 * @param violationMrid the violationMrid to set
	 */
	public void setViolationMrid(String violationMrid) {
		this.violationMrid = violationMrid;
	}

	/**
	 * @return the violationValue
	 */
	public double getViolationValue() {
		return violationValue;
	}

	/**
	 * @param violationValue the violationValue to set
	 */
	public void setViolationValue(double violationValue) {
		this.violationValue = violationValue;
	}

	
}
