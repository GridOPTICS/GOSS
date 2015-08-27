package pnnl.goss.core.server.web;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.apache.shiro.mgt.SecurityManager;

import pnnl.goss.core.server.TokenIdentifierMap;

public class Activator extends DependencyActivatorBase {

	private static String WEB_CONFIG_PID = "pnnl.goss.core.server.web";
	
	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		
		manager.add(createComponent()
				.setInterface(Object.class.getName(), null)
					.setImplementation(LoginService.class)
				//.setCallbacks("added", "removed", null, null)
				.add(createServiceDependency()
						.setService(SecurityManager.class))
				.add(createServiceDependency()
						.setService(TokenIdentifierMap.class)));
		
		manager.add(createComponent()
				.setImplementation(XDomainFilter.class)
				.add(createServiceDependency()
						.setService(ExtHttpService.class)));
		
		manager.add(createComponent()
				.setImplementation(LoggedInFilter.class)
				.add(createServiceDependency()
						.setService(ExtHttpService.class))
				.add(createServiceDependency()
						.setService(TokenIdentifierMap.class)));
		
		manager.add(createComponent()
				.setInterface(Object.class.getName(), null).setImplementation(
						LoginTestService.class));
		
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
		// nop
	}
}

