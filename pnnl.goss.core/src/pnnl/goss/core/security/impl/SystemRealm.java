package pnnl.goss.core.security.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

public class SystemRealm extends AuthorizingRealm implements Realm {
	
	private final Map<String, SimpleAccount> accntMap = new ConcurrentHashMap<>();
	
	public SystemRealm(String systemUserName, String systemPassword) throws Exception{
		if (systemPassword == null || systemPassword.isEmpty()){
			throw new Exception("Invalid system password");
		}
		if (systemUserName == null || systemUserName.isEmpty()){
			throw new Exception("Invalid system username");
		}
		SimpleAccount accnt = new SimpleAccount(systemUserName, systemPassword, getName());
		accnt.addStringPermission("*");
		accntMap.put("system", accnt);		
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		//get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);
        
        if (accntMap.containsKey(username)){
        	return accntMap.get(username);
        }
        
        return null;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		// we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
        // objects.  See the Realm.supports() method if your application will use a different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		return accntMap.get(upToken.getUsername());
	}
}
