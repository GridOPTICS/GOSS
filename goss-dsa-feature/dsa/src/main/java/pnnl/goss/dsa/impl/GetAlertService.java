package pnnl.goss.dsa.impl;

import java.util.List;

import javax.jws.WebService;

import pnnl.goss.dsa.DsaActivator;
import pnnl.goss.dsa.GetAlertsSei;
import pnnl.goss.powergrid.datamodel.Alert;
import pnnl.goss.powergrid.datamodel.AlertContext;

@WebService(serviceName="GetAlertService", 
	portName="AlertPort", 
	name="Alerts",
targetNamespace="http://alerts.ws.dsa.fpgi.pnnl.gov/")
public class GetAlertService implements GetAlertsSei {

	@Override
	public AlertContext getAlertContext() {
		GridOpticsServiceImpl gossService = new GridOpticsServiceImpl();
		gossService.setPowerGridName(DsaActivator.getPowergridName());
		return gossService.getAlertContext();
	}

	@Override
	public List<Alert> getAlerts(String timestep) {
		GridOpticsServiceImpl gossService = new GridOpticsServiceImpl();
		gossService.setPowerGridName(DsaActivator.getPowergridName());
		return gossService.getAlerts(timestep);
	}

}
