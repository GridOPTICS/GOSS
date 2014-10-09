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
package pnnl.goss.server.core;

import java.util.Dictionary;

import javax.jms.JMSException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
//import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.server.core.internal.GridOpticsServer;
import static pnnl.goss.core.GossCoreContants.*;

@Component(managedservice=PROP_CORE_CONFIG)
@Instantiate
public class GossServer {

	private static final Logger log = LoggerFactory.getLogger(GossServer.class);
	private GridOpticsServer gossServer;
	
	@Requires
	private GossRequestHandlerRegistrationService service;
	
	@Requires
	private BundleContext context;
	
	/**
	 * A listener for .class elements so that we can attempt to load annotations for
	 * the handlers.
	 */
	private BundleListener classListener;
	
	public GossServer(){
		log.debug("Constructing");
	}
	
	@Validate
	public void start(){
		log.debug("Starting Bundle");
		if (context != null){
			classListener = new BundleListener() {				
				@SuppressWarnings("unchecked")
				@Override
				public void bundleChanged(BundleEvent event) {
					
					if (event.getType() == BundleEvent.INSTALLED){
						service.registerHandlers(event.getBundle().findEntries("/", "*.class", true));
					}
					else {
						service.unregisterHandlers(event.getBundle().findEntries("/", "*.class", true));
					}
					log.debug("change event: " + event.toString());
				}
			};
			
			log.debug("Adding bundle listener.");
			context.addBundleListener(classListener);
		}
		else{
			log.debug("Not in osgi environment.");
		}
	}
	
	@Invalidate
	public void stop() throws Exception {
		log.debug("Stopping Bundle");
		try {
			if (classListener != null){
				log.debug("Removing bundle listener.");
				
				if (context != null) {
					context.removeBundleListener(classListener);
				}
				else{
					log.error("Invalid bundle context!");
				}
			}
			
			if (gossServer != null) {
				gossServer.close();
				gossServer = null;
//				GossServerActivator.bundleContext = null;
			}
		} catch (Exception ex) {
			log.error("Shutdown error!", ex);
			ex.printStackTrace();
		}
	}
	
	
//	private class GossServerConfigUpdater implements ManagedService {

	@Updated
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config){ // throws ConfigurationException {
		log.debug("Updating Goss Server configuration");
			
		if (gossServer != null){
			try {
				gossServer.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}
		
		// first time through update.
		if (config == null || config.size() < 2) {
			return;
		}
		
		service.setCoreServerConfig(config);
		gossServer = new GridOpticsServer(service, false);			
	}
}