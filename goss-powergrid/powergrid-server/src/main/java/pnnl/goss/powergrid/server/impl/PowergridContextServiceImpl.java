package pnnl.goss.powergrid.server.impl;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import pnnl.goss.powergrid.datamodel.AlertContext;
import pnnl.goss.powergrid.datamodel.AlertSeverity;
import pnnl.goss.powergrid.datamodel.AlertType;
import pnnl.goss.powergrid.datamodel.PowergridTimingOptions;
import pnnl.goss.powergrid.server.PowergridContextService;

public class PowergridContextServiceImpl implements PowergridContextService, ManagedService {
	
	private PowergridTimingOptions timingOption = null;

	public PowergridContextServiceImpl(){
		timingOption = new PowergridTimingOptions(TIMING_OPTION_CURRENT, null);
	}
	
	@Override
	public boolean SaveAlertContextItem(int powergridId, AlertType alertType, AlertSeverity alertSeverity, String measuredProperty, double alertLevel) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean SaveAlertContextItem(String powergridMrid, AlertType alertType, AlertSeverity alertSeverity, String measuredProperty, double alertLevel) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlertContext getAlertContext(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlertContext getAlertContext(String powergridName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		// TODO add updated properties for powergrid.
		
	}

	@Override
	public boolean saveTimingOptions(PowergridTimingOptions timingOption) {
		this.timingOption = timingOption;
		return true;
	}

	@Override
	public PowergridTimingOptions getPowergridTimingOptions() {
		return this.timingOption;
	}


}
