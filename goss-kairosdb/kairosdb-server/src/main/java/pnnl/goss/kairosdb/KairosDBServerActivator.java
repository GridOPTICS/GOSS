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
package pnnl.goss.kairosdb;

import java.util.Dictionary;

import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.kairosdb.handlers.RequestKairosTestHandler;
import pnnl.goss.kairosdb.handlers.RequestPMUKairosHandler;
import pnnl.goss.kairosdb.handlers.RequestPMUMetadataHandler;
import pnnl.goss.kairosdb.requests.RequestKairosAsyncTest;
import pnnl.goss.kairosdb.requests.RequestKairosTest;
import pnnl.goss.kairosdb.requests.RequestPMUKairos;
import pnnl.goss.kairosdb.requests.RequestPMUMetaData;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

public class KairosDBServerActivator{

	
	public static final String PROP_KAIROSDB_HOST = "kairosdb.db.uri";
	public static final String PROP_KAIROSDB_PORT = "kairosdb.db.port";
	
	private String hostname;
	private String port;
	
	/**
	 * <p>
	 * Allows the tracking of the goss registration service.
	 * </p>
	 */
	private GossRequestHandlerRegistrationService registrationService;
	private GossDataServices dataServices;
	
	/**
	 * <p>
	 * Add logging to the class so that we can debug things effectively after deployment.
	 * </p>
	 */
	private static Logger log = LoggerFactory.getLogger(KairosDBServerActivator.class);

	
	public KairosDBServerActivator(
			@Requires GossRequestHandlerRegistrationService registrationService,
			@Requires GossDataServices dataServices) {
		this.registrationService = registrationService;
		this.dataServices = dataServices;
		log.debug("Constructing activator");
	}
	
	@Validate
	public void start() {
		log.info("Starting bundle: " + this.getClass().getName());
		try{
		registrationService.addHandlerMapping(RequestKairosAsyncTest.class, RequestKairosTestHandler.class);
		registrationService.addHandlerMapping(RequestKairosTest.class, RequestKairosTestHandler.class);
		registrationService.addHandlerMapping(RequestPMUMetaData.class, RequestPMUMetadataHandler.class);
		registrationService.addHandlerMapping(RequestPMUKairos.class, RequestPMUKairosHandler.class);
		
		registrationService.addSecurityMapping(RequestKairosAsyncTest.class, AccessControlHandlerAllowAll.class);
		registrationService.addSecurityMapping(RequestKairosTest.class, AccessControlHandlerAllowAll.class);
		registrationService.addSecurityMapping(RequestPMUMetaData.class, AccessControlHandlerAllowAll.class);
		registrationService.addSecurityMapping(RequestPMUKairos.class, AccessControlHandlerAllowAll.class);
		}
		catch(Exception e){
			log.error("Error starting bundle", e);
		}

    }

	@Invalidate
    public void stop() {
    	try {
			log.info("Stopping the bundle"+this.getClass().getName());
			
			registrationService.removeHandlerMapping(RequestKairosAsyncTest.class);
			registrationService.removeHandlerMapping(RequestKairosTest.class);
			
			registrationService.removeSecurityMapping(RequestKairosAsyncTest.class);
			registrationService.removeSecurityMapping(RequestKairosTest.class);
			
    	}
		catch(Exception e){
			log.error("Error stopping  bundle", e);
		}
    }

    
    @SuppressWarnings("rawtypes")
	@Updated
	public void update(Dictionary config) {
		log.debug("updating");
		try{
			hostname = (String) config.get(PROP_KAIROSDB_HOST);
			port = (String) config.get(PROP_KAIROSDB_PORT);
			if (dataServices != null) {
				dataServices.registerData(PROP_KAIROSDB_HOST, hostname);
				dataServices.registerData(PROP_KAIROSDB_PORT, port);
			} else {
				log.error("dataServices is null!");
			}
		}
		catch(Exception e){
			log.error("Error updating bundle", e);
		}
		
	}	
	
}