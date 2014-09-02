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
package pnnl.goss.gridmw.handlers;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.Response;
import pnnl.goss.gridmw.datamodel.GridMWTestData;
import pnnl.goss.gridmw.datasources.GridMW;
import pnnl.goss.gridmw.requests.RequestGridMWAsyncTest;
import pnnl.goss.gridmw.requests.RequestGridMWTest;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestGridMWTestHandler extends GossRequestHandler {

	RequestGridMWAsyncTest gridmwTestRequest;
	long startTime = 0;
	long endTime = 0;
	
	@Override
	public Response handle(Request request) {
		if(request instanceof RequestAsync)
			return asynchronousHandle(request);
		else
			return synchronousHandle(request);
	}
	
	private Response synchronousHandle(Request request){
		RequestGridMWTest gridmwTestRequest = (RequestGridMWTest) request;
		GridMWTestData data = new GridMWTestData();
		GridMW gridMW = GridMW.getInstance();
		long perfStartTime = System.currentTimeMillis();
		float[] values= new float[gridmwTestRequest.getCount()];
		try{
			values = gridMW.get(gridmwTestRequest.getTimeSeriesId(), gridmwTestRequest.getStartTime(), gridmwTestRequest.getEndTime(),gridmwTestRequest.getCount());
		}
		catch(Exception e){
			long time = System.currentTimeMillis()-perfStartTime;
			data.setValues(values);
			data.setTime(time);
			DataResponse response  = new DataResponse(data);
			return response;
		}
		long time = System.currentTimeMillis()-perfStartTime;
		data.setValues(values);
		data.setTime(time);
		DataResponse response  = new DataResponse(data);
		return response;
	}
	
	private Response asynchronousHandle(Request request){
		if(this.gridmwTestRequest==null)
			this.gridmwTestRequest = (RequestGridMWAsyncTest) request;
		if(startTime==0)
			startTime = gridmwTestRequest.getStartTime();
		else
			startTime = endTime;
		endTime = startTime+gridmwTestRequest.getSegment();
		
		GridMWTestData data = new GridMWTestData();
	//	long perfStartTime = System.currentTimeMillis();
		
		data.setBeforetime(System.currentTimeMillis());
		GridMW gridMW = GridMW.getInstance();
		float[] values= new float[gridmwTestRequest.getCount()];
		try{
			values = gridMW.get(gridmwTestRequest.getTimeSeriesId(), startTime, endTime,gridmwTestRequest.getCount());
		}
		catch(Exception e){
			DataError error = new DataError(e.getCause().toString());
			DataResponse response  = new DataResponse(error);
			return response;
		}
		//long time = System.currentTimeMillis()-perfStartTime;
		data.setValues(values);
		data.setTime(System.currentTimeMillis());
		DataResponse response  = new DataResponse(data);
		if(endTime==gridmwTestRequest.getEndTime())
			response.setResponseComplete(true);
		else
			response.setResponseComplete(false);
		return response;
	}
	
}
