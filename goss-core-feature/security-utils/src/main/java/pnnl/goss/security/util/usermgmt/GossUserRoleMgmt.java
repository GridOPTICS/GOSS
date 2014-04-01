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
package pnnl.goss.security.util.usermgmt;


import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

public class GossUserRoleMgmt {

	protected DirContext ctx;

	protected Logger log = Logger.getLogger(GossUserRoleMgmt.class);

	
	public static void main(String[] args){
		
		GossUserRoleMgmt u = new GossUserRoleMgmt();

		
		u.initializeGOSSContext();
		
		u.initializeTestUsers();
		
	}
	
	
	protected DirContext getLDAPContext(){
		if(ctx==null){
			ctx = GossLDAPUtils.getLDAPContext();
		}
		return ctx;
	}
	
	
	/**
	 * 
	 * @param userName
	 * @return
	 */
	public List<String> getGroupsForUser(String userName){
		List<String> groups = new ArrayList<String>();
		NamingEnumeration<SearchResult> answer;
		try {
			answer = getLDAPContext().search(GossLDAPUtils.GOSS_LDAP_GROUP_OU, 
				    "(member=uid\\="+userName+")", null);
			while(answer.hasMore()){
				SearchResult r = answer.next();
				String groupName = r.getName();
				groupName = groupName.replace("cn=","");
	        	groups.add(groupName);
	        }
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return groups;
	}
	
	/**
	 * 
	 * @param groupName
	 * @return
	 */
	public List<String> getUsersByGroup(String groupName){
		List<String> users = new ArrayList<String>();
		NamingEnumeration<SearchResult> answer;
		try {
			answer = getLDAPContext().search(GossLDAPUtils.GOSS_LDAP_GROUP_OU, 
				    "(cn="+groupName+")", null);
			while(answer.hasMore()){
				SearchResult r = answer.next();
				NamingEnumeration<? extends Attribute> attrs = r.getAttributes().getAll();
				while(attrs.hasMore()){
					Attribute a = attrs.next();
//					System.out.println("ID "+a.getID());
					if("member".equals(a.getID())){
						for(int i=0;i<a.size();i++){
//							System.out.println("member "+a.get(i));
							String userName = a.get(i).toString();
							userName = userName.replace("uid=", "");
							users.add(userName);
						}
					}
				}
	        }
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return users;
	}
	
	/**
	 * 
	 * @param userName
	 * @param groupName
	 */
	@SuppressWarnings("rawtypes")
	public boolean addUserToGroup(String userName, String groupName){
		//verify valid user and group
		if(!checkExists("uid="+userName+","+GossLDAPUtils.GOSS_LDAP_USER_OU)){
			throw new RuntimeException("Error adding user to group, user "+userName+" doesn't exist");
		}
		if(!checkExists("cn="+groupName+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU)){
			throw new RuntimeException("Error adding user to group, group "+groupName+" doesn't exist");
		}
		
		//TODO get users for group, if the user isn't already in the group
		try {
			Attributes attrs = getLDAPContext().getAttributes("cn="+groupName+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU);
			NamingEnumeration attrEnum = attrs.getAll();
			while(attrEnum.hasMoreElements()){
				Attribute a = (Attribute)attrEnum.nextElement();
				if("member".equals(a.getID())){
					NamingEnumeration memberAttrEnum = a.getAll();
					while(memberAttrEnum.hasMoreElements()){
						String name = memberAttrEnum.nextElement().toString();
						if(name!=null && name.equals("uid="+userName)){
							System.out.println("User "+userName+" already exists in group "+groupName);
							return true;
						}
					}
					
				}
					
			}
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Specify the changes to make
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
		    new BasicAttribute("member", "uid="+userName));

		try {
			getLDAPContext().modifyAttributes("cn="+groupName+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU, mods);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param userName
	 * @param groupName
	 */
	public boolean removeUserFromGroup(String userName, String groupName){
		//TODO verify valid group
		
		
		// Specify the changes to make
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
		    new BasicAttribute("member", "uid="+userName));

		try {
			getLDAPContext().modifyAttributes("cn="+groupName+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU, mods);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param userName
	 */
	public void removeUserFromAllGroups(String userName) {
		List<String> groups = getGroupsForUser(userName);
		for(String group: groups){
			removeUserFromGroup(userName, group);
		}
	}
	
	/**
	 * 
	 * @param groupName
	 */
	public boolean createGroup(String groupName){
		//TODO
		String groupOU = "cn="+groupName+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU;
		try {
			if(!checkExists(groupOU)){
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "uid=goss"));
				
				getLDAPContext().createSubcontext(groupOU, attrs);
				System.out.println("Group "+groupName +" created");
			} else {
				System.out.println("Group "+groupName +" already exists");
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param userName
	 * @param password
	 */
	public boolean createUser(String userName, String password){
		//TODO
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
				System.out.println("User "+userName +" created");
			} else {
				System.out.println("User "+userName +" already exists");
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param groupName
	 */
	public boolean deleteGroup(String groupName){
		try {
			getLDAPContext().destroySubcontext("cn="+groupName+","+GossLDAPUtils.GOSS_LDAP_GROUP_OU);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param userName
	 */
	public boolean deleteUser(String userName){
		removeUserFromAllGroups(userName);
		
		try {
			getLDAPContext().destroySubcontext("uid="+userName+","+GossLDAPUtils.GOSS_LDAP_USER_OU);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public boolean initializeGOSSContext(){
		System.out.println("Initializing GOSS Context");
		
		try{
			//make sure ou=system exists
			String ouName = "ou=system";
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			
			//make sure ou=goss, ou=system exists
			ouName = "ou=goss, ou=system";
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			
			//make sure ou=Destination, ou=goss, ou=system exists
			ouName = GossLDAPUtils.GOSS_LDAP_DESTINATION_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			//make sure ou=Queue, ou=Destination, ou=goss, ou=system exists
			ouName = "ou=Queue, "+GossLDAPUtils.GOSS_LDAP_DESTINATION_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			//make sure cn=Request, ou=Queue, ou=Destination, ou=goss, ou=system exists
			ouName = "cn=Request, ou=Queue, "+GossLDAPUtils.GOSS_LDAP_DESTINATION_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "applicationProcess"));
				//attrs.put(new BasicAttribute("cn", "Request"));?
				getLDAPContext().createSubcontext(ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				//attrs.put(new BasicAttribute("cn", "admin"));?
				getLDAPContext().createSubcontext("cn=admin, "+ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "read"));?
				getLDAPContext().createSubcontext("cn=read, "+ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "write"));?
				getLDAPContext().createSubcontext("cn=write, "+ouName, attrs);
			}
	
			//make sure ou=Topic, ou=Destination, ou=goss, ou=system exists
			ouName = "ou=Topic, "+GossLDAPUtils.GOSS_LDAP_DESTINATION_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			//make sure cn=ActiveMQ.Advisory, ou=Topic, ou=Destination, ou=goss, ou=system exists
			ouName = "cn=ActiveMQ.Advisory, ou=Topic, "+GossLDAPUtils.GOSS_LDAP_DESTINATION_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "applicationProcess"));
				//attrs.put(new BasicAttribute("cn", "ActiveMQ.Advisory"));?
				getLDAPContext().createSubcontext(ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "admin"));?
				getLDAPContext().createSubcontext("cn=admin, "+ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "read"));?
				getLDAPContext().createSubcontext("cn=read, "+ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "write"));?
				getLDAPContext().createSubcontext("cn=write, "+ouName, attrs);
			}
			//make sure cn=ActiveMQ.Temp, ou=Topic, ou=Destination, ou=goss, ou=system exists
			ouName = "cn=ActiveMQ.Temp, ou=Topic, "+GossLDAPUtils.GOSS_LDAP_DESTINATION_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "applicationProcess"));
				//attrs.put(new BasicAttribute("cn", "ActiveMQ.Advisory"));?
				getLDAPContext().createSubcontext(ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "admin"));?
				getLDAPContext().createSubcontext("cn=admin, "+ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "read"));?
				getLDAPContext().createSubcontext("cn=read, "+ouName, attrs);
				
				attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "cn=gossServer"));
				attrs.put(new BasicAttribute("member", "cn=gossUsers"));
				//attrs.put(new BasicAttribute("cn", "write"));?
				getLDAPContext().createSubcontext("cn=write, "+ouName, attrs);
			}
			
			//make sure ou=groups, ou=goss, ou=system exists
			ouName = GossLDAPUtils.GOSS_LDAP_GROUP_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			//make sure cn=gossServer, ou=groups, ou=goss, ou=system exists
			ouName = "cn=gossServer, "+GossLDAPUtils.GOSS_LDAP_GROUP_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "uid=goss"));
				//attrs.put(new BasicAttribute("cn", "gossUsers"));?
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			//make sure cn=gossUsers, ou=groups, ou=goss, ou=system exists
			ouName = "cn=gossUsers, "+GossLDAPUtils.GOSS_LDAP_GROUP_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "groupOfNames"));
				attrs.put(new BasicAttribute("member", "uid=goss"));
				//attrs.put(new BasicAttribute("cn", "gossUsers"));?
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			
//			//make sure ou=roles, ou=goss, ou=system exists
//			ouName = GossLDAPUtils.GOSS_LDAP_ROLE_OU;
//			if(!checkExists(ouName)){
//				System.out.println(ouName+" does not exists, creating.");
//				Attributes attrs = new BasicAttributes();
//				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
//				getLDAPContext().createSubcontext(ouName, attrs);
//			}
			
			//make sure ou=users, ou=goss, ou=system exists
			ouName = GossLDAPUtils.GOSS_LDAP_USER_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "organizationalUnit"));
				getLDAPContext().createSubcontext(ouName, attrs);
			}
			
			String sysUser = GossLDAPUtils.getProperty(GossLDAPUtils.GOSS_PROP_SYSTEM_USER);
			if(sysUser==null){
				sysUser="goss";
				System.out.println("Warning: systemUser property not set, defaulting to 'goss'");
			}
			String sysPW = GossLDAPUtils.getProperty(GossLDAPUtils.GOSS_PROP_SYSTEM_USER);
			if(sysPW==null){
				sysPW="manager";
				System.out.println("Warning: systemPW property not set, defaulting to 'manager'");
			}
			
			//make sure uid=goss, ou=users, ou=goss, ou=system exists
			ouName = "uid="+sysUser+", "+GossLDAPUtils.GOSS_LDAP_USER_OU;
			if(!checkExists(ouName)){
				System.out.println(ouName+" does not exists, creating.");
				Attributes attrs = new BasicAttributes();
				attrs.put(new BasicAttribute("objectClass", "inetOrgPerson"));
				attrs.put(new BasicAttribute("cn", "GOSS Server User"));
				attrs.put(new BasicAttribute("sn", "Server User"));
				attrs.put(new BasicAttribute("userPassword", sysPW));
				getLDAPContext().createSubcontext(ouName, attrs);
				
				addUserToGroup(sysUser, "gossServer");
				addUserToGroup(sysUser, "gossUsers");
			}
			
			
			
		}catch (NamingException e) {
			e.printStackTrace();
			return false;
		}
		
		System.out.println("Initialization complete");
		return true;
	}
	
	
	public void initializeTestUsers(){
		
		String defaultPW = "password";
		
		//Initialize groups
		createGroup("utility_1");
		createGroup("utility_2");
		
		
		//Initialize users
		String userName = "gca_user";
		createUser(userName, defaultPW);
		addUserToGroup(userName, "gossUsers");
		
		userName = "pmu_user"; 
		createUser(userName, defaultPW);
		addUserToGroup(userName, "gossUsers");
		addUserToGroup(userName, "utility_1");
		addUserToGroup(userName, "utility_2");
		
		userName = "pmu_user1";
		createUser(userName, defaultPW);
		addUserToGroup(userName, "gossUsers");
		addUserToGroup(userName, "utility_1");
		
		userName = "pmu_user2"; 
		createUser(userName, defaultPW);
		addUserToGroup(userName, "gossUsers");
		addUserToGroup(userName, "utility_2");
		
		
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
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(ctx!=null){
			ctx.close();
		}
	}
	
	
	
}
