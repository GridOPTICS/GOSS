package pnnl.goss.core.security.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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

@Component
public class GossAuthorizingRealm extends AuthorizingRealm implements Realm  {
	
	// Depend on this so that the security manager service is loaded before
	// this package.
	@ServiceDependency
	private volatile SecurityManager securityManager;
	
	private Collection<String> getPermissionsByRole(String role){
		Set<String> permissions = new HashSet<>();
		
		switch (role) {
		case "users":
			permissions.add("queue:*");
			//permissions.add("queue:request:write");
			//permissions.add("queue:request:create");
			permissions.add("temp-queue:*");
			break;
		
		case "system":
			permissions.add("*");
			break;
		
		case "advisory":
			permissions.add("topic:*"); //ctiveMQ.Advisory.*");
			//permissions.add("topic:ActiveMQ.Advisory.*");
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
    		//account.addRole("darklord");
    		//account.addStringPermissions(getPermissionsByRole("users"));
    		break;   	
    	case "system":
    		account = new SimpleAccount(username, "manager", getName());
    		account.addRole("system");
    		account.addStringPermissions(getPermissionsByRole("system"));
    		break;
    	}
    	
    	if (account == null){
    		System.out.println("Couldn't authenticate on realm: "+ getName() + " for user: "+username);
    		return null;
    	}
    	
    	for(String s: defaultRoles){
    		account.addRole(s);
    		account.addStringPermissions(getPermissionsByRole(s));
    	}
    	
//    	SimpleAccount account = new SimpleAccount(username, "manager", getName());
//        //simulate some roles and permissions:
//        account.addRole("users");
//        account.addRole("admin");
//        //most applications would assign permissions to Roles instead of users directly because this is much more
//        //flexible (it is easier to configure roles and then change role-to-user assignments than it is to maintain
//        // permissions for each user).
//        // But these next lines assign permissions directly to this trivial account object just for simulation's sake:
//        account.addStringPermission("blogEntry:edit"); //this user is allowed to 'edit' _any_ blogEntry
//        //fine-grained instance level permission:
//        account.addStringPermission("printer:print:laserjet2000"); //allowed to 'print' to the 'printer' identified
//        //by the id 'laserjet2000'

    	System.out.println("account: "+ account.getPrincipals().getPrimaryPrincipal()+ " creds: "+account.getCredentials());
        return account;
    }
	

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		
		 //get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);

        //call the underlying EIS for the account data:
        return getAccount(username);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		
		//we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
        //objects.  See the Realm.supports() method if your application will use a different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        return getAccount(upToken.getUsername());
	}
}
