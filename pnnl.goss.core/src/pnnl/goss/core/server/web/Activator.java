package pnnl.goss.core.server.web;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.apache.shiro.mgt.SecurityManager;
import pnnl.goss.core.server.TokenIdentifierMap;

public class Activator extends DependencyActivatorBase {

	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		manager.add(createComponent()
				.setInterface(Object.class.getName(), null).setImplementation(
						LoginService.class)
				.add(createServiceDependency()
						.setService(SecurityManager.class))
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

