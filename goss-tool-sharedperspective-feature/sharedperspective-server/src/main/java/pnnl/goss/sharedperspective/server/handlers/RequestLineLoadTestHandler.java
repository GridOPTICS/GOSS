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
package pnnl.goss.sharedperspective.server.handlers;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import pnnl.goss.core.Data;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.server.core.GossRequestHandler;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegmentTest;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadAsyncTest;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadTest;
import pnnl.goss.sharedperspective.dao.PowergridSharedPerspectiveDaoMySql;

public class RequestLineLoadTestHandler extends GossRequestHandler {

	Data data =null;
	Connection connection=null;
	RequestLineLoadAsyncTest requestAsync = null;
	Date startTime = null;
	Date endTime = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public Response handle(Request request){
		if(request instanceof RequestLineLoadAsyncTest)
				return asynchronousHandle((RequestLineLoadAsyncTest)request);
			else
				return synchronousHandle((RequestLineLoadTest)request);
	}
		
	private DataResponse asynchronousHandle(RequestLineLoadAsyncTest request){
		
		if(this.requestAsync==null)
			this.requestAsync = request;
		if(startTime==null)
			startTime = request.getStartTime();
		else
			startTime = endTime;
		
		long segment = startTime.getTime();
		segment = segment + request.getSegment()*1000;
		endTime.setTime(segment);
		
		DataResponse dataResponse = new DataResponse();
		ACLineSegmentTest data =null;
		try{
			long time = System.currentTimeMillis();
			String dsName = PowergridDataSources.instance().getDatasourceKeyWherePowergridName(new PowergridDaoMySql(), request.getPowergridName());
			PowergridSharedPerspectiveDaoMySql dao = new PowergridSharedPerspectiveDaoMySql(PowergridDataSources.instance().getConnectionPool(dsName));
			int powergridId = dao.getPowergridId(request.getPowergridName());
			data = dao.getLineLoadTest(powergridId, formatter.format(startTime),request.getLineId());
			data.setBeforetime(time);
			data.setTime(System.currentTimeMillis());
			dataResponse.setData(data);
		}
		catch(Exception e){
			e.printStackTrace();
			dataResponse.setData(new DataError(e.getMessage()));
			dataResponse.setResponseComplete(true);
			return dataResponse;
		}
		if(startTime.after(requestAsync.getEndTime()))
			dataResponse.setResponseComplete(true);
		else
			dataResponse.setResponseComplete(false);
		return dataResponse;
	}
	
	private DataResponse synchronousHandle(RequestLineLoadTest request){
		DataResponse dataResponse = new DataResponse();
		ACLineSegmentTest data =null;
		try{
			long time = System.currentTimeMillis();
			String dsName = PowergridDataSources.instance().getDatasourceKeyWherePowergridName(new PowergridDaoMySql(), request.getPowergridName());
			PowergridSharedPerspectiveDaoMySql dao = new PowergridSharedPerspectiveDaoMySql(PowergridDataSources.instance().getConnectionPool(dsName));
			int powergridId = dao.getPowergridId(request.getPowergridName());
			data = dao.getLineLoadTest(powergridId, request.getStartTime(),request.getLineId());
			data.setTime(System.currentTimeMillis()-time);
			dataResponse.setData(data);

			//PowergridDataSources.instance().shutdown();
		}
		catch(Exception e){
			e.printStackTrace();
			dataResponse.setData(new DataError(e.getMessage()));
			return dataResponse;
		}
		
		return dataResponse;
	}

}