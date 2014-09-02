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
package pnnl.goss.client.tests;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.client.tests.util.ClientAuthHelper;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.requests.RequestPowergrid;

// import pnnl.goss.events.LineTripEvent;

public class ClientMainGca implements GossResponseEvent {

	private static GossClient client = null;
	private static final Logger log = LoggerFactory.getLogger(ClientMainGca.class);
	private static final String DATA = "Greek-118-North";

	public static void main(String[] args) {

		PowergridModel pgModel = null;
		DataResponse response = null;
		try {

			client = new GossClient(ClientAuthHelper.getGCACredentials());

			RequestPowergrid request1 = new RequestPowergrid(DATA);
			response = (DataResponse) client.getResponse(request1);

			if (response.getData() instanceof DataError) {
				log.debug("Data Error " + ((DataError) response.getData()).getMessage());
				client.close();
			} else {
				pgModel = (PowergridModel) response.getData();
				log.debug("Powergrid: " + pgModel.getPowergrid().getName() + " Number: " + pgModel.getPowergrid().getPowergridId());
				log.debug("# Buses: " + pgModel.getBuses().size());
				log.debug("# Branches: " + pgModel.getBranches().size());
				log.debug("# Transformers: " + pgModel.getTransformers().size());
				log.debug("# Lines: " + pgModel.getLines().size());
				log.debug("# Substations: " + pgModel.getSubstations().size());
				log.debug("# Machines: " + pgModel.getMachines().size());
				log.debug("# Loads: " + pgModel.getLoads().size());
				log.debug("# SwitchedShunts: " + pgModel.getSwitchedShunts().size());
			}
			/*
			 * pgModel =
			 * (PowergridModel)((DataResponse)client.getResponse(request1
			 * )).getData();
			 * 
			 * XStream xStream = new XStream(); xStream.toXML(pgModel, new
			 * FileOutputStream("powergrid.xml"));
			 */

		} catch (Exception e) {
			e.printStackTrace();
			client.close();
			return;
		}
		finally{
			client.close();
		}

//		if (pgModel != null) {
//			log.debug("Number of timesteps: " + pgModel.getTimesteps().size());
//
//			RequestPowergridTimeStep request2 = new RequestPowergridTimeStep(NORTH_AND_SOUTH, 1, pgModel.getTimesteps().get(1));
//
//			PowergridModel stepModel;
//			try {
//				response = (DataResponse) client.getResponse(request2);
//
//				if (response.getData() instanceof DataError) {
//					log.debug("Data Error " + ((DataError) response.getData()).getMessage());
//				} else {
//					stepModel = (PowergridModel) response.getData();
//					if (stepModel.getTimesteps() == null || stepModel.getTimesteps().size() == 0) {
//						log.error("No timesteps available.");
//					} else {
//						if (stepModel.getCurrentTimestamp() == null) {
//							log.error("Invalid current timestamp detected!");
//						} else {
//							if (stepModel.getCurrentTimestamp().equals(pgModel.getTimesteps().get(1))) {
//								log.debug("Timestamp correctly populated!");
//							} else {
//								log.debug("Timestamp not correctly populated!");
//							}
//						}
//					}
//				}
//			} catch (IllegalStateException e) {
//				e.printStackTrace();
//			} catch (JMSException e) {
//				e.printStackTrace();
//			} finally {
//				client.close();
//			}
//		}

		/*
		 * RequestComponentTimeSteps request3 = new RequestComponentTimeSteps(1,
		 * null, "line"); //load, switchedshunt, transformer
		 * client.sendRequest(request3);
		 * 
		 * RequestContingencyModel request4 = new RequestContingencyModel(1);
		 * client.sendRequest(request4);
		 * 
		 * RequestContingencyModelTimeStepValues request6 = new
		 * RequestContingencyModelTimeStepValues(1,1);
		 * client.sendRequest(request6);
		 * 
		 * RequestViolationTimeSteps request5 = new
		 * RequestViolationTimeSteps(1,null,Type.BRANCH);
		 * client.sendRequest(request5);
		 */

		// client.subscribeTo("LineTripEvent");

		// client.close();

	}

	public void onMessage(Serializable response) {

		/*
		 * DataResponse dataResponse=null;
		 * 
		 * 
		 * if(response instanceof DataResponse){ dataResponse =
		 * (DataResponse)response;
		 * 
		 * if(dataResponse.getData() instanceof PowerGrids){ PowerGrids
		 * powerGrid = (PowerGrids)dataResponse.getData();
		 * log.debug(powerGrid.getName()); }
		 * 
		 * if(dataResponse.getData() instanceof PowerGridTimeStepsList){
		 * PowerGridTimeStepsList timestepResponse =
		 * (PowerGridTimeStepsList)dataResponse.getData();
		 * log.debug(timestepResponse.getPowerGridId()); }
		 * 
		 * if(dataResponse.getData() instanceof LoadTimeStepsList){
		 * LoadTimeStepsList loadTimeStepsList =
		 * (LoadTimeStepsList)dataResponse.getData();
		 * log.debug(loadTimeStepsList.getLoadTimeStepsList().size()); }
		 * 
		 * if(dataResponse.getData() instanceof LineTimeStepsList){
		 * LineTimeStepsList line = (LineTimeStepsList)dataResponse.getData();
		 * log.debug(line.getLineTimeStepsList().size()); }
		 * 
		 * if(dataResponse.getData() instanceof SwitchedShuntTimeStepsList){
		 * SwitchedShuntTimeStepsList shunt =
		 * (SwitchedShuntTimeStepsList)dataResponse.getData();
		 * log.debug(shunt.getSwitchedShuntTimeStepsList().size()); }
		 * 
		 * if(dataResponse.getData() instanceof TransformerTimeStepsList){
		 * TransformerTimeStepsList transformer =
		 * (TransformerTimeStepsList)dataResponse.getData();
		 * log.debug(transformer.getTransformerTimeStepsList().size()); }
		 * 
		 * if(dataResponse.getData() instanceof Contingencies){ Contingencies
		 * contingencies = (Contingencies)dataResponse.getData();
		 * contingencies.getPowerFlowStatus();
		 * log.debug(contingencies.getName());
		 * 
		 * 
		 * 
		 * 
		 * 
		 * }
		 * 
		 * 
		 * if(dataResponse.getData() instanceof ContingenciesTYPEList){
		 * ContingenciesTYPEList contingenciesList =
		 * (ContingenciesTYPEList)dataResponse.getData();
		 * log.debug(contingenciesList.getContingenciesList().size()); }
		 * 
		 * if(dataResponse.getData() instanceof LineTripEvent){ LineTripEvent
		 * event = (LineTripEvent)dataResponse.getData();
		 * log.debug(event.getId());
		 * 
		 * 
		 * RequestPowerGrid request1 = new RequestPowerGrid("Greek-118");
		 * client.sendRequest(request1);
		 * 
		 * 
		 * }
		 * 
		 * 
		 * 
		 * }
		 */

	}
}
