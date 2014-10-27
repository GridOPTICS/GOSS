package pnnl.goss.security.util;

import java.io.FileWriter;
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
	protected boolean authenticate(String username, String password) throws LoginException {
		boolean isValid = false;
		try{
			FileWriter logWriter = new FileWriter("GossLDAPLoginModule_authenticate.log",true);
			logWriter.write(System.nanoTime()+";");
			isValid = super.authenticate(username, password);
			logWriter.write(System.nanoTime()+"\n");
			logWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return isValid;
	}
	
	
	@Override
	public boolean login() throws LoginException {
		boolean login = false;
		try{
			FileWriter logWriter = new FileWriter("GossLDAPLoginModule_login.log",true);
			logWriter.write(System.nanoTime()+";");
			login = super.login();
			logWriter.write(System.nanoTime()+"\n");
			logWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return login;
	}

	
@Override
	protected List<String> getRoles(DirContext context, String dn, String username, List<String> currentRoles) throws NamingException {
		List<String> roles = null;
		try{
			FileWriter logWriter = new FileWriter("GossLDAPLoginModule_getRoles.log",true);
			logWriter.write(System.nanoTime()+";");
			roles = super.getRoles(context, dn, username, currentRoles);
			logWriter.write(System.nanoTime()+"\n");
			logWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return roles;
	}

	@Override
	protected boolean bindUser(DirContext context, String dn, String password) throws NamingException {
		boolean bindUser = false;
		try{
			FileWriter logWriter = new FileWriter("GossLDAPLoginModule_bindUser.log",true);
			logWriter.write(System.nanoTime()+";");
			bindUser = super.bindUser(context, dn, password);
			logWriter.write(System.nanoTime()+"\n");
			logWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return bindUser;
	}



}
