package pnnl.goss.core.server.tester;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import pnnl.goss.core.security.GossRealm;

@Component
public class BlacklistRealm extends AuthorizingRealm implements GossRealm  {
	private final Map<String, SimpleAccount> builtAccounts = new ConcurrentHashMap<>();
	
	private Collection<String> getPermissionsByRole(String role){
		Set<String> permissions = new HashSet<>();
		
		switch (role) {
		case "users":
			permissions.add("queue:*"); //request:write");
			//permissions.add("queue:request:create");
			permissions.add("temp-queue:*");
			break;
		
		case "advisory":
			permissions.add("topic:*"); //ctiveMQ.Advisory.*");
			//permissions.add("topic:ActiveMQ.Advisory.*");
			break;
		
		case "allword":
			permissions.add("words:all");
			break;
		}	
		
		return permissions;
	}
	
    protected SimpleAccount getAccount(String username) {
    	
    	SimpleAccount account = null;
    	Set<String> defaultRoles = new HashSet<String>();
    	defaultRoles.add("users");
    	defaultRoles.add("advisory");
    	
        // Populate a dummy instance based upon the username's access privileges.
    	switch(username){
    	case "darkhelmet":
    		account = new SimpleAccount(username, "ludicrousspeed", getName());
    		account.addRole("darklord");
    		account.addStringPermissions(getPermissionsByRole("users"));
    		break;   	
    	case "allword":
    		account = new SimpleAccount(username, "allword", getName());
    		account.addStringPermissions(getPermissionsByRole("allword"));
    		break;   	
    	}
    	
    	if (account == null) {
    		System.err.println("Unknown user: "+username);
    	}
    	else{
	    	for(String s: defaultRoles){
	    		account.addRole(s);
	    		account.addStringPermissions(getPermissionsByRole(s));
	    	}
    	}
    	    	
        return account;
    }
	

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		
		//get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);

        SimpleAccount account = getAccount(username);
        builtAccounts.put(username, account);
        return account;
        //call the underlying EIS for the account data:
        //return getAccount(username);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		
		//we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
        //objects.  See the Realm.supports() method if your application will use a different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        return getAccount(upToken.getUsername());
	}

	@Override
	public Set<String> getPermissions(String identifier) {
		Set<String> hashSet = new HashSet<>();
		
		if (builtAccounts.containsKey(identifier)){
			hashSet.addAll(builtAccounts.get(identifier).getStringPermissions());
		}
		
		return hashSet;
	}

	@Override
	public boolean hasIdentifier(String identifier) {
		return builtAccounts.containsKey(identifier);
	}
}
