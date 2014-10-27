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
package pnnl.goss.security.core.activemq;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.ObjectMessage;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.Message;
import org.apache.activemq.jaas.UserPrincipal;
import org.apache.activemq.security.MessageAuthorizationPolicy;
import org.apache.log4j.Logger;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.security.core.internal.GossSecurityHandlerImpl;


public class GossAuthPolicy implements MessageAuthorizationPolicy {
	
	//TODO need to find a way to age off old mappings so it doesn't continue to grow
	protected static Map<String, List<String>> tempTopicRoles = new HashMap<String, List<String>>();
	protected Logger log = Logger.getLogger(GossAuthPolicy.class);
	
	//@Override
	public boolean isAllowedToConsume(ConnectionContext context, Message message) {
		/*
		 * Authorization will be always allowed on certain queues, such as those for sending requests and notifications about temporary queues, however access to data received on the temporary queues will be restricted on a per message basis as described below.
		 *	1) request for data received (but authorization is for the receiver of the message (the GOSS server)), however we know what topic the response will be sent on, so when the request is received by the ServerListener, we check to see which roles are allowed to view the response, and store these roles mapped to the topic name.
		 *	2) response is received on the temp topic, authorization is for the user receiving the data.  It checks the previously mapped topic/role mappings to see if the user is in any of the roles for the current topic.
		 */
		
		
		try{
			if(message instanceof ObjectMessage){
				Object o = 	((ObjectMessage)message).getObject();
				
				//Request received,allow it, only need to check when receiving data response.  The topic->allowed role mappings will be stored when
				// security check is called by the serverListener
				if(o instanceof Request){
					return true;
				}
				
				
				if(o instanceof DataResponse){
					
					
					//Only allow to consume if the user is in one of the valid roles for the request that generated this response 
					//  (identified by the temporary queue name)
					String tempDestination = message.getDestination().getQualifiedName();
					log.info("User "+getUserName(context.getSecurityContext().getPrincipals())+" receiving data response on "+tempDestination);
					
					if(tempDestination!=null){
						return GossSecurityHandlerImpl.checkAllowedRolesForTopic(tempDestination, context.getSecurityContext().getPrincipals());
					}
					
					log.warn("Returning false for data response, because there wasn't a temp destination to verify");
					return false;
				}
				
				return true;
			} else if(message instanceof ActiveMQMessage){
				//Always allow to consume the activeMQ messages
				// Mostly used for setting up asynchronous responses, it appears that the users calls this first, then the system
				//VERIFY THIS
				return true;
			} else {
				log.warn("GOSSAuthPolicy received unknown message type, don't know what to do with it: "+message.getClass());
				return false;
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("Error occurred while authorizing message receipt "+e.getMessage());
		}
		return false;
	}

	
	private String getUserName(Set<Principal> principals){
		for(Principal p: principals){
			if(p instanceof UserPrincipal){
				return p.getName();
			}
		}
		return null;
	}
}
