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
package pnnl.goss.server.core.internal;

import java.util.Dictionary;
import java.util.HashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.security.core.GossSecurityHandler;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.GossRequestHandler;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

@SuppressWarnings("rawtypes")
@Instantiate
@Provides
@Component(immediate=true)
public class GossRequestHandlerRegistrationImpl implements GossRequestHandlerRegistrationService {

	private static final Logger log = LoggerFactory.getLogger(GossRequestHandlerRegistrationImpl.class);
	private HashMap<String, String> handlerMap = new HashMap<String, String>();
	private HashMap<String, Class> handlerToClasss = new HashMap<String, Class>();
	private GossSecurityHandler securityHandler;
	private Dictionary coreServerConfig = null;
	@Requires 
	private GossDataServices dataServices;
	
	public GossRequestHandlerRegistrationImpl(@Requires GossDataServices dataServices){
		log.debug("Constructing");
		this.dataServices = dataServices;
	}
	
//	@Property
//	public void setSecurityHandler(GossSecurityHandler securityHandler){
//		this.securityHandler = securityHandler;
//	}
	@Validate
	public void startHandler(){
		log.debug("Starting handler");
	}

//	public GossRequestHandlerRegistrationImpl(GossSecurityHandler securityHandler){
//		System.out.println("CONSTRUCTING "+getClass());
//		//this.securityHandler = securityHandler;
//	}
	@Invalidate
	public void shutdown(){
		log.debug("shutdown");
		this.handlerMap.clear();
	}
	
	public void addHandlerMapping(String request, String handler) {
		if (request == null || handler == null) {
			log.error("request and handler must not be null!");
			return;
		}
		
		log.debug("adding handler mapping: "+request+" -> "+ handler);
		try {
			Class requestCls = Class.forName(request);
			Class handlerCls = Class.forName(handler);
			
			addHandlerMapping(requestCls, handlerCls);
			
		} catch (ClassNotFoundException e) {
			log.error("Error with class not found", e);
		}
	}
	
	
	public void addHandlerMapping(Class request, Class handler) {
		if (request == null || handler == null) {
			log.error("request and handler must not be null!");
			return;
		}

		try {
			log.debug("add handler mapping.\n\tRequest: " + request.getName() + "\n\tHandler: " + handler.getName());

			// Attempt to instantiate class before adding the string
			handler.newInstance();
			
			Class superClassTester = request.getSuperclass();
			boolean foundSuperClassRequest = false;
			boolean foundSuperClassHandler = false;

			while(superClassTester != null){
				
				if(superClassTester.equals(Request.class) || superClassTester.equals(RequestAsync.class) || superClassTester.equals(UploadRequest.class)){
					foundSuperClassRequest = true;
					break;
				}
				superClassTester = superClassTester.getSuperclass();
			}
			
			superClassTester = handler.getSuperclass();
			
			while(superClassTester != null){
				
				if(superClassTester.equals(GossRequestHandler.class)){
					foundSuperClassHandler = true;
					break;
				}
				superClassTester = superClassTester.getSuperclass();
			}
			
			if(!foundSuperClassHandler){
				throw new Exception("Invalid handler, must be subclass of "+GossRequestHandler.class.toString());
			}
			
			if(!foundSuperClassRequest){
				throw new Exception("Invalid request, must be subclass of "+Request.class.toString());
			}
			
			// Keep the string of the class.
			handlerMap.put(request.getName(), handler.getName());
			handlerToClasss.put(request.getName(), handler);

		} catch (InstantiationException e) {
			log.error("Couldn't instantiate " + handler.getName(), e);
		} catch (IllegalAccessException e) {
			log.error("Access error couldn't instantiate " + handler.getName(), e);
		} catch (Exception e) {
			log.error("AddHandlerMapping Exception ", e);
		}
	}
	
	/**
	 * Creates mapping between upload data type and corresponding RequestHandler class.
	 * @param dataType 
	 * 				String representing upload data type
	 * @param handler
	 * 				RequestHandler class name
	 */
	//TODO: complete data type and handler mapping.
	public void addUploadHandlerMapping(String dataType, String handler) {
		if (handler == null || dataType==null) {
			log.error("data type and handler must not be null!");
			return;
		}
		
		try {
			Class handlerCls = Class.forName(handler);
			addUploadHandlerMapping(dataType, handlerCls);
			
		} catch (ClassNotFoundException e) {
			log.error("Error with class not found", e);
		}
	}
	
	
	/**
	 * Creates mapping between upload data type and corresponding RequestHandler class.
	 * @param dataType 
	 * 				String representing upload data type
	 * @param handler
	 * 				RequestHandler class
	 */
	//TODO: complete data type and handler mapping.
	public void addUploadHandlerMapping(String dataType, Class handler) {
		if (handler == null || dataType==null) {
			log.error("data type and handler must not be null!");
			return;
		}
		
		try {
			
			// Attempt to instantiate class before adding the string
			handler.newInstance();
			
			Class superClassTester = handler.getSuperclass();
			boolean foundSuperClassHandler = false;
			
			while(superClassTester != null){
				
				if(superClassTester.equals(GossRequestHandler.class)){
					foundSuperClassHandler = true;
					break;
				}
				superClassTester = superClassTester.getSuperclass();
			}
			
			if(!foundSuperClassHandler){
				throw new Exception("Invalid handler, must be subclass of "+GossRequestHandler.class.toString());
			}
			
			// Keep the string of the class.
			handlerMap.put(dataType, handler.getName());

			
		} catch (ClassNotFoundException e) {
			log.error("Error with class not found", e);
		} catch (IllegalAccessException e) {
			log.error("Access error couldn't instantiate " + handler.getName(), e);
		} catch (Exception e) {
			log.error("AddUploadHandlerMapping Exception ", e);
		}
	}
	
	
	public void removeHandlerMapping(Class request) {
		if (request != null) {
			log.debug("removing mapping for: " + request.getName());

			if (handlerMap.containsKey(request)) {
				handlerMap.remove(request);
			}
			
			if (handlerToClasss.containsKey(request)){
				handlerToClasss.remove(request);
			}
		}
	}

	public Response handle(Request request) {
		Response response = null;
		GossRequestHandler handler = null;
		if (request != null) {
			log.debug("handling request for:\n\t " + request.getClass().getName() + " => " + handlerMap.get(request.getClass().getName()));

			if (handlerMap.containsKey(request.getClass().getName())) {
				try {
					Class handlerClass = handlerToClasss.get(request.getClass().getName());
					//Class handlerClass = Class.forName(handlerMap.get(request.getClass().getName()));
					handler = (GossRequestHandler) handlerClass.newInstance();
					if(handler!=null){
						handler.setGossDataservices(dataServices);
						handler.setHandlerService(this);
						response = handler.handle(request);
					}
					/*
					 * String handlerStr =
					 * handlerMap.get(request.getClass().getName());
					 * GossRequestHandler handler = (GossRequestHandler)
					 * Class.forName(handlerStr).newInstance(); response =
					 * handler.handle(request);
					 */
				} catch (Exception e) {
					log.error("Handle error exception", e);
				}
				/*
				 * catch (InstantiationException e) { log.error(e, e);
				 * e.printStackTrace(); } catch (IllegalAccessException e) {
				 * log.error(e, e); e.printStackTrace(); } catch
				 * (ClassNotFoundException e) { log.error(e, e);
				 * e.printStackTrace(); }
				 */
			}
		}

		if (handler == null) {
			log.debug("Passed handler object instance was null!");
			response = new DataResponse(new DataError("Handler mapping for: " + request.getClass().getName() + " not found!"));
		}
		if (response == null) {
			log.debug("Passed response object instance was null!");
			response = new DataResponse(new DataError("Empty response for: " + request.getClass().getName() + "!"));
		}
		return response;
	}
	
	@Override
	public Response handle(Request request, String dataType) {
		Response response = null;
		GossRequestHandler handler = null;
		if (dataType != null) {
			log.debug("handling request for: " + dataType);
			if (handlerMap.containsKey(dataType)) {
				try {
					Class handlerClass = Class.forName(handlerMap.get(dataType));
					handler = (GossRequestHandler) handlerClass.newInstance();
					if(handler!=null){
						handler.setGossDataservices(dataServices);
						handler.setHandlerService(this);
						response = handler.handle(request);
					}
					/*
					 * String handlerStr =
					 * handlerMap.get(request.getClass().getName());
					 * GossRequestHandler handler = (GossRequestHandler)
					 * Class.forName(handlerStr).newInstance(); response =
					 * handler.handle(request);
					 */
				} catch (Exception e) {
					log.error("Handle error exception", e);
				}
				/*
				 * catch (InstantiationException e) { log.error(e, e);
				 * e.printStackTrace(); } catch (IllegalAccessException e) {
				 * log.error(e, e); e.printStackTrace(); } catch
				 * (ClassNotFoundException e) { log.error(e, e);
				 * e.printStackTrace(); }
				 */
			}
		}
		return response;
	}
	
	public boolean checkAccess(Request request, String userPrincipals, String tempDestination) {
		return securityHandler.checkAccess(request, userPrincipals, tempDestination);
	}

	
	public void addSecurityMapping(Class request, Class handler) {
		if (securityHandler != null){
			securityHandler.addHandlerMapping(request, handler);
		}
		else{
			log.error("Security handler is null!");
		}
	}

	
	public void removeSecurityMapping(Class request) {
		log.debug("Removing security mapping for: "+ request.getClass().getName());
		if(securityHandler != null){
			securityHandler.removeHandlerMapping(request);
		}
		else{
			log.error("Security handler is null!");
		}
	}

	
	public Dictionary getCoreServerConfig() {
		return coreServerConfig;
	}

	
	public void setCoreServerConfig(Dictionary config) {
		coreServerConfig = config;
		
	}

	
	public GossRequestHandler getHandler(Request request) {
		GossRequestHandler handler = null;
		if (request != null) {
			log.debug("handling request for: " + request.getClass().getName());

			if (handlerMap.containsKey(request.getClass().getName())) {
				try {
					Class handlerClass = Class.forName(handlerMap.get(request.getClass().getName()));
					handler = (GossRequestHandler) handlerClass.newInstance();
				} catch (Exception e) {
					log.error("Handle error exception", e);
				}
			}
		}
		return handler;
		
	
	}

}
