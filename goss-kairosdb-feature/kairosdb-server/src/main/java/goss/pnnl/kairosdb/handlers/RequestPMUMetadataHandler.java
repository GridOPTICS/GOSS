package goss.pnnl.kairosdb.handlers;

import java.util.ArrayList;

import org.kairosdb.client.HttpClient;
import org.kairosdb.client.response.GetResponse;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestPMUMetadataHandler  extends GossRequestHandler{

	@Override
	public Response handle(Request request) {
		
		ArrayList<String> channels = new ArrayList<String>();
		DataResponse dataResponse = new DataResponse();
		
		try{
		
			//HttpClient client = new HttpClient("eioc-goss", 8020);
			HttpClient client = new HttpClient("localhost", 8080);
			GetResponse response = client.getMetricNames();
	
			for (String name : response.getResults())
			{
				if(name.contains("phasor"))
					channels.add(name);
			}
			client.shutdown();
			dataResponse.setData(channels);
			
		}
		catch(Exception e){
			e.printStackTrace();
			dataResponse.setData(new DataError(e.getMessage()));
		}
		
		
		return dataResponse;
	}
	
	
	
	

}
