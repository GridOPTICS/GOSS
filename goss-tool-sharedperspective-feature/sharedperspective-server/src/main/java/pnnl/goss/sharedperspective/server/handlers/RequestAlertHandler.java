package pnnl.goss.sharedperspective.server.handlers;

import java.io.Serializable;
import java.util.List;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.datamodel.Alert;
import pnnl.goss.powergrid.datamodel.AlertContext;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.server.core.GossRequestHandler;
import pnnl.goss.sharedperspective.common.requests.RequestAlertContext;
import pnnl.goss.sharedperspective.common.requests.RequestAlerts;
import pnnl.goss.sharedperspective.dao.PowergridSharedPerspectiveDaoMySql;

public class RequestAlertHandler extends GossRequestHandler {
	
	private List<Alert> getAlerts(RequestAlerts alertsRequest) throws Exception{
		String dsName = PowergridDataSources.instance().getDatasourceKeyWherePowergridName(new PowergridDaoMySql(), alertsRequest.getPowergridName());
		PowergridSharedPerspectiveDaoMySql dao = new PowergridSharedPerspectiveDaoMySql(PowergridDataSources.instance().getConnectionPool(dsName));
		
		int pgid =dao.getPowergridId(alertsRequest.getPowergridName());
		PowergridModel model = dao.getPowergridModelAtTime(pgid, alertsRequest.getTimestep());
		return model.getAlerts();	
	}
	
	private AlertContext getAlertContext(RequestAlertContext request) throws Exception{
		String dsName = PowergridDataSources.instance().getDatasourceKeyWherePowergridName(new PowergridDaoMySql(), request.getPowergridName());
		PowergridSharedPerspectiveDaoMySql dao = new PowergridSharedPerspectiveDaoMySql(PowergridDataSources.instance().getConnectionPool(dsName));
		
		int pgid =dao.getPowergridId(request.getPowergridName());
		return dao.getAlertContext(pgid);
	}
	
		
	@Override
	public Response handle(Request request) {
		DataResponse dataResponse = new DataResponse();
		try{
			if (request instanceof RequestAlertContext){
				dataResponse.setData(getAlertContext((RequestAlertContext)request));
			}
			else if (request instanceof RequestAlerts){
				dataResponse.setData((Serializable) getAlerts((RequestAlerts)request));
			}
		}
		catch(Exception e){
			dataResponse.setData(new DataError(e.getMessage()));
			e.printStackTrace();
			return dataResponse;
		}
		
		return dataResponse;
	}

}
