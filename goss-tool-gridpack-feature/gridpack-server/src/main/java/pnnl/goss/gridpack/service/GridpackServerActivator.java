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
package pnnl.goss.gridpack.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.requests.RequestPowergridList;
import pnnl.goss.powergrid.requests.RequestPowergridTimeStep;
import pnnl.goss.powergrid.server.PowergridServerActivator;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.powergrid.server.handlers.RequestPowergridHandler;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

public class GridpackServerActivator implements BundleActivator {
	/**
	 * <p>
	 * Allows tracking of the registration service from the core-server.
	 * </p>
	 */
	private ServiceTracker registrationTracker;
		
	/**
	 * <p>
	 * Add logging to the class so that we can debug things effectively after deployment.
	 * </p>
	 */
	private static Logger log = LoggerFactory.getLogger(PowergridServerActivator.class);
    public void start(BundleContext context) {
        System.out.println("Starting the bundle");
        /*log.info("Starting bundle: " + this.getClass().getName());
		try {
			String filterStr = "(" + Constants.OBJECTCLASS + "=" + GossRequestHandlerRegistrationService.class.getName() + ")";
			Filter filter = context.createFilter(filterStr);
			registrationTracker = new ServiceTracker(context, filter, null);
			registrationTracker.open();
			
			// Register the handlers on the registration service.
			registerPowergridHandlers();
			
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    }

    public void stop(BundleContext context) {
    	log.info("Stopping the bundle"+this.getClass().getName());
		/*try {
			log.info("Stopping the bundle"+this.getClass().getName());
			System.out.println("Stopping the bundle"+this.getClass().getName());
			GossRequestHandlerRegistrationService registrationService = (GossRequestHandlerRegistrationService) registrationTracker.getService();

			if (registrationService != null) {
				registrationService.removeHandlerMapping(RequestPowergrid.class);
				registrationService.removeHandlerMapping(RequestPowergridTimeStep.class);
				registrationService.removeHandlerMapping(RequestPowergridList.class);
				
				registrationService.removeSecurityMapping(RequestPowergrid.class);
				registrationService.removeSecurityMapping(RequestPowergridTimeStep.class);
			}
			
			if (registration != null){
				registration.unregister();
			}
			
			if (powergridDatasources  != null){
				powergridDatasources.shutdown();
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}*/
    }

    /**
	 * <p>
	 * The registerPowergridHandlers method registers the handlers with the GossRequestHandlerRegistrationService.
	 * </p>
	 */
    /*
	private void registerPowergridHandlers(){
		GossRequestHandlerRegistrationService registrationService = (GossRequestHandlerRegistrationService) registrationTracker.getService();
		
		if(registrationService != null){
			// Registering service handlers here
			registrationService.addHandlerMapping(RequestPowergrid.class, RequestPowergridHandler.class);
			registrationService.addHandlerMapping(RequestPowergridTimeStep.class, RequestPowergridHandler.class);
			registrationService.addHandlerMapping(RequestPowergridList.class, RequestPowergridHandler.class);
			
			registrationService.addSecurityMapping(RequestPowergrid.class, AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestPowergridTimeStep.class, AccessControlHandlerAllowAll.class);
		}
		else{
			log.debug(GossRequestHandlerRegistrationService.class.getName()+" not found!");
		}		
	}*/
}