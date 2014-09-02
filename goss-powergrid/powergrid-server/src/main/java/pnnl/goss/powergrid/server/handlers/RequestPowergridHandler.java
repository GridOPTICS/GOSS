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
package pnnl.goss.powergrid.server.handlers;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Data;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.collections.PowergridList;
import pnnl.goss.powergrid.collections.PowergridNameList;
import pnnl.goss.powergrid.dao.PowergridDao;
import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.requests.RequestPowergridList;
import pnnl.goss.powergrid.requests.RequestPowergridTimeStep;
import pnnl.goss.powergrid.requests.RequestPowergridTimeStepValues;
import pnnl.goss.powergrid.server.PowergridServerActivator;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestPowergridHandler extends GossRequestHandler {

	String datasourceKey = null;
	private static Logger log = LoggerFactory.getLogger(RequestPowergridHandler.class);

	private DataResponse getPowergridModleAtTimestepResponse(PowergridDao dao, String powergridName, Timestamp timestep) {
		Powergrid grid = dao.getPowergridByName(powergridName);
		DataResponse response = new DataResponse();
		if (grid.isSetPowergridId()) {
			PowergridModel model = dao.getPowergridModelAtTime(grid.getPowergridId(), timestep);
			response.setData(model);
		} else {
			response.setData(new DataError("Powergrid not found!"));
		}

		return response;
	}

	private DataResponse getPowergridModelResponse(PowergridDao dao, RequestPowergrid request) {
		Powergrid grid = null;
		
		// Determine how the user requested the data.
		if (request.getPowergridName() != null && !request.getPowergridName().isEmpty())
		{
			grid = dao.getPowergridByName(request.getPowergridName());
		}
		
		DataResponse response = new DataResponse();
		
		if (grid != null && grid.isSetPowergridId()) {
			PowergridModel model = dao.getPowergridModel(grid.getPowergridId());
			response.setData(model);
		} else {
			response.setData(new DataError("Powergrid not found!"));
		}

		return response;

	}
	
	private DataResponse getAvailablePowergrids(PowergridDao dao){
		PowergridList grids = new PowergridList(dao.getAvailablePowergrids());
		DataResponse response = new DataResponse();
		response.setData(grids);
		return response;
	}
	
	private DataResponse getAllPowergrids(){
		DataResponse response = new DataResponse();
		PowergridList powergridList = new PowergridList(PowergridDataSources.instance().getAllPowergrids());
		response.setData(powergridList);
		return response;
	}

	public DataResponse getResponse(Request request) {
		DataResponse response = null;
		
		// All of the requests must stem from RequestPowergrid.
		if (!(request instanceof RequestPowergrid)){
			response = new DataResponse(new DataError("Unkown request: " + request.getClass().getName()));
			return response;
		}
		
		RequestPowergrid requestPowergrid = (RequestPowergrid)request;
		
		if(requestPowergrid.getPowergridName()== null && request instanceof RequestPowergridList){
			return getAllPowergrids();
		}
		
		// Make sure there is a valid name.
		if(requestPowergrid.getPowergridName() == null || requestPowergrid.getPowergridName().isEmpty()){
			response = new DataResponse(new DataError("Bad powergrid name"));
			return response;
		}
		
//		String datasourceKey = PowergridDataSources.instance().getDatasourceKeyWherePowergridName(new PowergridDaoMySql(), requestPowergrid.getPowergridName());
//		
//		// Make sure the powergrid name is located in our set.
//		if (datasourceKey == null){
//			response = new DataResponse(new DataError("Unkown powergrid: " + requestPowergrid.getPowergridName()));
//			return response;
//		}
		
		log.debug("using datasource: " + datasourceKey);
		PowergridDao dao = new PowergridDaoMySql((DataSource) this.dataservices.getDataService(PowergridServerActivator.PROP_POWERGRID_DATASERVICE));

		if (request instanceof RequestPowergridTimeStep) {
			RequestPowergridTimeStep pgRequest = (RequestPowergridTimeStep) request;
			response = getPowergridModleAtTimestepResponse(dao, requestPowergrid.getPowergridName(), pgRequest.getTimestep());
		} else if (request instanceof RequestPowergridList) {
			RequestPowergridList pgRequest = (RequestPowergridList) request;
			response = getAvailablePowergrids(dao);
		} else if (request instanceof RequestPowergridTimeStepValues) {
			response = new DataResponse(new DataError("RequestPowergridTimeStepValues not implemented yet!"));
		} else{
			response = getPowergridModelResponse(dao, (RequestPowergrid) request);
		}

		// A data response if there is an invalid request type.
		if (response == null) {
			response = new DataResponse();
			response.setData(new DataError("Invalid request type specified!"));
		}

		return response;
	}

	public void upload(Data data) {

	}

	@Override
	public Response handle(Request request) {
		Response response = null;
		try {
			response = getResponse(request);
		} catch (Exception e) {
			log.error("handler", e);
			DataResponse dr = new DataResponse();
			dr.setData(new DataError("Handling data error."));
			response = dr;

		}

		return response;
	}

}
