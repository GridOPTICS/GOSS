package pnnl.goss.core.security.propertyfile;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.GossRealm;

import com.northconcepts.exception.SystemException;

/**
 * This class handles property based authentication/authorization.  It will only be
 * started as a component if a pnnl.goss.core.security.properties.cfg file exists
 * within the configuration directory.
 * 
 * The format of each property should be username=password,permission1,permission2 ... where
 * permission1 and permission2 are of the format domain:object:action.  There can be multiple
 * levels of domain object and action.  An example permission string format is printers:lp2def:create
 * or topic:request:subscribe.
 * 
 * NOTE: This class assumes uniqueness of username in the properties file.
 * 
 * @author Craig Allwardt
 *
 */
@Component
public class PropertyBasedRealm extends AuthorizingRealm implements GossRealm {
	
	private static final String CONFIG_PID = "pnnl.goss.core.security.propertyfile";
	private static final Logger log = LoggerFactory.getLogger(PropertyBasedRealm.class);
	
	private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		
		//get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);
        return userMap.get(username);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		
		//we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
        //objects.  See the Realm.supports() method if your application will use a different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        return userMap.get(upToken.getUsername());
	}
	
	@ConfigurationDependency(pid=CONFIG_PID)
	public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {

		if (properties != null){
			log.debug("Updating PropertyBasedRealm");
			userMap.clear();
			userPermissions.clear();
			
			Enumeration<String> keys = properties.keys();
			Set<String> perms = new HashSet<>();
			while(keys.hasMoreElements()){
				String k = keys.nextElement();
				String v = (String)properties.get(k);
				String[] credAndPermissions = v.split(",");
				
				SimpleAccount acnt = new SimpleAccount(k, credAndPermissions[0], getName() );
				for(int i =1; i<credAndPermissions.length; i++){
					acnt.addStringPermission(credAndPermissions[i]);
					perms.add(credAndPermissions[i]);
				}
				userMap.put(k, acnt);
				userPermissions.put(k, perms);
				
			}
		}		
	}

	@Override
	public Set<String> getPermissions(String identifier) {
		if (hasIdentifier(identifier)){
			return userPermissions.get(identifier);
		}
		return new HashSet<>();
	}

	@Override
	public boolean hasIdentifier(String identifier) {
		return userMap.containsKey(identifier);
	}
	
	 @Override
	public PermissionResolver getPermissionResolver() {
		return null; // new GossWildcardPermissionResolver();
	}
}
