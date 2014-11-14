package pnnl.goss.fusiondb;

import static pnnl.goss.core.GossCoreContants.PROP_DATASOURCES_CONFIG;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.fusiondb.handlers.FusionUploadHandler;
import pnnl.goss.fusiondb.handlers.RequestActualTotalHandler;
import pnnl.goss.fusiondb.handlers.RequestCapacityRequirementHandler;
import pnnl.goss.fusiondb.handlers.RequestForecastTotalHandler;
import pnnl.goss.fusiondb.handlers.RequestGeneratorDataHandler;
import pnnl.goss.fusiondb.handlers.RequestHAInterchangeScheduleHandler;
import pnnl.goss.fusiondb.handlers.RequestInterfacesViolationHandler;
import pnnl.goss.fusiondb.handlers.RequestRTEDScheduleHandler;
import pnnl.goss.fusiondb.handlers.RequestVoltageStabilityViolationHandler;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.fusiondb.requests.RequestGeneratorData;
import pnnl.goss.fusiondb.requests.RequestHAInterchangeSchedule;
import pnnl.goss.fusiondb.requests.RequestInterfacesViolation;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;
import pnnl.goss.fusiondb.requests.RequestVoltageStabilityViolation;
import pnnl.goss.security.core.authorization.basic.AccessControlHandlerAllowAll;
import pnnl.goss.server.core.BasicDataSourceCreator;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

@Instantiate
@Component(managedservice = PROP_DATASOURCES_CONFIG)
public class FusionDBServerActivator {
	public static final String PROP_FUSIONDB_DATASERVICE = "goss/fusiondb";
	public static final String PROP_FUSIONDB_USER = "fusion.db.user";
	public static final String PROP_FUSIONDB_PASSWORD = "fusion.db.password";
	public static final String PROP_FUSIONDB_URI = "fusion.db.uri";

	/**
	 * <p>
	 * Add logging to the class so that we can debug things effectively after
	 * deployment.
	 * </p>
	 */
	private static Logger log = LoggerFactory
			.getLogger(FusionDBServerActivator.class);

	private GossRequestHandlerRegistrationService registrationService;
	private GossDataServices dataServices;
	@Requires
	private BasicDataSourceCreator datasourceCreator;

	private String user;
	private String password;
	private String uri;

	public FusionDBServerActivator(
			@Requires GossRequestHandlerRegistrationService registrationService,
			@Requires GossDataServices dataServices) {
		this.registrationService = registrationService;
		this.dataServices = dataServices;
		log.debug("Constructing activator");
	}

	private void registerFusionDataService() {
		if (dataServices != null) {
			if (!dataServices.contains(PROP_FUSIONDB_DATASERVICE)) {
				log.debug("Attempting to register dataservice: "
						+ PROP_FUSIONDB_DATASERVICE);
				String user = dataServices.getPropertyValue(PROP_FUSIONDB_USER);
				String uri = dataServices.getPropertyValue(PROP_FUSIONDB_URI);
				String password = dataServices.getPropertyValue(PROP_FUSIONDB_PASSWORD);
				if (datasourceCreator == null){
					datasourceCreator = new BasicDataSourceCreator();
				}
				if (datasourceCreator != null){
					try {
						dataServices.registerData(PROP_FUSIONDB_DATASERVICE,
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
	
	public void update() throws IllegalStateException{
		if (dataServices == null){
			throw new IllegalStateException("dataservices cannot be null!");
		}
		
		registerFusionDataService();
	}
	
/*
	@SuppressWarnings("rawtypes")
	@Updated
	public void update(Dictionary config) {
		log.debug("updating");
		user = (String) config.get(PROP_FUSIONDB_USER);
		password = (String) config.get(PROP_FUSIONDB_PASSWORD);
		uri = (String) config.get(PROP_FUSIONDB_URI);
		
		boolean valid = true;
		
		if (user == null || user.isEmpty()){
			log.error("Invalid user in config property: " + PROP_FUSIONDB_USER);
			valid = false;
		}
		if (password == null || password.isEmpty()){
			log.error("Invalid password in config property: " + PROP_FUSIONDB_PASSWORD);
			valid = false;
		}
		if (uri == null || uri.isEmpty()){
			log.error("Invalid uri in config proeprty: "+PROP_FUSIONDB_URI);
			valid = false;
		}
			
		if (valid){
			log.debug("updated uri: " + uri + "\n\tuser:" + user);			
			registerFusionDataService();
		}
	}
*/
	@Validate
	public void start() {
		log.info("Starting bundle: " + this.getClass().getName());
		
		if (registrationService != null) {
			registrationService.addHandlerMapping(RequestActualTotal.class,
					RequestActualTotalHandler.class);
			registrationService.addHandlerMapping(
					RequestCapacityRequirement.class,
					RequestCapacityRequirementHandler.class);
			registrationService.addHandlerMapping(RequestForecastTotal.class,
					RequestForecastTotalHandler.class);
			registrationService.addHandlerMapping(
					RequestHAInterchangeSchedule.class,
					RequestHAInterchangeScheduleHandler.class);
			registrationService.addHandlerMapping(RequestRTEDSchedule.class,
					RequestRTEDScheduleHandler.class);
			registrationService.addHandlerMapping(RequestGeneratorData.class, 
					RequestGeneratorDataHandler.class);
			registrationService.addHandlerMapping(RequestInterfacesViolation.class,
					RequestInterfacesViolationHandler.class);
			registrationService.addHandlerMapping(RequestVoltageStabilityViolation.class, 
					RequestVoltageStabilityViolationHandler.class);
			
			registrationService.addUploadHandlerMapping("CapacityRequirement", 
					FusionUploadHandler.class);
			registrationService.addUploadHandlerMapping("fusion_GeneratorData", 
					FusionUploadHandler.class);
			registrationService.addUploadHandlerMapping("InterfacesViolation", 
					FusionUploadHandler.class);
			registrationService.addUploadHandlerMapping("VoltageStabilityViolation", 
					FusionUploadHandler.class);
			
			
			
			// Fusion Security----------------------------------------------
			registrationService.addSecurityMapping(RequestActualTotal.class,
					AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestCapacityRequirement.class,
					AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestForecastTotal.class,
					AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestHAInterchangeSchedule.class,
					AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestRTEDSchedule.class,
					AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestInterfacesViolation.class,
					AccessControlHandlerAllowAll.class);
			registrationService.addSecurityMapping(RequestVoltageStabilityViolation.class,
					AccessControlHandlerAllowAll.class);
			
			// Fusion Launchers----------------------------------------------
			//DataStreamLauncher launcher = new DataStreamLauncher(registrationService, dataServices);
			//launcher.startLauncher();
			
			update();
			
		} else {
			log.error(GossRequestHandlerRegistrationService.class.getName()
					+ " not found!");
		}
	}

	@Invalidate
	public void stop() {
		try {
			log.info("Stopping the bundle" + this.getClass().getName());
			if (registrationService != null) {
				registrationService
						.removeHandlerMapping(RequestActualTotalHandler.class);
				registrationService
						.removeHandlerMapping(RequestCapacityRequirementHandler.class);
				registrationService
						.removeHandlerMapping(RequestForecastTotalHandler.class);
				registrationService
						.removeHandlerMapping(RequestHAInterchangeScheduleHandler.class);
				registrationService
						.removeHandlerMapping(RequestRTEDScheduleHandler.class);

				registrationService
						.removeSecurityMapping(RequestActualTotal.class);
				registrationService
						.removeSecurityMapping(RequestCapacityRequirement.class);
				registrationService
						.removeSecurityMapping(RequestForecastTotal.class);
				registrationService
						.removeSecurityMapping(RequestHAInterchangeSchedule.class);
				registrationService
						.removeSecurityMapping(RequestRTEDSchedule.class);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dataServices != null) {
				dataServices.unRegisterData(PROP_FUSIONDB_DATASERVICE);
			}
		}
	}

}
