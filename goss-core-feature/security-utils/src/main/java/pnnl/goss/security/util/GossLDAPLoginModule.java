package pnnl.goss.security.util;

import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.activemq.jaas.LDAPLoginModule;

public class GossLDAPLoginModule extends LDAPLoginModule {

	@Override
	protected boolean authenticate(String username, String password)
			throws LoginException {
		System.out.println("LOGIN MODULE: AUTHENTICATE");
		return super.authenticate(username, password);
	}
	
	
	@Override
	public boolean login() throws LoginException {
		System.out.println("LOGIN MODULE: LOGIN");
		return super.login();
	}

	
@Override
	protected List<String> getRoles(DirContext context, String dn,
			String username, List<String> currentRoles) throws NamingException {
		System.out.println("LOGIN MODULE: GET ROLES");
		return super.getRoles(context, dn, username, currentRoles);
	}

	@Override
	protected boolean bindUser(DirContext context, String dn, String password)
			throws NamingException {
		System.out.println("LOGIN MODULE: BIND USER");
		return super.bindUser(context, dn, password);
	}



}
