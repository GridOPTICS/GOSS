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

import pnnl.goss.powergrid.ContingencyTimeStepModelValues;
import pnnl.goss.powergrid.datamodel.Contingency;
import pnnl.goss.powergrid.datamodel.ContingencyBranchViolation;
import pnnl.goss.powergrid.datamodel.ContingencyBusViolation;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pnnl.goss.core.Data;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.powergrid.requests.RequestContingencyModelTimeStepValues;
import pnnl.goss.powergrid.server.PowergridServerActivator;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.server.core.GossRequestHandler;
import pnnl.goss.server.core.GossServerActivator;

public class RequestContingencyModelTimeStepValuesHandler extends GossRequestHandler {

	String datasourceKey = null;
	
	@Override
	public Response handle(Request request) {
		//datasourceKey = (request.getDatasourceKey() == null)?"northandsouth":request.getDatasourceKey();
		return null;
	}
		
	public DataResponse getResponse(Request request){

		RequestContingencyModelTimeStepValues ctgRequest = null;
		ContingencyTimeStepModelValues model = null; 
		
		if(request instanceof RequestContingencyModelTimeStepValues){
			
			ctgRequest = (RequestContingencyModelTimeStepValues)request;
			
			try{
				
				model = new ContingencyTimeStepModelValues();
				model.setContingencyId(ctgRequest.getContingencyId());
				model.setPowergridId(ctgRequest.getPowerGridId());
				
				/*
				String dbQuery = "select * from contingencies where contingencyid ="+ctgRequest.getContingencyId()+" and powergridid ="+ctgRequest.getPowerGridId();
				Statement stmt = PowergridDataSource.getConnection().createStatement();
				ResultSet rs = stmt.executeQuery(dbQuery.toLowerCase());
				
				if(rs.next()){
					contingency.setContingencyId(ctgRequest.getContingencyId());
					contingency.setName(rs.getString(3));
					contingency.setPowerGridId(rs.getInt(2));
					contingency.setPowerFlowStatus(rs.getInt(4));
				}
				
				

				//Add Branches Outs
				dbQuery = "select * from contingencybranchesout where contingencyid ="+ctgRequest.getContingencyId()+" and powergridid ="+ctgRequest.getPowerGridId();
				rs = stmt.executeQuery(dbQuery.toLowerCase());
				List<ContingencyBranchesOut> branchesOuts = new ArrayList<ContingencyBranchesOut>();
				while(rs.next()){
					ContingencyBranchesOut branchesOut = new ContingencyBranchesOut();
					branchesOut.setPowerGridId(rs.getInt(3));
					branchesOut.setBranchId(rs.getInt(1));
					branchesOut.setContingencyId(rs.getInt(2));
					branchesOuts.add(branchesOut);
				}
				contingency.setBranchesOut(branchesOuts);*/
				
				//Add Branch Voilations
				String dbQuery = "select * from contingencybranchviolations where contingencyid ="+ctgRequest.getContingencyId()+" and powergridid ="+ctgRequest.getPowerGridId();
				Statement stmt = this.dataservices.getPooledConnection(PowergridServerActivator.PROP_POWERGRID_DATASERVICE).createStatement();
				//Statement stmt = PowergridDataSources.instance().getConnection(datasourceKey).createStatement();
				ResultSet rs = stmt.executeQuery(dbQuery.toLowerCase());
				List<ContingencyBranchViolation> branchViolations = new ArrayList<ContingencyBranchViolation>();
				while(rs.next()){
					ContingencyBranchViolation violations = new ContingencyBranchViolation();
					violations.setPowergridId(rs.getInt(2));
					violations.setBranchId(rs.getInt(3));
					violations.setContingencyId(rs.getInt(1));
					violations.setVoltage(rs.getDouble(4));
					branchViolations.add(violations);
				}
				model.setBranchViolations(branchViolations);

				//Add Bus violation
				dbQuery = "select * from contingencybusviolations where contingencyid ="+ctgRequest.getContingencyId()+" and powergridid ="+ctgRequest.getPowerGridId();
				rs = stmt.executeQuery(dbQuery.toLowerCase());
				List<ContingencyBusViolation> busViolations = new ArrayList<ContingencyBusViolation>();
				while(rs.next()){
					ContingencyBusViolation violations = new ContingencyBusViolation();
					violations.setPowergridId(rs.getInt(2));
					violations.setBusNumber(rs.getInt(3));
					violations.setContingencyId(rs.getInt(1));
					violations.setVoltage(rs.getDouble(4));
					busViolations.add(violations);
				}
				model.setBusViolations(busViolations);
				
				
			}
			catch(Exception e){
				e.printStackTrace();

			}
		}

		DataResponse response = new DataResponse();
		response.setData(model);

		return response;
	}

}
