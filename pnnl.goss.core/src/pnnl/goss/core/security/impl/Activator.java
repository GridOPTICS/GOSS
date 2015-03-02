package pnnl.goss.core.security.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.shiro.mgt.DefaultActiveMqSecurityManager;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {

		//Factory<SecurityManager> factory = new DefaultSecurityManager();
		//Secu new IniSecurityManagerFactory(
		//		"conf/shiro.ini");

		Realm defaultRealm = new SystemRealm();
		Set<Realm> realms = new HashSet<>();
		realms.add(defaultRealm);
		DefaultActiveMqSecurityManager securityManager = new DefaultActiveMqSecurityManager();
		
		securityManager.setRealms(realms);
		//CurrentAuthorizedPrincipals principleHandler = new CurrentAuthorizedPrincipals();
		
		
		//gt((AbstractAuthenticator)securityManager.getAuthenticator()).getAuthenticationListeners().add(principleHandler);
		
		SecurityUtils.setSecurityManager(securityManager);
				

		manager.add(createComponent().setInterface(
				SecurityManager.class.getName(), null).setImplementation(
						securityManager));
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
		// 
	}
}
