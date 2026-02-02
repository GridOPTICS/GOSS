package pnnl.goss.core.security.system;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
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

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.security.GossPermissionResolver;
import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.SecurityConfig;


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
public class SystemBasedRealm extends AuthorizingRealm implements GossRealm {
	
	private static final String CONFIG_PID = "pnnl.goss.core.security.systemrealm";
	private static final Logger log = LoggerFactory.getLogger(SystemBasedRealm.class);
	
	private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
	
	@ServiceDependency
	GossPermissionResolver gossPermissionResolver;
	
	@ServiceDependency
    private volatile SecurityConfig securityConfig;
	
	@Override
		protected void onInit() {
			super.onInit();
			Set<String> perms = new HashSet<>();

			SimpleAccount acnt = new SimpleAccount(securityConfig.getManagerUser(), securityConfig.getManagerPassword(), getName() );
			acnt.addStringPermission("queue:*,topic:*,temp-queue:*,fusion:*:read,fusion:*:write");
			perms.add("queue:*,topic:*,temp-queue:*,fusion:*:read,fusion:*:write");
			userMap.put(securityConfig.getManagerUser(), acnt);
			userPermissions.put(securityConfig.getManagerUser(), perms);
		}
	
	
	@Start
	public void start(){
	}
	
	@ConfigurationDependency(pid=CONFIG_PID)
	public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
	}
	
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
        upToken.setRememberMe(true);
        return userMap.get(upToken.getUsername());
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
		 if(gossPermissionResolver!=null)
			 return gossPermissionResolver;
		 else 
			 return super.getPermissionResolver();
	}
}
