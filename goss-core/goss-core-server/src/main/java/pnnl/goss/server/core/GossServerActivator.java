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
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.server.core.internal.GridOpticsServer;
@Component(architecture=true, managedservice=GossServerActivator.CONFIG_PID)
@Instantiate
public class GossServerActivator {

	private static final Logger log = LoggerFactory.getLogger(GossServerActivator.class);
//	private static BundleContext bundleContext;
	
	protected static final String CONFIG_PID = "pnnl.goss.core";
	

	private GridOpticsServer gossServer;
	
	@Requires
	private GossRequestHandlerRegistrationService service;

	
	
	@Validate
	public void start() throws Exception {
		log.info("Starting: " + this.getClass().getName());
		System.out.println("Starting the core server bundle");
		try {
//			bundleContext = context;
//
//			log.debug("Registering Request Handler Registration Service");
//			service = new GossRequestHandlerRegistrationImpl();
//			context.registerService(GossRequestHandlerRegistrationService.class.getName(), service, null);
//
			// Register for updates to the goss.core.server config file.
//			Hashtable<String, Object> properties = new Hashtable<String, Object>();
//			properties.put(Constants.SERVICE_PID, CONFIG_PID);
//			context.registerService(ManagedService.class.getName(), new GossServerConfigUpdater(), properties);
			
			
			
			
			//will be initialize in the config updated()
//			URI uri = URI.create("tcp://localhost:61616");
//			gossServer = new GridOpticsServer(uri, service);
			//gossServer = new GridOpticsServer(factory, service); //(uri, service);
			
		} catch (Exception ex) {
			log.error("Startup error!", ex);
			ex.printStackTrace();
		}

	}

	@Invalidate
	public void stop() throws Exception {
		System.out.println("Stopping the core server bundle");
		try {
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
	public void updated(Dictionary config) throws ConfigurationException {
		System.out.println("Updating Goss Server configuration");
			
		if (gossServer != null){
			try {
				gossServer.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}
			
		if (config == null) {
			return;
		}
		String brokerURI = (String)config.get("brokerURI");
		log.info("BrokerURI from config: "+brokerURI);
		service.setCoreServerConfig(config);
		gossServer = new GridOpticsServer(service, false);
		log.info("Started GOSS Server: "+brokerURI);
			
	}

//	}


	/*
	 * private static GridOpticsServer server = null; private static final
	 * String CONFIG_PID = "goss.core.server"; // Set to the registerd service
	 * for monitoring the configuration of the service. private
	 * ServiceRegistration serviceReg; private Dictionary currentConfiguration;;
	 * private URI currentBrokerUri; private Thread thread; private
	 * ServiceTracker tracker; // Tracker for classes that implement
	 * AbstractServiceHandler
	 * 
	 * private void restartBroker(final URI newBrokerUri){
	 * 
	 * 
	 * if (serverThread != null){ try { server.shutdown(); serverThread.join();
	 * } catch (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch(JMSException e){ e.printStackTrace(); } }
	 * 
	 * serverThread = new Thread(new Runnable() { public void run(){ // Create a
	 * new goss server instance. server = new GridOpticsServer(newBrokerUri);
	 * currentBrokerUri = newBrokerUri; } });
	 * 
	 * serverThread.start();
	 * 
	 * 
	 * if (server != null){ try { System.out.println("Restarting broker");
	 * server.shutdown(); } catch (JMSException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } } else{
	 * System.out.println("Starting broker"); }
	 * 
	 * // Create a new goss server instance. server = new
	 * GridOpticsServer(newBrokerUri); currentBrokerUri = newBrokerUri;
	 * 
	 * }
	 * 
	 * public void start(BundleContext context) { tracker = new
	 * ServiceTracker(context, AbstractRequestHandler.class.getName(), null);
	 * tracker.open(); thread = new Thread(this, "GossServer Whiteboard");
	 * thread.start();
	 * 
	 * Hashtable<String, Object> properties = new Hashtable<String, Object>();
	 * properties.put(Constants.SERVICE_PID, CONFIG_PID); serviceReg =
	 * context.registerService(ManagedService.class.getName(), new
	 * ConfigUpdater(), properties); System.out.println("Starting the bundle: "+
	 * getClass().getName()); }
	 * 
	 * public void stop(BundleContext context) { tracker.close(); thread = null;
	 * 
	 * serviceReg.unregister(); System.out.println("Stopping the bundle"); try {
	 * if (server != null) { server.shutdown(); } } catch (JMSException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); } finally{ server =
	 * null; } }
	 * 
	 * 
	 * 
	 * Internal class to deal with a change in this classes configuration
	 * scheme.
	 * 
	 * private class ConfigUpdater implements ManagedService {
	 * 
	 * @SuppressWarnings("rawtypes") public void updated(Dictionary config)
	 * throws ConfigurationException {
	 * System.out.println("Updating configuration"); if (config == null){
	 * return; }
	 * 
	 * System.out.println("New broker is: "+config.get("brokerURI"));
	 * 
	 * URI newBrokerUri = URI.create((String) config.get("brokerURI"));
	 * 
	 * if(currentBrokerUri == null || !currentBrokerUri.equals(newBrokerUri)) {
	 * restartBroker(newBrokerUri); }
	 * 
	 * } }
	 * 
	 * 
	 * public synchronized void run() {
	 * 
	 * Thread current = Thread.currentThread(); int n = 0;
	 * 
	 * while (current == thread){ Object[] providers = tracker.getServices(); if
	 * (providers != null && providers.length > 0){ if (n >= providers.length){
	 * n = 0; }
	 * 
	 * 
	 * } }
	 * 
	 * 
	 * }
	 */

}