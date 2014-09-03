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
package pnnl.goss.demo.pmu;


import static pnnl.goss.core.GossCoreContants.PROP_DATASOURCES_CONFIG;

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

import pnnl.goss.demo.security.util.DemoConstants;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

@Instantiate
@Component(managedservice = PROP_DATASOURCES_CONFIG)
public class GossSecurityDemoActivator {

	public static final String PROP_DEMO_DEFAULT_POLL_FREQ = "demo.defaultPollFreq";
	public static final String PROP_DEMO_DEFAULT_POLL_INC = "demo.defaultPollInc";
	public static final String PROP_DEMO_DEFAULT_POLL_TIME_SHOWN = "demo.defaultPollTimeShown";
	public static final String PROP_DEMO_DEFAULT_START = "demo.defaultStart";
	
	/**
	 * <p>
	 * Allows the tracking of the goss registration service.
	 * </p>
	 */
	private GossRequestHandlerRegistrationService registrationService;
	private GossDataServices dataServices;
	
	/**
	 * <p>
	 * The configuration file in $SMX_HOME/etc will be CONFIG_PID.cfg
	 * </p>
	 */
	private static Logger log = LoggerFactory.getLogger(GossSecurityDemoActivator.class);

	public GossSecurityDemoActivator(
		@Requires GossRequestHandlerRegistrationService registrationService,
		@Requires GossDataServices dataServices){
		this.registrationService = registrationService;
		this.dataServices = dataServices;
		log.debug("Constructing goss-demo activator");
	}
	
	@Validate
	public void start(BundleContext context) throws Exception {
		System.out.println("Starting the "+this.getClass().getName()+" Activator");
		
		// Register for updates to the goss.core.security config file.
//        Hashtable<String, Object> properties = new Hashtable<String, Object>();
//        properties.put(Constants.SERVICE_PID, CONFIG_PID);
//        context.registerService(ManagedService.class.getName(), this, properties);
	}

	@Invalidate
	public void stop(BundleContext context) throws Exception {
		System.out.println("Stopping the "+this.getClass().getName()+" Activator");
		
	}

	@Updated
	public void updated(Dictionary properties) throws ConfigurationException {
		log.debug("Updating configuration for: "+this.getClass().getName());
		//TODO it would be nice if this could be on the GOSS Client so that it closes and restarts the session when this happens
//		DemoConstants.setProperties(properties);
		if (dataServices != null) {
			dataServices.registerData(PROP_DEMO_DEFAULT_POLL_FREQ, (String)properties.get(PROP_DEMO_DEFAULT_POLL_FREQ));
			dataServices.registerData(PROP_DEMO_DEFAULT_POLL_INC, (String)properties.get(PROP_DEMO_DEFAULT_POLL_INC));
			dataServices.registerData(PROP_DEMO_DEFAULT_POLL_TIME_SHOWN, (String)properties.get(PROP_DEMO_DEFAULT_POLL_TIME_SHOWN));
			dataServices.registerData(PROP_DEMO_DEFAULT_START, (String)properties.get(PROP_DEMO_DEFAULT_START));
		} else {
			log.error("dataServices is null!");
		}
	}
	
}
