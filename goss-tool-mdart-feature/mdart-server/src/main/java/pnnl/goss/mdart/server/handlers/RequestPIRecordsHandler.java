package pnnl.goss.mdart.server.handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.mdart.common.requests.RequestPIRecords;
import pnnl.goss.mdart.server.datasources.MDARTDataSource;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestPIRecordsHandler extends GossRequestHandler{

	@Override
	public Response handle(Request request) {
		
		
		DataResponse response  = new DataResponse();
		Connection connection= MDARTDataSource.getInstance().getConnection();
		
		try{
			RequestPIRecords requestPIRecords = (RequestPIRecords)request;
			
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			
			Date startDate = requestPIRecords.getStartTime();
			Date endDate = requestPIRecords.getEndTime();
			
			String query = "select * from pi_records where DATE_TIME between str_to_date('"+formatter.format(startDate)+"','%m/%d/%Y %r') "+
								" and str_to_date('"+formatter.format(endDate)+"','%m/%d/%Y %r')";
			
			
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			rs.last();
			int rows = rs.getRow();
			int cols = rs.getMetaData().getColumnCount();
			rs.beforeFirst();
			
			String[][] values = new String[rows][cols];
		
			int row=0;
			while(rs.next()){
				for(int col=1; col<=cols;col++){
					values[row][col-1] = rs.getString(col);
				}
				row++;
			}
			
			response.setData(values);
			
		}
		catch(Exception e){
			try{
				if(connection !=null)
					connection.close();
			}
			catch(Exception ee){
				ee.printStackTrace();
			}
			e.printStackTrace();
			response.setData(new DataError(e.getMessage()));
			return response;
		}
		
		return response;
	}

}
