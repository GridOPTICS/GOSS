package goss.pnnl.fusiondb;

import goss.pnnl.fusiondb.handlers.RequestActualTotalHandler;
import goss.pnnl.fusiondb.handlers.RequestCapacityRequirementHandler;
import goss.pnnl.fusiondb.handlers.RequestForecastTotalHandler;
import goss.pnnl.fusiondb.handlers.RequestHAInterchangeScheduleHandler;
import goss.pnnl.fusiondb.handlers.RequestRTEDScheduleHandler;

import java.util.Dictionary;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.fusiondb.requests.RequestHAInterchangeSchedule;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

@Component(managedservice="pnnl.goss.fusiondb.server")
@Instantiate
public class FusionDBServerActivator {

	/**
	 * <p>
	 * The configuration file in $SMX_HOME/etc will be CONFIG_PID.cfg
	 * </p>
	 */
	//private static final String CONFIG_PID = "pnnl.goss.fusiondb.server";
		
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
	//private ServiceTracker registrationTracker;
	
	@Requires
	private GossRequestHandlerRegistrationService registrationService;

	@Validate
	public void start() {
		System.out.println("Starting bundle"+this.getClass().getName());
		log.info("Starting bundle: " + this.getClass().getName());
		if(registrationService != null){
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
		}else{
			log.debug(GossRequestHandlerRegistrationService.class.getName()+" not found!");
		}
	}

	@Invalidate
	public void stop(BundleContext context) throws Exception {
			try {
				log.info("Stopping the bundle"+this.getClass().getName());
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

	@Updated
	public void updated(Dictionary properties) throws ConfigurationException {
		log.info("Updating configuration if required for "+this.getClass().getName());
	}
	
}
