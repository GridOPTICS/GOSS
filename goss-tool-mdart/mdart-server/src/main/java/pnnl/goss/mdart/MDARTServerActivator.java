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
package pnnl.goss.mdart;

import static pnnl.goss.core.GossCoreContants.PROP_DATASOURCES_CONFIG;

import java.util.Dictionary;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.mdart.common.requests.RequestPIRecords;
import pnnl.goss.mdart.server.handlers.RequestPIRecordsHandler;
import pnnl.goss.server.core.BasicDataSourceCreator;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

@Instantiate
@Component(managedservice = PROP_DATASOURCES_CONFIG)
public class MDARTServerActivator {

	public static final String PROP_MDARTDB_DATASERVICE = "goss/mdartdb";
	public static final String PROP_MDARTDB_USER = "mdart.db.user";
	public static final String PROP_MDARTDB_PASSWORD = "mdart.db.password";
	public static final String PROP_MDARTDB_URI = "mdart.db.uri";
	
	
	/**
	 * <p>
	 * Add logging to the class so that we can debug things effectively after
	 * deployment.
	 * </p>
	 */
	private static Logger log = LoggerFactory
			.getLogger(MDARTServerActivator.class);
	
	private GossRequestHandlerRegistrationService registrationService;
	private GossDataServices dataServices;
	@Requires
	private BasicDataSourceCreator datasourceCreator;

	private String user;
	private String password;
	private String uri;
	
	
	
	public MDARTServerActivator(
			@Requires GossRequestHandlerRegistrationService registrationService,
			@Requires GossDataServices dataServices) {
		this.registrationService = registrationService;
		this.dataServices = dataServices;
		log.debug("Constructing activator");
	}
		
	
	
	private void registerDataService() {
		if (dataServices != null) {
			if (!dataServices.contains(PROP_MDARTDB_DATASERVICE)) {
				log.debug("Attempting to register dataservice: "
						+ PROP_MDARTDB_DATASERVICE);
				if (datasourceCreator == null){
					datasourceCreator = new BasicDataSourceCreator();
				}
				if (datasourceCreator != null){
					try {
						dataServices.registerData(PROP_MDARTDB_DATASERVICE,
								datasourceCreator.create(uri, user, password));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
				else{
					log.error("datasourceCreator is null!");
				}
			}
		} else {
			log.error("dataServices is null!");
		}
	}

	
	@SuppressWarnings("rawtypes")
	@Updated
	public void update(Dictionary config) {
		log.debug("updating");
		user = (String) config.get(PROP_MDARTDB_USER);
		password = (String) config.get(PROP_MDARTDB_PASSWORD);
		uri = (String) config.get(PROP_MDARTDB_URI);
		
		boolean valid = true;
		
		if (user == null || user.isEmpty()){
			log.error("Invalid user in config property: " + PROP_MDARTDB_USER);
			valid = false;
		}
		if (password == null || password.isEmpty()){
			log.error("Invalid password in config property: " + PROP_MDARTDB_PASSWORD);
			valid = false;
		}
		if (uri == null || uri.isEmpty()){
			log.error("Invalid uri in config proeprty: "+PROP_MDARTDB_URI);
			valid = false;
		}
			
		if (valid){
			log.debug("updated uri: " + uri + "\n\tuser:" + user);			
			registerDataService();
		}
	}

	@Validate
	public void start() {
		log.info("Starting bundle: " + this.getClass().getName());
		try{
			if (registrationService != null) {
			
				registrationService.addHandlerMapping(RequestPIRecords.class, RequestPIRecordsHandler.class);
	
			} else {
				log.error(GossRequestHandlerRegistrationService.class.getName()
						+ " not found!");
			}
		} catch (Exception e) {
			log.error("error during starting of bundle", e);
		} 
	}

	@Invalidate
	public void stop() {
		try {
			log.info("Stopping the bundle" + this.getClass().getName());
			if (registrationService != null) {
				registrationService.removeHandlerMapping(RequestPIRecords.class);
			}

		} catch (Exception e) {
			log.error("error during stopping of bundle", e);
		} finally {
			if (dataServices != null) {
				dataServices.unRegisterData(PROP_MDARTDB_DATASERVICE);
			}
		}
	}
}
