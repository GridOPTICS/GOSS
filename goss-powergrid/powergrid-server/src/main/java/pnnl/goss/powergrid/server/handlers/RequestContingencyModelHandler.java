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

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.powergrid.ContingencyModel;
import pnnl.goss.powergrid.dao.PowergridDao;
import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.datamodel.Contingency;
import pnnl.goss.powergrid.datamodel.ContingencyBranchOut;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.powergrid.requests.RequestContingencyModel;
import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.server.core.AbstractGossRequestHandler;

public class RequestContingencyModelHandler extends AbstractGossRequestHandler {

	@Override
	public Response handle(Request request) {
		return getResponse(request);
	}

	public DataResponse getResponse(Request request) {

		ContingencyModel model = new ContingencyModel();

		RequestContingencyModel contingencyRequest = null;

		DataResponse response = null;

		// All of the requests must stem from RequestPowergrid.
		if (!(request instanceof RequestPowergrid)) {
			response = new DataResponse(new DataError("Unkown request: " + request.getClass().getName()));
			return response;
		}

		RequestPowergrid requestPowergrid = (RequestPowergrid) request;

		// Make sure there is a valid name.
		if (requestPowergrid.getPowergridName() == null || requestPowergrid.getPowergridName().isEmpty()) {
			response = new DataResponse(new DataError("Bad powergrid name"));
			return response;
		}

		String datasourceKey = PowergridDataSources.instance().getDatasourceKeyWherePowergridName(new PowergridDaoMySql(), requestPowergrid.getPowergridName());

		// Make sure the powergrid name is located in our set.
		if (datasourceKey == null) {
			response = new DataResponse(new DataError("Unkown powergrid: " + requestPowergrid.getPowergridName()));
			return response;
		}
		
		PowergridDao dao = new PowergridDaoMySql(PowergridDataSources.instance().getConnectionPool(datasourceKey));
		Powergrid grid = dao.getPowergridByName(requestPowergrid.getPowergridName());
		
		if (request instanceof RequestContingencyModel) {
			contingencyRequest = (RequestContingencyModel) request;

			int powergridId = grid.getPowergridId();
			List<Timestamp> timesteps = null;

			try {
				String dbQuery = "select * from contingencies where powergridid =" + powergridId;
				Statement stmt = PowergridDataSources.instance().getConnection(datasourceKey).createStatement();
				ResultSet rs = stmt.executeQuery(dbQuery.toLowerCase());
				List<Contingency> contingencies = new ArrayList<Contingency>();

				while (rs.next()) {
					Contingency contingency = new Contingency();

					contingency.setContingencyId(rs.getInt(1));
					contingency.setName(rs.getString(3));
					contingency.setPowergridId(powergridId);
					contingency.setPowerFlowStatus(rs.getInt(4));
					contingencies.add(contingency);

				}
				rs.close();

				for (Contingency contingency : contingencies) {
					model.addContingency(contingency, getContingencyBranchesOut(datasourceKey, powergridId, contingency.getContingencyId()));

					if (timesteps == null) {
						timesteps = getContingencyTimesteps(datasourceKey, powergridId, contingency.getContingencyId());
					}
					model.setTimeSteps(timesteps);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		}

		if (response == null){
			
		}
		
		response = new DataResponse();
		response.setData(model);

		return response;
	}

	private List<ContingencyBranchOut> getContingencyBranchesOut(String datasourceKey, int powergridId, int contingencyId) {

		List<ContingencyBranchOut> branchesOuts = new ArrayList<ContingencyBranchOut>();
		try {
			String dbQuery = "select * from contingencybranchesout where contingencyid=" + contingencyId + " AND powergridId=" + powergridId;
			Statement stmt = PowergridDataSources.instance().getConnection(datasourceKey).createStatement();
			ResultSet rs = stmt.executeQuery(dbQuery.toLowerCase());
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				ContingencyBranchOut branchesOut = new ContingencyBranchOut();
				branchesOut.setPowergridId(rs.getInt(3));
				branchesOut.setBranchId(rs.getInt(1));
				branchesOut.setContingencyId(rs.getInt(2));
				branchesOuts.add(branchesOut);
			}

			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return branchesOuts;
	}

	private List<Timestamp> getContingencyTimesteps(String datasourceKey, int powergridId, int contingencyId) {
		List<Timestamp> timesteps = new ArrayList<Timestamp>();
		try {
			String dbQuery = "select timestep from contingencytimesteps where contingencyid=" + contingencyId + " AND powergridId=" + powergridId;
			Statement stmt = PowergridDataSources.instance().getConnection(datasourceKey).createStatement();
			ResultSet rs = stmt.executeQuery(dbQuery.toLowerCase());
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				timesteps.add(rs.getTimestamp(0));
			}

			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return timesteps;
	}
}
