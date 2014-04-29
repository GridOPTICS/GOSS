package pnnl.goss.dsa;

import java.util.List;

import pnnl.goss.powergrid.datamodel.Alert;
import pnnl.goss.powergrid.datamodel.AlertContext;

public interface GetAlertsSei {
	
	public AlertContext getAlertContext();
	
	public List<Alert> getAlerts(String timestep);
	
}
