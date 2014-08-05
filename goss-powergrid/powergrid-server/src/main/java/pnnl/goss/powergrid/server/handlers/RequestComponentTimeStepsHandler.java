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

import pnnl.goss.powergrid.datamodel.LineTimeStep;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;

import java.sql.ResultSet;
import java.sql.Statement;

import pnnl.goss.core.Data;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;


public class RequestComponentTimeStepsHandler{
	/*
	public DataResponse getResponse(Request request){
		
		Data data =null;
		
		
		try{
		if(request instanceof RequestComponentTimeSteps){
			
			RequestComponentTimeSteps componentRequest = (RequestComponentTimeSteps)request;
			componentRequest.getPowerGridId();
			componentRequest.getTimestep();
			
			String component = componentRequest.getComponent();
			String table= component + "timesteps";
			
			String dbQuery = "select * from powergrids."+table+" where powergridid = "+componentRequest.getPowerGridId()+" and timestep = '"+componentRequest.getTimestep() + "'";
			Statement stmt = PowergridDataSource.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(dbQuery.toLowerCase());
			
			if(component.equals("load")){
				LoadTimeStepsList loadList = new LoadTimeStepsList();
				while(rs.next()){
					LoadTimeSteps load = new LoadTimeSteps();
					load.setLoadId(rs.getInt(1));
					load.setPLoad(rs.getInt(4));
					load.setPowerGridId(componentRequest.getPowerGridId());
					load.setQLoad(rs.getInt(5));
					load.setTimeStep(componentRequest.getTimestep());
					loadList.addLoadTimeStepsList(load);
				}
				data = loadList;
			}
			
			else if(component.equals("line")){
				LineTimeStepsList lineList = new LineTimeStepsList();
				while(rs.next()){
					LineTimeSteps line = new LineTimeSteps();
					line.setPowerGridId(componentRequest.getPowerGridId());
					line.setLineId(rs.getInt(3));
					line.setTimeStep(componentRequest.getTimestep());
					line.setStatus(rs.getInt(4));
					line.setP(rs.getDouble("p"));
					line.setQ(rs.getDouble("q"));
					lineList.addLineTimeStepsList(line);
				}
				data = lineList;
			}
			
			else if(component.equals("switchedshunt")){
				SwitchedShuntTimeStepsList shuntTimeStepsList = new SwitchedShuntTimeStepsList();
				while(rs.next()){
					SwitchedShuntTimeSteps shuntTimeSteps = new SwitchedShuntTimeSteps();
					shuntTimeSteps.setPowerGridId(componentRequest.getPowerGridId());
					shuntTimeSteps.setTimeStep(componentRequest.getTimestep());
					shuntTimeSteps.setStatus(rs.getInt(4));
					shuntTimeSteps.setSwitchedShuntId(rs.getInt(3));
					shuntTimeStepsList.addSwitchedShuntTimeStepsList(shuntTimeSteps);
				}
				data = shuntTimeStepsList;
			}
			
			else if(component.equals("transformer")){
				TransformerTimeStepsList transformerTimeStepsList = new TransformerTimeStepsList();
				while(rs.next()){
					TransformerTimeSteps transformerTimeSteps = new TransformerTimeSteps();
					transformerTimeSteps.setPowerGridId(componentRequest.getPowerGridId());
					transformerTimeSteps.setTimeStep(componentRequest.getTimestep());
					transformerTimeSteps.setRatio(rs.getDouble(5));
					transformerTimeSteps.setStatus(rs.getInt(6));
					transformerTimeSteps.setTapPosition(rs.getDouble(4));
					transformerTimeSteps.setTransformerId(rs.getInt(3));
					transformerTimeSteps.setP(rs.getDouble("p"));
					transformerTimeSteps.setQ(rs.getDouble("q"));
					transformerTimeStepsList.addTransformerTimeStepsList(transformerTimeSteps);
				}
				data = transformerTimeStepsList;
			}
			else if(component.equals("machine")){ 
				MachineTimeStepList machineTimeStepsList = new MachineTimeStepList();
				while(rs.next()){
					MachineTimeSteps machineTimeStep = new MachineTimeSteps();
					machineTimeStep.setMachineId(rs.getInt("machineid"));
					machineTimeStep.setPowerGridId(componentRequest.getPowerGridId());
					machineTimeStep.setTimeStep(componentRequest.getTimestep());
					machineTimeStep.setPGen(rs.getDouble("pgen"));
					machineTimeStep.setQGen(rs.getDouble("qgen"));
					machineTimeStep.setStatus(rs.getInt("status"));
					machineTimeStepsList.addMachineTimeStepsList(machineTimeStep);
				}
				data = machineTimeStepsList;
			}
			
		}
			
		
		}
		catch(Exception e){
			e.printStackTrace();
			
		}
		
		DataResponse dataResponse = new DataResponse();
		dataResponse.setData(data);
		return dataResponse;
		
		
	}*/

}
