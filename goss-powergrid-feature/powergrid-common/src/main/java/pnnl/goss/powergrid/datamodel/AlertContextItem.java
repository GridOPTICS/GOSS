package pnnl.goss.powergrid.datamodel;

import java.io.Serializable;

public class AlertContextItem implements Serializable {

	private static final long serialVersionUID = -6641068521282891641L;
	private AlertSeverity alertSeverity;
	private AlertType alertType;
	private double alertLevel;
	private String measuredProperty;
	
	public AlertContextItem(){
		
	}
	
	public AlertContextItem(AlertSeverity severity, AlertType type, double alertLevel, String measured){
		this.alertSeverity = severity;
		this.alertType = type;
		this.alertLevel = alertLevel;
		this.measuredProperty = measured;
	}
	
	/**
	 * @return the alertSeverity
	 */
	public AlertSeverity getAlertSeverity() {
		return alertSeverity;
	}
	/**
	 * @param alertSeverity the alertSeverity to set
	 */
	public void setAlertSeverity(AlertSeverity alertSeverity) {
		this.alertSeverity = alertSeverity;
	}
	/**
	 * @return the alertType
	 */
	public AlertType getAlertType() {
		return alertType;
	}
	/**
	 * @param alertType the alertType to set
	 */
	public void setAlertType(AlertType alertType) {
		this.alertType = alertType;
	}
	/**
	 * @return the measuredProperty
	 */
	public String getMeasuredProperty() {
		return measuredProperty;
	}
	/**
	 * @param measuredProperty the measuredProperty to set
	 */
	public void setMeasuredProperty(String measuredProperty) {
		this.measuredProperty = measuredProperty;
	}
	/**
	 * @return the alertLevel
	 */
	public double getAlertLevel() {
		return alertLevel;
	}
	/**
	 * @param alertLevel the alertLevel to set
	 */
	public void setAlertLevel(double alertLevel) {
		this.alertLevel = alertLevel;
	}
	
	
}
