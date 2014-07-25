package goss.pnnl.fusiondb.handlers;

import goss.pnnl.fusiondb.datasources.FusionDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.fusiondb.datamodel.CapacityRequirementValues;
import pnnl.goss.fusiondb.datamodel.GeneratorData;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement.Parameter;
import pnnl.goss.fusiondb.requests.RequestGeneratorData;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestGeneratorDataHandler extends GossRequestHandler {

	@Override
	public Response handle(Request request) {
		
		DataResponse response = new DataResponse();
		Connection connection = FusionDataSource.getInstance().getConnection();
		
		try{
			
			GeneratorData data= null;
			RequestGeneratorData request1 = (RequestGeneratorData)request;
			Statement stmt = connection.createStatement();
			ResultSet rs = null;
			
			String query = "select * from generator where busnum="+request1.getBusNum()+" and gen_id="+request1.getGenId();
					
			System.out.println(query);
			rs = stmt.executeQuery(query);
			
			if (rs.next()) {
				int busNum =  rs.getInt("busnum");
				double genMW = rs.getDouble("genmw");
				double genMVR = rs.getDouble("gen_mvr");
				double genMVRMax = rs.getDouble("gen_mvr_max");
				double genMVRMin = rs.getDouble("gen_mvr_min");
				double genVoltSet = rs.getDouble("gen_volt_set");
				String genId = rs.getString("gen_id");
				String genStatus = rs.getString("gen_status");
				double genMWMax = rs.getDouble("gen_mw_max");
				double genMWMin = rs.getDouble("gen_mw_min");
				data = new GeneratorData(busNum, genMW, genMVR, genMVRMax, genMVRMin, genVoltSet, genId, genStatus, genMWMax, genMWMin);
			}

			response.setData(data);
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
