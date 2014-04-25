package pnnl.goss.powergrid.datamodel;

import java.io.Serializable;
import java.util.HashMap;

public class AlertContext implements Serializable {
	
	private static final long serialVersionUID = 941584033189627086L;
	HashMap<AlertType, HashMap<AlertSeverity, Double>> alertContext = new HashMap<AlertType, HashMap<AlertSeverity, Double>>();
	HashMap<AlertType, HashMap<AlertSeverity, String>> measuredProperties = new HashMap<AlertType, HashMap<AlertSeverity, String>>();
		
	public void setAlertContext(AlertType alertType, AlertSeverity severity, double threshold, String measuredProperty){
		if(!alertContext.containsKey(alertType)){
			alertContext.put(alertType, new HashMap<AlertSeverity, Double>());
		}
		
		if(!measuredProperties.containsKey(alertType)){
			measuredProperties.put(alertType, new HashMap<AlertSeverity, String>());
		}
		
		alertContext.get(alertType).put(severity, threshold);
		measuredProperties.get(alertType).put(severity, measuredProperty);
	}
	
	public HashMap<AlertSeverity, Double> getSeverityLevels(AlertType alertType){
		return alertContext.get(alertType);
	}
	
	public HashMap<AlertSeverity, Double> getMeasuredProperty(AlertType alertType){
		return alertContext.get(alertType);
	}
	
}
