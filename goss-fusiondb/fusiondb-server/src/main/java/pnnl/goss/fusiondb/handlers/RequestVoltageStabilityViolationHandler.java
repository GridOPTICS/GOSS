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
import pnnl.goss.fusiondb.datamodel.VoltageStabilityViolation;
import pnnl.goss.fusiondb.requests.RequestVoltageStabilityViolation;
import pnnl.goss.server.core.AbstractGossRequestHandler;

public class RequestVoltageStabilityViolationHandler extends AbstractGossRequestHandler {

	@Override
	public Response handle(Request request) {
		DataResponse response = new DataResponse();
		Connection connection = this.dataservices.getPooledConnection(PROP_FUSIONDB_DATASERVICE);
		try{

			RequestVoltageStabilityViolation request1 = (RequestVoltageStabilityViolation)request;
			Statement stmt = connection.createStatement();
			ResultSet rs = null;
			String query=null;
			
			if(request1.getIntervalId()!=0)
				query = "select * from voltage_stability_violation where `timestamp` = '"+request1.getTimestamp()+"' and interval_id = "+request1.getIntervalId();
			else
				query = "select * from voltage_stability_violation where `timestamp` = '"+request1.getTimestamp()+"' order by interval_id";

			System.out.println(query);
			rs = stmt.executeQuery(query);

			ArrayList<VoltageStabilityViolation> list = new ArrayList<VoltageStabilityViolation>();
			VoltageStabilityViolation voltageStabilityViolation=null;
			while (rs.next()) {
				String timestamp = rs.getString("timestamp");
				int intervalId = rs.getInt("interval_id");
				int busId = rs.getInt("bus_id");
				double probability = rs.getDouble("probability");
				voltageStabilityViolation = new VoltageStabilityViolation(timestamp,intervalId, busId, probability);
				list.add(voltageStabilityViolation);
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
