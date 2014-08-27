package pnnl.goss.kairosdb.handlers;

import java.util.ArrayList;

import org.kairosdb.client.HttpClient;
import org.kairosdb.client.response.GetResponse;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.server.core.GossRequestHandler;
import static pnnl.goss.kairosdb.KairosDBServerActivator.PROP_KAIROSDB_HOST;
import static pnnl.goss.kairosdb.KairosDBServerActivator.PROP_KAIROSDB_PORT;

public class RequestPMUMetadataHandler  extends GossRequestHandler{

	@Override
	public Response handle(Request request) {
		
		String hostname = (String)this.dataservices.getDataService(PROP_KAIROSDB_HOST);
		int port = Integer.parseInt(this.dataservices.getDataService(PROP_KAIROSDB_PORT).toString());
		
		ArrayList<String> channels = new ArrayList<String>();
		DataResponse dataResponse = new DataResponse();
		
		try{
		
			HttpClient client = new HttpClient(hostname, port);
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
