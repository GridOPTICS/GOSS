package pnnl.goss.core.security.ldap;
import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import pnnl.goss.core.security.GossRealm;

@Component
public class GossLDAPRealm extends AuthorizingRealm implements GossRealm{

	@Override
	public Set<String> getPermissions(String identifier) {
		// TODO Auto-generated method stub
		return null;
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
		return null;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}	
}
