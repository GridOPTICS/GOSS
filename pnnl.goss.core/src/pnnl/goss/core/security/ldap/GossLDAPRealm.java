package pnnl.goss.core.security.ldap;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;

import pnnl.goss.core.security.GossRealm;

@Component
public class GossLDAPRealm extends JndiLdapRealm implements GossRealm{

	public GossLDAPRealm(){
		setUserDnTemplate("uid={0},ou=users,ou=goss,ou=system");
		JndiLdapContextFactory fac = new JndiLdapContextFactory();
		fac.setUrl("ldap://localhost:10389");
//		fac.setSystemUsername("uid=admin,ou=system");
//		fac.setSystemPassword("Ldp4Goss$");
		setContextFactory(fac);
	}
	
	@Override
	public Set<String> getPermissions(String identifier) {
		// TODO Auto-generated method stub
		System.out.println("LDAP GET PERMISSIONS "+identifier);
		//TODO get roles for identifier
		
		//look up permissions based on roles
		
		return new HashSet<String>();
	}

	
	@Override
	public boolean hasIdentifier(String identifier) {
		// TODO Auto-generated method stub
		System.out.println("HAS IDENTIFIER "+identifier);
		return false;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		// TODO Auto-generated method stub
		System.out.println("DO GET AUTH INFO");
		for(Object p: principals.asList()){
			System.out.println("    principal: "+p+"   "+p.getClass());
		}
		AuthorizationInfo info =  super.doGetAuthorizationInfo(principals);
		System.out.println("info "+info);
		
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
		System.out.println("GET AUTH TOKEN "+token); 
		AuthenticationInfo info = super.doGetAuthenticationInfo(token);
		System.out.println("GOT INFO "+info);
		return info;
	}
	
	@Override
	public boolean supports(AuthenticationToken token) {
		System.out.println("SUPPORTS "+token);
		// TODO Auto-generated method stub
		boolean supports = super.supports(token);
		System.out.println("SUPPORTS "+supports);
		return supports;
	}
	
}
