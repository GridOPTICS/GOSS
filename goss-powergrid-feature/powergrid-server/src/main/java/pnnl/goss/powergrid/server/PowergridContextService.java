package pnnl.goss.powergrid.server;

import pnnl.goss.powergrid.datamodel.AlertContext;
import pnnl.goss.powergrid.datamodel.AlertSeverity;
import pnnl.goss.powergrid.datamodel.AlertType;
import pnnl.goss.powergrid.datamodel.PowergridTimingOptions;

public interface PowergridContextService {
	
	public final String TIMING_OPTION_OFFSET = "TIMING_OPTION_OFFSET";
	public final String TIMING_OPTION_STATIC = "TIMING_OPTION_STATIC";
	public final String TIMING_OPTION_CURRENT = "TIMING_OPTION_CURRENT";
	
	/**
	 * Save timing option saves the way the powergrid responds when a timestep is passed
	 * through to it.  
	 * 
	 * If the option is <code>TIMING_OPTION_CURRENT</code> then the default
	 * behaviour is used.  
	 * 
	 * If <code>TIMING_OPTION_OFFSET</code> is used then any offset 
	 * (specified in argvalue) is added to the current/passed timestep in the powergrid.
	 * 
	 * If <code>TIMING_OPTION_STATIC</code> is used then no matter what function is
	 * called the powergrid will only return the timestamp specified in argvalue.
	 * 
	 * @param timingOption
	 * @param argvalue
	 * @return
	 */
	public boolean saveTimingOptions(PowergridTimingOptions timingOption);
	
	public PowergridTimingOptions getPowergridTimingOptions();
	
	/**
	 * Retrieves an <code>AlertContext</code> object from storage.
	 * 
	 * @param powergridId
	 * @return
	 */
	public AlertContext getAlertContext(int powergridId);
	
	/**
	 * Retrieves an <code>AlertContext</code> object from storage.
	 * 
	 * @param powergridName
	 * @return
	 */
	public AlertContext getAlertContext(String powergridName);
	

	/**
	 * Save the alert context item to storage.
	 * 
	 * @param powergridId
	 * @param alertType
	 * @param alertSeverity
	 * @param measuredProperty
	 * @param alertLevel
	 * @return
	 */
	public boolean SaveAlertContextItem(int powergridId, AlertType alertType, AlertSeverity alertSeverity, String measuredProperty, double alertLevel);
	
	/**
	 * Save the alert context item to storage.
	 * 
	 * @param powergridMrid
	 * @param alertType
	 * @param alertSeverity
	 * @param measuredProperty
	 * @param alertLevel
	 * @return
	 */
	public boolean SaveAlertContextItem(String powergridMrid, AlertType alertType, AlertSeverity alertSeverity, String measuredProperty, double alertLevel);
}
