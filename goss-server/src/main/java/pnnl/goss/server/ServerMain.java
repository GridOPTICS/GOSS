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

import java.util.Dictionary;

import pnnl.goss.fusiondb.FusionDBServerActivator;
import pnnl.goss.fusiondb.launchers.DataStreamLauncher;
import pnnl.goss.gridmw.GridMWServerActivator;
import pnnl.goss.kairosdb.KairosDBServerActivator;
import pnnl.goss.mdart.MDARTServerActivator;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.internal.GossDataServicesImpl;
import pnnl.goss.server.core.internal.GossRequestHandlerRegistrationImpl;
import pnnl.goss.server.core.internal.GridOpticsServer;
import pnnl.goss.util.Utilities;
import static pnnl.goss.core.GossCoreContants.PROP_CORE_CONFIG;
import static pnnl.goss.core.GossCoreContants.PROP_DATASOURCES_CONFIG;


public class ServerMain {

	//private final static String powergridDatasourceConfig = "pnnl.goss.powergrid.server.cfg"; 

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

		Dictionary dataSourcesConfig = Utilities.loadProperties(PROP_DATASOURCES_CONFIG);
		Dictionary coreConfig = Utilities.loadProperties(PROP_CORE_CONFIG);
		
		GossDataServices dataServices = new GossDataServicesImpl();
		
		GossRequestHandlerRegistrationImpl registrationService = new GossRequestHandlerRegistrationImpl(dataServices);
		registrationService.setCoreServerConfig(coreConfig);
		
		@SuppressWarnings("unused")
		GridOpticsServer server = new GridOpticsServer(registrationService, true);
				
		FusionDBServerActivator fusionDBServerActivator = new FusionDBServerActivator(registrationService, dataServices);
		fusionDBServerActivator.update(dataSourcesConfig);
		fusionDBServerActivator.start();
		
		
		MDARTServerActivator mdartServerActivator = new MDARTServerActivator(registrationService, dataServices);
		mdartServerActivator.update(dataSourcesConfig);
		mdartServerActivator.start();
		
		KairosDBServerActivator kairosDBServerActivator = new KairosDBServerActivator(registrationService, dataServices);
		kairosDBServerActivator.update(dataSourcesConfig);
		kairosDBServerActivator.start();
		
		GridMWServerActivator gridmwServerActivator = new GridMWServerActivator(registrationService, dataServices);
		gridmwServerActivator.update(dataSourcesConfig);
		gridmwServerActivator.start();
		
		
		//Fusion Launchers----------------------------------------------
//		DataStreamLauncher launcher = new DataStreamLauncher();
//		launcher.startLauncher();
		
		
		//TODO: All the lines below to be removed after all the bundles are tested.
		//GossRequestHandlerRegistrationService handlers = new GossRequestHandlerRegistrationImpl(dataServices);

		//------------------------------------Powergrid(CA)----------------------------------------
		/*handlers.addHandlerMapping(RequestPowergrid.class, RequestPowergridHandler.class);
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
		handlers.addHandlerMapping(RequestAlertContext.class, RequestAlertHandler.class);
		handlers.addHandlerMapping(RequestAlerts.class, RequestAlertHandler.class);
		
		//--------------------------------Shared Perspective Security---------------------------------------
		//handlers.addSecurityMapping(RequestLineLoadTest.class, AccessControlHandlerAllowAll.class);
		
		//-------------------------------------HPC-------------------------------------------------
		//handlers.addHandlerMapping(ExecuteRequest.class, ExecuteHPCHandler.class);
		
		
		
		
		//-------------------------------------Fusion Security----------------------------------------------
		handlers.addSecurityMapping(RequestActualTotal.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestCapacityRequirement.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestForecastTotal.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestHAInterchangeSchedule.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestRTEDSchedule.class, AccessControlHandlerAllowAll.class);		
		handlers.addSecurityMapping(RequestPowergrid.class, AccessControlHandlerAllowAll.class);
		handlers.addSecurityMapping(RequestGeneratorData.class, AccessControlHandlerAllowAll.class);
		
		
		//-------------------------------------Tutorial----------------------------------------------
		handlers.addUploadHandlerMapping("Tutorial", TutorialDesktopHandler.class);
		handlers.addHandlerMapping(TutorialDownloadRequestSync.class, TutorialDesktopDownloadHandler.class);
		//Start launcher and aggregators
		String[] arguments = new String[] {};
		//Start aggregator and generator listening so they can be started by web ui
		 * 
		 */
	    
		//try {
			//Dictionary config = Utilities.loadProperties(powergridDatasourceConfig);

			//PowergridDataSources.instance().addConnections(config, "datasource");
			//Dictionary coreConfig = Utilities.loadProperties("config.properties");
			//handlers.setCoreServerConfig(coreConfig);
			//@SuppressWarnings("unused")
			//GridOpticsServer server = new GridOpticsServer(registrationService, true);
			
			//Launch the generator and aggregator listeners
			//new AggregatorLauncher().start();
		    //new GeneratorLauncher().start();
		    //new PythonJavaGatewayLauncher().start();

		/*} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDatasourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/


	}

}
