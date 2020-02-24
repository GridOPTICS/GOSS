package pnnl.goss.core.security.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

import pnnl.goss.core.security.GossLoginManager;
import pnnl.goss.core.security.GossSecurityManager;
import pnnl.goss.core.security.SecurityConfig;

@Component
public class GossLoginAuthorizingRealm extends AuthorizingRealm implements Realm  {
	
	public static final String DEFAULT_SYSTEM_USER = "system";
	
	// Depend on this so that the security manager service is loaded before
	// this package.
//	@ServiceDependency
//	private volatile SecurityManager securityManager;
//	@ServiceDependency
//	private volatile SecurityConfig securityConfig;
	private HashMap<String, SimpleAccount> accountCache = new HashMap<String, SimpleAccount>();
	@ServiceDependency
	GossLoginManager gossLoginManager;
	
	
    protected SimpleAccount getAccount(String username, String password) {

    	//TODO verify that username/pw are correct using GOSSLoginMnager
    	SimpleAccount account = null;
    	account = new SimpleAccount(username, password, getName());
    	account.addRole("login");
    	
    	List<String> permission = new ArrayList<>();
    	permission.add("queue:"+GossSecurityManager.PROP_GOSS_LOGIN_TOPIC);
    	permission.add("topic:"+GossSecurityManager.PROP_GOSS_LOGIN_TOPIC);
    	account.addStringPermissions(permission);
    	
    	if (account == null){
    		System.out.println("Couldn't authenticate on realm: "+ getName() + " for user: "+username);
    		return null;
    	}
    	
    	accountCache.put(username, account);
    	
        return account;
    }
	

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		
		 //get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);
        //call the underlying EIS for the account data:
//        return getAccount(username);
		return accountCache.get(username);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		
		//we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
        //objects.  See the Realm.supports() method if your application will use a different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        return getAccount(upToken.getUsername(), upToken.getPassword().toString());
	}
}
