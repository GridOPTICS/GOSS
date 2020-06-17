package pnnl.goss.core.security.impl;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.security.RoleManager;



@Component
public class RoleManagerImpl implements RoleManager {

	private static final Logger log = LoggerFactory.getLogger(RoleManagerImpl.class);
	private static final String CONFIG_PID = "pnnl.goss.core.security.rolefile";
 
//	private HashMap<String, List<String>> roles = new HashMap<String, List<String>>();
	private final Map<String, Set<String>> rolePermissions = new ConcurrentHashMap<>();

	
//	
//	
//	@Override
//	public List<String> getRoles(String userName) throws Exception {
//		if(!roles.containsKey(userName)){
//			throw new Exception("No roles specified for user "+userName);
//		}
//		
//		return roles.get(userName);
//	}
//
//	@Override
//	public boolean hasRole(String userName, String roleName) throws Exception {
//		if(!roles.containsKey(userName)){
//			throw new Exception("No roles specified for user "+userName);
//		}
//		
//		List<String> groups = roles.get(userName);
//		return groups.contains(roleName);
//	}
	
	
	
	@ConfigurationDependency(pid=CONFIG_PID)
	public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
		if (properties != null){
			log.debug("Updating RoleManagerImpl");
			rolePermissions.clear();
			
			Enumeration<String> keys = properties.keys();
			Set<String> perms = new HashSet<>();
			while(keys.hasMoreElements()){
				String k = keys.nextElement();
				String v = (String)properties.get(k);
				String[] credAndPermissions = v.split(",");
				
				for(int i =1; i<credAndPermissions.length; i++){
					perms.add(credAndPermissions[i]);
				}
				rolePermissions.put(k, perms);
				
			}
		}		
		
		
		
//		if (properties != null) {
//			Enumeration<String> keys = properties.keys();
//			
//			while(keys.hasMoreElements()){
//				String user = keys.nextElement();
//				String groups = properties.get(user).toString();
//				System.out.println("Registering user roles: "+user+" --  "+groups);
//				List<String> groupList = new ArrayList<>(Arrays.asList(StringUtils.split(groups, ',')));
//				roles.put(user, groupList);
//			}
//		}
	 }


	@Override
	public Set<String> getRolePermissions(String roleName) throws Exception {
		if(rolePermissions.containsKey(roleName)){
			return rolePermissions.get(roleName);
		} else {
		return null;
		}
	}


	@Override
	public Set<String> getAllRoles() {
		return rolePermissions.keySet();
	}


	@Override
	public Set<String> getRolePermissions(List<String> roleNames) throws Exception {
		Set<String> perms = new HashSet<>();
		for(String role: roleNames){
			Set<String> rolePerms = rolePermissions.get(role);
			for (String p: rolePerms){
				if(!perms.contains(p)){
					perms.add(p);
				}
			}
		}
		
		return perms;
	}

}
