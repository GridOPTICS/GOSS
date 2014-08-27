/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.kairosdb.handlers;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.DataPoint;
import org.kairosdb.client.builder.LongDataPoint;
import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.response.QueryResponse;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.Response;
import pnnl.goss.kairosdb.datamodel.KairosTestData;
import pnnl.goss.kairosdb.requests.RequestKairosAsyncTest;
import pnnl.goss.kairosdb.requests.RequestKairosTest;
import pnnl.goss.server.core.GossRequestHandler;

import static pnnl.goss.kairosdb.KairosDBServerActivator.PROP_KAIROSDB_HOST;
import static pnnl.goss.kairosdb.KairosDBServerActivator.PROP_KAIROSDB_PORT;

public class RequestKairosTestHandler extends GossRequestHandler {
	
	HttpClient client = null;
	RequestKairosAsyncTest testRequest;
	Date startTime = null;
	Date endTime = null;
	
	@Override
	public Response handle(Request request) {
		if(request instanceof RequestAsync)
			return asynchronousHandle(request);
		else
			return synchronousHandle(request);
	}
	
	private Response synchronousHandle(Request request){
		
		String hostname = (String)this.dataservices.getDataService(PROP_KAIROSDB_HOST);
		int port = Integer.parseInt(this.dataservices.getDataService(PROP_KAIROSDB_PORT).toString());
				
		DataResponse dataResponse =null;
		RequestKairosTest testRequest = (RequestKairosTest) request;
		KairosTestData data = new KairosTestData();
		long ds1 = System.nanoTime();
		data.setDS1(ds1);
		client = new HttpClient(hostname, port);
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	Date startTime = new Date((long)testRequest.getStartTime()*1000);
    	Date endTime = new Date((long)testRequest.getEndTime()*1000);
    	List<Float> values = new ArrayList<Float>();
		try{
			QueryBuilder builder = QueryBuilder.getInstance();
	    	builder.setStart(df.parse(df.format(startTime)))
	    			.setEnd(df.parse(df.format(endTime)))
	    			.addMetric("test_flag");
			
	    	QueryResponse response = client.query(builder);
	    	if(response.getErrors().size()>0){
	    		DataError error = new DataError(response.getErrors().get(0));
	    		dataResponse = new DataResponse(error);
	    		return dataResponse;
	    	}
	    	
	    	for(DataPoint dataPoint : response.getQueries().get(0).getResults().get(0).getDataPoints()){
	    		LongDataPoint point = (LongDataPoint)dataPoint;
	    		values.add((float)point.getValue());
	    	}
	    	
		}catch(Exception e){
			e.printStackTrace();
			dataResponse = new DataResponse(new DataError(e.getCause().toString()));
			return dataResponse;
		}
		
		data.setDS2(System.nanoTime());
		data.setValues(values.toArray(new Float[values.size()]));
		dataResponse  = new DataResponse(data);
		client.shutdown();
		return dataResponse;
	}
	
	private Response asynchronousHandle(Request request){
		
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	List<Float> values = new ArrayList<Float>();
		
    	DataResponse dataResponse = null;
    	if(client==null)
    		client = new HttpClient("we22743", 8080);
    	
		if(this.testRequest==null)
			this.testRequest = (RequestKairosAsyncTest) request;
		if(startTime==null)
			startTime = new Date((long)testRequest.getStartTime()*1000);
		else
			startTime = endTime;
		endTime = new Date(startTime.getTime()+((long)testRequest.getSegment()*1000));
		
		KairosTestData data = new KairosTestData();

		data.setDS1(System.nanoTime());
		try{
			
			QueryBuilder builder = QueryBuilder.getInstance();
	    	builder.setStart(df.parse(df.format(startTime)))
	    			.setEnd(df.parse(df.format(endTime)))
	    			.addMetric(testRequest.getPmuId()+"_"+testRequest.getChannel());
			
	    	QueryResponse response = client.query(builder);
	    	if(response.getErrors().size()>0){
	    		DataError error = new DataError(response.getErrors().get(0));
	    		dataResponse = new DataResponse(error);
	    		return dataResponse;
	    	}
	    	for(DataPoint dataPoint : response.getQueries().get(0).getResults().get(0).getDataPoints()){
	    		LongDataPoint point = (LongDataPoint)dataPoint;
	    		values.add((float)point.getValue());
	    	}
		}
		catch(Exception e){
			DataError error = new DataError(e.getCause().toString());
			dataResponse  = new DataResponse(error);
			return dataResponse;
		}
		data.setValues(values.toArray(new Float[values.size()]));
		data.setDS2(System.nanoTime());
		dataResponse  = new DataResponse(data);
		if(endTime.getTime()>=(testRequest.getEndTime()*1000)){
			dataResponse.setResponseComplete(true);
			if(client!=null)
				client.shutdown();
		}
		else
			dataResponse.setResponseComplete(false);
		return dataResponse;
		
	}
	
}
