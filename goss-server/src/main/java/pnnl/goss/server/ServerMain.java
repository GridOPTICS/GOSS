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
package pnnl.goss.server;

//import goss.pnnl.fusiondb.handlers.RequestUploadTestHandler;
import goss.pnnl.fusiondb.handlers.FusionUploadHandler;
import goss.pnnl.fusiondb.handlers.RequestActualTotalHandler;
import goss.pnnl.fusiondb.handlers.RequestCapacityRequirementHandler;
import goss.pnnl.fusiondb.handlers.RequestForecastTotalHandler;
import goss.pnnl.fusiondb.handlers.RequestHAInterchangeScheduleHandler;
import goss.pnnl.fusiondb.handlers.RequestRTEDScheduleHandler;
import goss.pnnl.kairosdb.handlers.RequestKairosTestHandler;
import goss.pnnl.kairosdb.handlers.RequestPMUKairosHandler;
import goss.pnnl.kairosdb.handlers.RequestPMUMetadataHandler;

import java.sql.SQLException;
import java.util.Dictionary;

import pnnl.goss.core.UploadRequest;
import pnnl.goss.fusiondb.datamodel.CapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.fusiondb.requests.RequestHAInterchangeSchedule;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;
//import pnnl.goss.fusiondb.requests.RequestUploadTest;
import pnnl.goss.gridmw.handlers.RequestGridMWTestHandler;
import pnnl.goss.gridmw.handlers.RequestPMUHandler;
import pnnl.goss.gridmw.requests.RequestGridMWAsyncTest;
import pnnl.goss.gridmw.requests.RequestGridMWTest;
import pnnl.goss.gridmw.requests.RequestPMU;
import pnnl.goss.kairosdb.requests.RequestKairosAsyncTest;
//import pnnl.goss.hpc.handlers.ExecuteHPCHandler;
import pnnl.goss.kairosdb.requests.RequestKairosTest;
import pnnl.goss.kairosdb.requests.RequestPMUKairos;
import pnnl.goss.kairosdb.requests.RequestPMUMetaData;
import pnnl.goss.mdart.common.requests.RequestPIRecords;
import pnnl.goss.mdart.server.handlers.RequestPIRecordsHandler;
import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.requests.RequestPowergridTimeStep;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.powergrid.server.handlers.RequestPowergridHandler;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;
import pnnl.goss.server.core.InvalidDatasourceException;
import pnnl.goss.server.core.internal.GossRequestHandlerRegistrationImpl;
import pnnl.goss.server.core.internal.GridOpticsServer;
import pnnl.goss.sharedperspective.common.requests.RequestContingencyResult;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoad;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadAsyncTest;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadTest;
import pnnl.goss.sharedperspective.common.requests.RequestTopology;
import pnnl.goss.sharedperspective.server.handlers.RequestContingencyResultHandler;
import pnnl.goss.sharedperspective.server.handlers.RequestLineLoadHandler;
import pnnl.goss.sharedperspective.server.handlers.RequestLineLoadTestHandler;
import pnnl.goss.sharedperspective.server.handlers.RequestTopologyHandler;
import pnnl.goss.util.Utilities;

public class ServerMain {

	private final static String powergridDatasourceConfig = "pnnl.goss.powergrid.server.cfg.WE22743"; 

	public void attachShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Shutdown server main");
			}

		});

	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

		ServerMain main = new ServerMain();
		main.attachShutdownHook();

		// Add mappings to handler
		GossRequestHandlerRegistrationService handlers = new GossRequestHandlerRegistrationImpl();
		//------------------------------------Powergrid(CA)----------------------------------------
		handlers.addHandlerMapping(RequestPowergrid.class, RequestPowergridHandler.class);
		handlers.addHandlerMapping(RequestPowergridTimeStep.class, RequestPowergridHandler.class);


		//---------------------------Performance Testing-------------------------------------------
		handlers.addHandlerMapping(RequestGridMWTest.class, RequestGridMWTestHandler.class);
		handlers.addHandlerMapping(RequestGridMWAsyncTest.class, RequestGridMWTestHandler.class);
		handlers.addHandlerMapping(RequestKairosTest.class, RequestKairosTestHandler.class);
		handlers.addHandlerMapping(RequestKairosAsyncTest.class, RequestKairosTestHandler.class);
		handlers.addHandlerMapping(RequestLineLoadTest.class, RequestLineLoadTestHandler.class);
		handlers.addHandlerMapping(RequestLineLoadAsyncTest.class, RequestLineLoadTestHandler.class);
				
		//---------------------------Performance Testing Security-----------------------------------
		handlers.addSecurityMapping(RequestGridMWTest.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestGridMWAsyncTest.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestKairosTest.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestKairosAsyncTest.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestLineLoadTest.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestLineLoadAsyncTest.class, AccessControlHandlerAllowAll.class);

		//-------------------------------------PMU(GridMW)-----------------------------------------
		handlers.addHandlerMapping(RequestPMU.class, RequestPMUHandler.class);
		//handlers.addSecurityMapping(RequestPMU.class, AccessControlHandlerPMU.class);
		handlers.addHandlerMapping(RequestPMUMetaData.class, RequestPMUMetadataHandler.class);
		handlers.addHandlerMapping(RequestPMUKairos.class, RequestPMUKairosHandler.class);
		
		
		//--------------------------------Shared Perspective---------------------------------------
		handlers.addHandlerMapping(RequestTopology.class, RequestTopologyHandler.class);
		handlers.addHandlerMapping(RequestLineLoadTest.class, RequestLineLoadTestHandler.class);
		handlers.addHandlerMapping(RequestContingencyResult.class, RequestContingencyResultHandler.class);
		handlers.addHandlerMapping(RequestLineLoad.class, RequestLineLoadHandler.class);
		handlers.addHandlerMapping(RequestContingencyResult.class, RequestContingencyResultHandler.class);
		
		//--------------------------------Shared Perspective Security---------------------------------------
		//handlers.addSecurityMapping(RequestLineLoadTest.class, AccessControlHandlerAllowAll.class);
		
		//-------------------------------------HPC-------------------------------------------------
		//handlers.addHandlerMapping(ExecuteRequest.class, ExecuteHPCHandler.class);

		//-------------------------------------Fusion----------------------------------------------
		handlers.addHandlerMapping(RequestActualTotal.class, RequestActualTotalHandler.class);
		handlers.addHandlerMapping(RequestCapacityRequirement.class, RequestCapacityRequirementHandler.class);
		handlers.addHandlerMapping(RequestForecastTotal.class, RequestForecastTotalHandler.class);
		handlers.addHandlerMapping(RequestHAInterchangeSchedule.class, RequestHAInterchangeScheduleHandler.class);
		handlers.addHandlerMapping(RequestRTEDSchedule.class, RequestRTEDScheduleHandler.class);		
		handlers.addHandlerMapping(RequestPowergrid.class, RequestPowergridHandler.class);
		handlers.addHandlerMapping(UploadRequest.class, FusionUploadHandler.class);
		
		//-------------------------------------Fusion Security----------------------------------------------
		handlers.addSecurityMapping(RequestActualTotal.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestCapacityRequirement.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestForecastTotal.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestHAInterchangeSchedule.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestRTEDSchedule.class, AccessControlHandlerAllowAll.class);		
		handlers.addSecurityMapping(RequestPowergrid.class, AccessControlHandlerAllowAll.class);
		
				
		
		//-------------------------------------MDART----------------------------------------------
		handlers.addHandlerMapping(RequestPIRecords.class, RequestPIRecordsHandler.class);
		//handlers.addSecurityMapping(RequestPIRecords.class, AccessControlHandlerAllowAll.class);
		

		try {
			Dictionary config = Utilities.loadProperties(powergridDatasourceConfig);

			PowergridDataSources.instance().addConnections(config, "datasource");
			Dictionary coreConfig = Utilities.loadProperties("config.properties");
			handlers.setCoreServerConfig(coreConfig);
			@SuppressWarnings("unused")
			GridOpticsServer server = new GridOpticsServer(handlers, true);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDatasourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
