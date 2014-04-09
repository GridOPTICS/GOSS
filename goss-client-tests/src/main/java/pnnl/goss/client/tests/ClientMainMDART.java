package pnnl.goss.client.tests;


import java.text.SimpleDateFormat;
import java.util.Date;

import pnnl.goss.client.tests.util.ClientAuthHelper;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.mdart.common.requests.RequestPIRecords;

public class ClientMainMDART {
	
	public static void main(String[] args){
		
		try{
		
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			String start = "11/15/2011 6:42:00 AM";
			String end = "11/15/2011 6:43:00 AM";
		 	Date startDate = formatter.parse(start);
			Date endDate = formatter.parse(end);
			
			GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
			RequestPIRecords request = new RequestPIRecords(startDate, endDate);
			
			DataResponse response = (DataResponse)client.getResponse(request);
			
			if(response.getData() instanceof String[][]){
				String[][] values  = (String[][])response.getData();
				System.out.println("Rows = "+values.length);
				System.out.println("Columns = "+values[0].length);
			}
			else if(response.getData() instanceof DataError){
				DataError error = (DataError)response.getData();
				System.out.println(error.getMessage());
			}
			
			client.close();
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	

}
