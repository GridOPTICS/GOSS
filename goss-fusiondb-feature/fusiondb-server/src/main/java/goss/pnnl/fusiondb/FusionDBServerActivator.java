package goss.pnnl.fusiondb;

import goss.pnnl.fusiondb.handlers.RequestActualTotalHandler;
import goss.pnnl.fusiondb.handlers.RequestCapacityRequirementHandler;
import goss.pnnl.fusiondb.handlers.RequestForecastTotalHandler;
import goss.pnnl.fusiondb.handlers.RequestHAInterchangeScheduleHandler;
import goss.pnnl.fusiondb.handlers.RequestRTEDScheduleHandler;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.fusiondb.requests.RequestHAInterchangeSchedule;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

public class FusionDBServerActivator implements BundleActivator, ManagedService{

	/**
	 * <p>
	 * The configuration file in $SMX_HOME/etc will be CONFIG_PID.cfg
	 * </p>
	 */
	private static final String CONFIG_PID = "pnnl.goss.fusiondb.server";
		
	/**
	 * <p>
	 * Add logging to the class so that we can debug things effectively after deployment.
	 * </p>
	 */
	private static Logger log = LoggerFactory.getLogger(FusionDBServerActivator.class);

	/**
	 * <p>
	 * Allows tracking of the registration service from the core-server.
	 * </p>
	 */
	private ServiceTracker registrationTracker;
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Starting bundle"+this.getClass().getName());
		log.info("Starting bundle: " + this.getClass().getName());
		try {
			String filterStr = "(" + Constants.OBJECTCLASS + "=" + GossRequestHandlerRegistrationService.class.getName() + ")";
			Filter filter = context.createFilter(filterStr);
			registrationTracker = new ServiceTracker(context, filter, null);
			registrationTracker.open();
			
			// Register the handlers on the registration service.
			registerRequestAndHandlers();
			
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Register the service for receiving updates to the configuration file
		context.registerService(ManagedService.class.getName(), this, getDefaults());
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Hashtable getDefaults(){
		Hashtable properties= new Hashtable();
		properties.put(Constants.SERVICE_PID,  CONFIG_PID);
		//properties.put("datasource0", "datasource0=northandsouth,jdbc:mysql://localhost:3306/northandsouth,root,rootpass,com.mysql.jdbc.Driver");
		return properties;
	}
	
		@Override
	public void stop(BundleContext context) throws Exception {
			try {
				log.info("Stopping the bundle"+this.getClass().getName());
				System.out.println("Stopping the bundle"+this.getClass().getName());
				GossRequestHandlerRegistrationService registrationService = (GossRequestHandlerRegistrationService) registrationTracker.getService();

				if (registrationService != null) {
					registrationService.removeHandlerMapping(RequestActualTotalHandler.class);
					registrationService.removeHandlerMapping(RequestCapacityRequirementHandler.class);
					registrationService.removeHandlerMapping(RequestForecastTotalHandler.class);
					registrationService.removeHandlerMapping(RequestHAInterchangeScheduleHandler.class);
					registrationService.removeHandlerMapping(RequestRTEDScheduleHandler.class);
					
					registrationService.removeSecurityMapping(RequestActualTotal.class);
					registrationService.removeSecurityMapping(RequestCapacityRequirement.class);
					registrationService.removeSecurityMapping(RequestForecastTotal.class);
					registrationService.removeSecurityMapping(RequestHAInterchangeSchedule.class);
					registrationService.removeSecurityMapping(RequestRTEDSchedule.class);
				}
							
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}
	
	private void registerRequestAndHandlers(){
		GossRequestHandlerRegistrationService registrationService = (GossRequestHandlerRegistrationService) registrationTracker.getService();
		
		if(registrationService != null){
			// Registering service handlers here
			//-------------------------------------Fusion----------------------------------------------
			registrationService.addHandlerMapping(RequestActualTotal.class, RequestActualTotalHandler.class);
			registrationService.addHandlerMapping(RequestCapacityRequirement.class, RequestCapacityRequirementHandler.class);
			registrationService.addHandlerMapping(RequestForecastTotal.class, RequestForecastTotalHandler.class);
			registrationService.addHandlerMapping(RequestHAInterchangeSchedule.class, RequestHAInterchangeScheduleHandler.class);
			registrationService.addHandlerMapping(RequestRTEDSchedule.class, RequestRTEDScheduleHandler.class);
			
			//-------------------------------------Fusion Security----------------------------------------------
			registrationService.addSecurityMapping(RequestActualTotal.class, AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestCapacityRequirement.class, AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestForecastTotal.class, AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestHAInterchangeSchedule.class, AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestRTEDSchedule.class, AccessControlHandlerAllowAll.class);
		}
		else{
			log.debug(GossRequestHandlerRegistrationService.class.getName()+" not found!");
		}		
	}

}
