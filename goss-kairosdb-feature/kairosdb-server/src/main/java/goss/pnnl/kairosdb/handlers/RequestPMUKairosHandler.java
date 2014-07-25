package goss.pnnl.kairosdb.handlers;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.DataPoint;
import org.kairosdb.client.builder.DoubleDataPoint;
import org.kairosdb.client.builder.LongDataPoint;
import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.response.QueryResponse;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.kairosdb.requests.RequestPMUKairos;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestPMUKairosHandler extends GossRequestHandler{

	@Override
	public Response handle(Request request) {
		DataResponse dataResponse =null;
		RequestPMUKairos request_ = (RequestPMUKairos) request;
		//HttpClient client = new HttpClient("eioc-goss", 8020);
		HttpClient client = new HttpClient("localhost", 8080);
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	Date startTime = new Date((long)request_.getStartTime()*1000);
    	Date endTime = new Date((long)request_.getEndTime()*1000);
    	ArrayList<Float> values = new ArrayList<Float>();
		try{
			QueryBuilder builder = QueryBuilder.getInstance();
	    	builder.setStart(df.parse(df.format(startTime)))
	    			.setEnd(df.parse(df.format(endTime)))
	    			.addMetric(request_.getChannel());
			
	    	QueryResponse response = client.query(builder);
	    	if(response.getErrors().size()>0){
	    		DataError error = new DataError(response.getErrors().get(0));
	    		dataResponse = new DataResponse(error);
	    		return dataResponse;
	    	}
	    	
	    	for(DataPoint dataPoint : response.getQueries().get(0).getResults().get(0).getDataPoints()){
	    		if(dataPoint instanceof DoubleDataPoint){
	    			DoubleDataPoint point = (DoubleDataPoint)dataPoint;
	    			values.add((float)point.getValue());
	    		}
	    		else if(dataPoint instanceof LongDataPoint){
	    			LongDataPoint point = (LongDataPoint)dataPoint;
	    			values.add((float)point.getValue());
	    		}
	    			    		
	    	}
	    	
		}catch(Exception e){
			e.printStackTrace();
			dataResponse = new DataResponse(new DataError(e.getCause().toString()));
			return dataResponse;
		}
		
		dataResponse  = new DataResponse(values);
		client.shutdown();
		return dataResponse;
	}
	
	

}
