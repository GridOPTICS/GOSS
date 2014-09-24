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
package pnnl.goss.security.core.internal;

import static pnnl.goss.core.GossCoreContants.PROP_CORE_CONFIG;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.jaas.GroupPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Request;
import pnnl.goss.security.core.GossSecurityHandler;
import pnnl.goss.security.core.authorization.AbstractAccessControlHandler;

public class GossSecurityHandlerImpl implements GossSecurityHandler {

	protected static Logger log = LoggerFactory.getLogger(GossSecurityHandler.class);
	/*
	 * We can't use Class here because of the way osgi bundles get resolved.
	 */
	private static HashMap<Class, Class> handlerMap = new HashMap<Class, Class>();
	protected static Map<String, List<String>> tempTopicRoles = new HashMap<String, List<String>>();
		
	public void startingHandler(){
		log.debug("Starting handler");
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.security.core.GossSecurityHandler#checkAccess(pnnl.goss.core.Request, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean checkAccess(Request request, String userPrincipals, String tempDestination) {
		//FIND HANDLER FOR REQUEST TYPE, IF NOT FOUND GIVE A WARNING BUT ALLOW ACCESS
		List<String> allowedRoles = getAllowedRoles(request);
		if(tempDestination!=null){
			tempTopicRoles.put(tempDestination, allowedRoles);
		}
		
		//CHECK PRINCIPALS TO SEE IF ROLE IS ALLOWED FOR USER
		List<String> userRoles = parseRoles(userPrincipals);
		if(allowedRoles==null){
			log.warn("WARNING: ALLOWED ROLES IS NULL BECAUSE NO AUTH HANDLER REGISTERED");
			return false;
		}
		//Make sure that user has all of the required roles for the request
		for(String allowedRole: allowedRoles){
			boolean allowed = true;
			String[] roles = allowedRole.split(":");
			for(String role: roles){
				if(!userRoles.contains(role)){
					allowed = false;
					break;
				}
			}
			if(allowed){
				//Valid role found, return true
				log.debug("Valid role found "+allowedRole+", allowing access");
				return true;
			}
		}
		
		
		return false;
	}
	public static List<String> getAllowedRoles(Request request){
		List<String> allowedRoles = null;
		
		if(handlerMap.containsKey(request.getClass())){
			log.info("Security handler "+handlerMap.get(request.getClass().getName())+" found for request type "+request.getClass());
			
			//USE HANDLER TO GET ALLOWED ROLES
			try {
				//Class handlerClass = Class.forName(handlerMap.get(request.getClass().getName()));
				Class handlerClass =  handlerMap.get(request.getClass());
				AbstractAccessControlHandler handler = (AbstractAccessControlHandler) handlerClass.newInstance();
				allowedRoles = handler.getAllowedRoles(request);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("InstantiationException", e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("IllegalAccessException", e);
			}
		 } else {
			 log.warn("Warning: Security handler class mapping not found for "+request.getClass());
			 System.out.println("WARNING: SECURITY HANDLER CLASS MAPPING NOT FOUND FOR "+request.getClass());
			 return null;
		 }
		return allowedRoles;
	}
	

	public static boolean checkAllowedRolesForTopic(String tempTopic, Set<Principal> principals){
		if(tempTopicRoles.containsKey(tempTopic)){
			List<String> allowedRoles = tempTopicRoles.get(tempTopic);
			List<String> userRoles = getRolesFromPrincipal(principals);
			for(String allowedRole: allowedRoles){
				boolean allowed = true;
				String[] roles = allowedRole.split(":");
				for(String role: roles){
					if(!userRoles.contains(role)){
						allowed = true;
						break;
					}
				}
				if(allowed){
					//Valid role found, return true
					log.info("Valid role found in checkAllowed for topic "+tempTopic+", "+allowedRole+", allowing access");
					return true;
				}
			}
		}
		return false;
	}
	
	private static List<String> getRolesFromPrincipal(Set<Principal> principals){
		List<String> roles = new ArrayList<String>();
		for(Principal p: principals){
			if (p instanceof GroupPrincipal){
				roles.add(((GroupPrincipal)p).getName());
			}
		}
		return roles;
	}
	
	public static void addHandlerMapping(String packageName) {
		// TODO scan the package for data

	}

	/* (non-Javadoc)
	 * @see pnnl.goss.security.core.GossSecurityHandler#addHandlerMapping(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHandlerMapping(String requestClass,
			String handlerClass) {
		
		try {
			// Uses the context loader to make sure that the class is available
			// on the class path.  The addHandlerMapping will then make sure
			// that a handler instance can be created.
			Class request = Class.forName(requestClass);
			Class handler = Class.forName(handlerClass);

			addHandlerMapping(request, handler);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see pnnl.goss.security.core.GossSecurityHandler#removeHandlerMapping(java.lang.Class)
	 */
	@Override
	public void removeHandlerMapping(Class request) {
		// TODO Is it an error to remove a mapping that doesn't exist?
		if (handlerMap.containsKey(request)) {
			handlerMap.remove(request);
		}
	}

	/* (non-Javadoc)
	 * @see pnnl.goss.security.core.GossSecurityHandler#addHandlerMapping(java.lang.Class, java.lang.Class)
	 */
	@Override
	public void addHandlerMapping(Class request, Class handler) {
		log.debug("Attempting to add security handler mapping for "+request.getName()+" to "+handler.getName());
		
		try {
			
			// Attempt to instantiate class before adding the string
			handler.newInstance();
			
			Class superClassTester = request.getSuperclass();
			boolean foundSuperClassRequest = false;
			boolean foundSuperClassHandler = false;

			while(superClassTester != null){
				
				if(superClassTester.equals(Request.class)){
					foundSuperClassRequest = true;
					break;
				}
				superClassTester = superClassTester.getSuperclass();
			}
			
			// Now check for handler superclasses.
			superClassTester = handler.getSuperclass();
			
			
			while(superClassTester != null){
				
				if(superClassTester.equals(AbstractAccessControlHandler.class)){
					foundSuperClassHandler = true;
					break;
				}
				superClassTester = superClassTester.getSuperclass();
			}
			
			// If either the abstract handler class or the request class aren't in the
			// passed classes chains then throw the exception.
			if (!foundSuperClassRequest || !foundSuperClassHandler){
				
				log.error(request.getName()+" and "+handler.getName()+" classes must be subclasses of "
						+ "Request and AbstractAccessControlHandler.");
				throw new Exception(
						request.getName()+" and "+handler.getName()+" classes must be subclasses of Request and AbstractAccessControlHandler.");
				
			} else {
				log.debug("Added security handler mapping for "+request.getName()+" to "+handler.getName());
				// Keep the string of the class.
				handlerMap.put(request, handler);
				//handlerMap.put(request, handler);
			}
		} catch (Exception e) {
			log.error("Adding handler", e);
			e.printStackTrace();
		}

	}
	
	
	private static List<String> parseRoles(String rolesStr){
		List<String> roles = new ArrayList<String>();
		if(rolesStr==null)
			return new ArrayList<String>();
		
		rolesStr = rolesStr.replace("[", "");
		rolesStr = rolesStr.replace("]", "");
		for(String s: rolesStr.split(",")){
			roles.add(s.trim());
		}
		return roles;
	}
	
	
}
