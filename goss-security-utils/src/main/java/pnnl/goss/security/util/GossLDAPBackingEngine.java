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
package pnnl.goss.security.util;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.log4j.Logger;

import pnnl.goss.security.util.usermgmt.GossLDAPUtils;
import pnnl.goss.security.util.usermgmt.GossUserRoleMgmt;


public class GossLDAPBackingEngine implements BackingEngine {

	protected DirContext ctx;
	protected Logger log = Logger.getLogger(GossUserRoleMgmt.class);


	public void addGroup(String username, String groupname) {
		System.out.println("Add group not currently implemented");
		log.warn("Add group not currently implemented");
	}

	public void addGroupRole(String group, String role) {
		System.out.println("At this time groups are not supported");
	}

	public void addRole(String username, String role) {
		//verify valid user and group
		if(!checkExists("uid="+username+","+GossLDAPUtils.GOSS_LDAP_USER_OU)){
			throw new RuntimeException("Error adding user to role, user "+username+" doesn't exist");
		}
		if(!checkExists("cn="+role+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU)){
			throw new RuntimeException("Error adding user to role, role "+role+" doesn't exist");
		}
						
		List<GroupPrincipal> userGroups = listGroups(new UserPrincipal(username));
		for(GroupPrincipal group: userGroups){
			if(group.getName().equals(role)){
				log.warn("User already exists in role");
				return;
			}
		}
				
		// Specify the changes to make
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("member", "uid="+username));
					
		try {
			getLDAPContext().modifyAttributes("cn="+role+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU, mods);
		} catch (NamingException e) {
			log.error("Error while adding user to role");
			e.printStackTrace();
		}
	}

	public void addUser(String userName, String password) {
		String userOU = "uid="+userName+","+GossLDAPUtils.GOSS_LDAP_USER_OU;
		try {
			if(!checkExists(userOU)){
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "inetOrgPerson"));
				attrs.put(new BasicAttribute("cn", userName));
				attrs.put(new BasicAttribute("sn", userName));
				//set password
				attrs.put(new BasicAttribute("userPassword", password));
					
				getLDAPContext().createSubcontext(userOU, attrs);
				log.info("User "+userName +" created");
			} else {
				log.warn("User "+userName +" already exists");
			}
		} catch (NamingException e) {
			log.error("Error adding ldap user ",e);
			e.printStackTrace();
		}
	}

	public void deleteGroup(String username, String groupname) {
		log.warn("Delete group not currently implemented");
	}

	public void deleteGroupRole(String group, String role) {
		log.warn("Delete group role not currently implemented");
	}

	public void deleteRole(String username, String role) {
		// Specify the changes to make
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute("member", "uid="+username));
		try {
			getLDAPContext().modifyAttributes("cn="+role+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU, mods);
		} catch (NamingException e) {
			log.error("Error while removing user from group",e);
			e.printStackTrace();
		}
	}

	public void deleteUser(String username) {
		//	removeUserFromAllGroups
		List<GroupPrincipal> groups = listGroups(new UserPrincipal(username));
		for(GroupPrincipal group: groups){
			deleteGroup(username, group.getName());
		}
		List<RolePrincipal> roles = listRoles(new UserPrincipal(username));
		for(RolePrincipal role: roles){
			deleteRole(username, role.getName());
		}
		
		
		try {
			getLDAPContext().destroySubcontext("uid="+username+","+GossLDAPUtils.GOSS_LDAP_USER_OU);
		} catch (NamingException e) {
			log.error("Error while deleting user",e);
			e.printStackTrace();
		}
	}

	public List<GroupPrincipal> listGroups(UserPrincipal user) {
		List<GroupPrincipal> groups = new ArrayList<GroupPrincipal>();
//		NamingEnumeration<SearchResult> answer;
//		try {
//			answer = getLDAPContext().search(GossLDAPUtils.GOSS_LDAP_GROUP_OU, 
//				    "(member=uid\\="+user.getName()+")", null);
//			while(answer.hasMore()){
//				SearchResult r = answer.next();
//				String groupName = r.getName();
//				groupName = groupName.replace("cn=","");
//	        	groups.add(new GroupPrincipal(groupName));
//	        }
//		} catch (NamingException e) {
//			log.error("Error while getting list of groups for "+user.getName(),e);
//			e.printStackTrace();
//		}
		log.warn("List groups not currently implemented");
		return groups;
	}

	public List<RolePrincipal> listRoles(Principal user) {
		List<RolePrincipal> roles = new ArrayList<RolePrincipal>();
		NamingEnumeration<SearchResult> answer;
		try {
			answer = getLDAPContext().search(GossLDAPUtils.GOSS_LDAP_GROUP_OU, 
				    "(member=uid\\="+user.getName()+")", null);
			while(answer.hasMore()){
				SearchResult r = answer.next();
				String roleName = r.getName();
				roleName = roleName.replace("cn=","");
	        	roles.add(new RolePrincipal(roleName));
	        }
		} catch (NamingException e) {
			log.error("Error while getting list of roles for "+user.getName(),e);
			e.printStackTrace();
		}
		return roles;
	}

	public List<UserPrincipal> listUsers() {
		List<UserPrincipal> users = new ArrayList<UserPrincipal>();
		String usersOU = GossLDAPUtils.GOSS_LDAP_USER_OU;
		try {
			NamingEnumeration<NameClassPair> userList = getLDAPContext().list(usersOU);
			while(userList.hasMoreElements()){
				NameClassPair next = userList.nextElement();
				String name = next.getName();
				if(name.startsWith("uid=")){
					name = name.substring(4);
				}
				users.add(new UserPrincipal(name));
			}
		} catch (NamingException e) {
			log.error("Error retreiving list of users",e);
			e.printStackTrace();
		}
		
		return users;
	}

	protected DirContext getLDAPContext(){
		if(ctx==null){
			ctx = GossLDAPUtils.getLDAPContext();
		}
		return ctx;
	}
	
	
	protected boolean checkExists(String ouName){
		try {
			DirContext result = getLDAPContext().getSchema(ouName);
			if(result!=null){
				return true;
			}
		}catch (NameNotFoundException e) {
			return false;
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
}
