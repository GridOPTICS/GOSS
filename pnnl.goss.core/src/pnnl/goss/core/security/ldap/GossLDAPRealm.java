package pnnl.goss.core.security.ldap;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.subject.PrincipalCollection;

import pnnl.goss.core.security.GossPermissionResolver;
import pnnl.goss.core.security.GossRealm;

import com.northconcepts.exception.SystemException;

@Component
public class GossLDAPRealm extends JndiLdapRealm implements GossRealm{
	 private static final String CONFIG_PID = "pnnl.goss.core.security.ldap";
	
	 @ServiceDependency
	 GossPermissionResolver gossPermissionResolver;
	 
	public GossLDAPRealm(){
		//TODO move these to config
		setUserDnTemplate("uid={0},ou=users,ou=goss,ou=system");
		JndiLdapContextFactory fac = new JndiLdapContextFactory();
		fac.setUrl("ldap://localhost:10389");
//		fac.setSystemUsername("uid=admin,ou=system");
//		fac.setSystemPassword("SYSTEMPW");
		setContextFactory(fac);
	}
	
	@Override
	public Set<String> getPermissions(String identifier) {
		
		//look up permissions based on roles
		
		return new HashSet<String>();
	}

	
	@Override
	public boolean hasIdentifier(String identifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		// TODO Auto-generated method stub
		AuthorizationInfo info =  super.doGetAuthorizationInfo(principals);
		
		if(info==null){
//			try {
				info = new SimpleAuthorizationInfo();
				//at the very least make sure they have the user role and can use the request and advisory topic
				((SimpleAuthorizationInfo)info).addRole("user");
				
				((SimpleAuthorizationInfo)info).addStringPermission("queue:*");
				((SimpleAuthorizationInfo)info).addStringPermission("temp-queue:*");
				((SimpleAuthorizationInfo)info).addStringPermission("topic:*"); //
				
				//LdapContext ctx = getContextFactory().getSystemLdapContext();
				//TODO lookup roles for user
				
//			} catch (NamingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
		}
		
		return info;
	}
	
	@Override
	public void setUserDnTemplate(String arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		super.setUserDnTemplate(arg0);
	}
	

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		
		// TODO Auto-generated method stub
		AuthenticationInfo info = super.doGetAuthenticationInfo(token);
		return info;
	}
	
	@Override
	public boolean supports(AuthenticationToken token) {
		// TODO Auto-generated method stub
		boolean supports = super.supports(token);
		return supports;
	}
	
	 @ConfigurationDependency(pid=CONFIG_PID)
	 public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
	    	
	    	if (properties != null) {
	    		   //TODO 	
//	    		shouldStartBroker = Boolean.parseBoolean(Optional
//	    				.ofNullable((String) properties.get(PROP_START_BROKER))
//	    				.orElse("true"));
	    		
	    	}
	 }
	 
	 @Override
	 public PermissionResolver getPermissionResolver() {
		 if(gossPermissionResolver!=null)
			 return gossPermissionResolver;
		 else 
			 return super.getPermissionResolver();
	 }
	
}
