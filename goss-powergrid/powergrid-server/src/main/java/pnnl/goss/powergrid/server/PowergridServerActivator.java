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
package pnnl.goss.powergrid.server;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.requests.RequestPowergridList;
import pnnl.goss.powergrid.requests.RequestPowergridTimeStep;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.powergrid.server.handlers.RequestAvailableDatasourcesHandler;
import pnnl.goss.powergrid.server.handlers.RequestPowergridHandler;
import pnnl.goss.powergrid.server.impl.PowergridContextServiceImpl;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;
import pnnl.goss.server.core.InvalidDatasourceException;

public class PowergridServerActivator implements BundleActivator, ManagedService{

	/**
	 * <p>
	 * The configuration file in $SMX_HOME/etc will be CONFIG_PID.cfg
	 * </p>
	 */
	private static final String CONFIG_PID = "pnnl.goss.powergrid.server";
	
	/**
	 * <p>
	 * Allows the tracking of the goss registration service.
	 * </p>
	 */
	private ServiceRegistration registration;
	
	/**
	 * <p>
	 * A reference to the datasources object that is registered with this class.
	 * </p>
	 */
	private static PowergridDataSources powergridDatasources;
	
	/**
	 * <p>
	 * Add logging to the class so that we can debug things effectively after deployment.
	 * </p>
	 */
	private static Logger log = LoggerFactory.getLogger(PowergridServerActivator.class);

	/**
	 * <p>
	 * Allows tracking of the registration service from the core-server.
	 * </p>
	 */
	private ServiceTracker registrationTracker;
	
	/**
	 * Keep a reference to the bundle context for unregistering services.
	 */
	private static BundleContext bundleContext;
	
	/**
	 * Tracks the registration of the PowergridUpdateService
	 */
	private ServiceRegistration contextServiceRegistration;
	
	
	public static BundleContext getBundleContext(){
		return bundleContext;
	}
	
	
	
	@SuppressWarnings("rawtypes")
	public void start(BundleContext context) {
		bundleContext = context;
		
		System.out.println("Starting bundle"+this.getClass().getName());
		log.info("Starting bundle: " + this.getClass().getName());
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
		}

		registration = context.registerService(ManagedService.class.getName(), this, getDefaults());
		
		
		
		PowergridServerActivator.powergridDatasources = PowergridDataSources.instance();
		context.registerService(PowergridDataSources.class.getName(), PowergridServerActivator.powergridDatasources, new Hashtable());
		
		contextServiceRegistration = bundleContext.registerService(PowergridContextService.class.getName(), new PowergridContextServiceImpl(), null);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Hashtable getDefaults(){
		Hashtable properties= new Hashtable();
		properties.put(Constants.SERVICE_PID,  CONFIG_PID);
		//properties.put("datasource0", "datasource0=northandsouth,jdbc:mysql://localhost:3306/northandsouth,root,rootpass,com.mysql.jdbc.Driver");
		return properties;
	}
	
	/**
	 * <p>
	 * The registerPowergridHandlers method registers the handlers with the GossRequestHandlerRegistrationService.
	 * </p>
	 */
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
	}

	public void stop(BundleContext context) {
		try {
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
			
			if (contextServiceRegistration != null){
				contextServiceRegistration.unregister();
			}

			
		} catch (Exception e) {
			log.error(e.getStackTrace().toString());
			e.printStackTrace();
		}
	}


	@SuppressWarnings("rawtypes")
	public synchronized void updated(Dictionary configuration) throws ConfigurationException {
		log.debug("Updating configuration for: "+this.getClass().getName());
		
		if (configuration == null){
			registration.setProperties(getDefaults());
		}
		else{
			try {
				powergridDatasources.addConnections(configuration, "datasource");
				// Sets the other properties in the configuration file to be on the service.
				registration.setProperties(configuration);
			} catch (SQLException e) {
				log.error("SqlException", e);
				throw new ConfigurationException("Sql Exception", null, e);
			} catch (InvalidDatasourceException e) {
				log.error("InvalidDatasourceException", e);
				throw new ConfigurationException("InvalidDatasourceException", null, e);
			}

		}
		
	}

}