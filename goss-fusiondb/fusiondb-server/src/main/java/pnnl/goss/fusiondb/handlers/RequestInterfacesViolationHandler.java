package pnnl.goss.fusiondb.handlers;

import static pnnl.goss.fusiondb.FusionDBServer.PROP_FUSIONDB_DATASERVICE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.fusiondb.datamodel.InterfacesViolation;
import pnnl.goss.fusiondb.requests.RequestInterfacesViolation;
import pnnl.goss.server.annotations.RequestHandler;
import pnnl.goss.server.core.AbstractGossRequestHandler;

@RequestHandler(requests=RequestInterfacesViolation.class)
public class RequestInterfacesViolationHandler extends AbstractGossRequestHandler {

	@Override
	public Response handle(Request request) {

		DataResponse response = new DataResponse();
		Connection connection = this.dataservices.getPooledConnection(PROP_FUSIONDB_DATASERVICE);
		try{

			RequestInterfacesViolation request1 = (RequestInterfacesViolation)request;
			Statement stmt = connection.createStatement();
			ResultSet rs = null;
			String query=null;
			
			if(request1.getIntervalId()!=0)
				query = "select * from interfaces_violation where `timestamp` = '"+request1.getTimestamp()+"' and interval_id = "+request1.getIntervalId();
			else
				query = "select * from interfaces_violation where `timestamp` = '"+request1.getTimestamp()+"' order by interval_id";

			System.out.println(query);
			rs = stmt.executeQuery(query);

			ArrayList<InterfacesViolation> list = new ArrayList<InterfacesViolation>();
			InterfacesViolation interfacesViolation=null;
			while (rs.next()) {
				String timestamp = rs.getString("timestamp");
				int intervalId = rs.getInt("interval_id");
				int interface_id = rs.getInt("interface_id");
				double probability = rs.getDouble("probability");
				interfacesViolation = new InterfacesViolation(timestamp,intervalId, interface_id, probability);
				list.add(interfacesViolation);
			}
			
			response.setData(list);
			connection.close();

		}
		catch(Exception e){
			e.printStackTrace();
			DataError error = new DataError(e.getMessage());
			response.setData(error);
			return response;
		}
		return response;
	}


}
