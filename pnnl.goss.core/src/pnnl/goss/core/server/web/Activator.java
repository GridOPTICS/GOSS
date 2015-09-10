package pnnl.goss.core.server.web;

import java.util.Hashtable;

import javax.servlet.Filter;

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		
		
		Hashtable xDomainProps = new Hashtable();
		xDomainProps.put("pattern", ".*");
		xDomainProps.put("service.ranking", 10);
		
		// Try and keep httpcontext of gosscontext across the board.
		Hashtable loggedInFilterProps = new Hashtable();
		loggedInFilterProps.put("pattern", "/.*");
		loggedInFilterProps.put("contextId", "GossContext");
		
		manager.add(createComponent()
				.setInterface(Filter.class.getName(), xDomainProps)
				.setImplementation(XDomainFilter.class));
		
		manager.add(createComponent()
				.setInterface(Filter.class.getName(),loggedInFilterProps)
				.setImplementation(LoggedInFilter.class)
					.add(createServiceDependency()
						.setService(TokenIdentifierMap.class)));
		
		manager.add(createComponent()
				.setInterface(Object.class.getName(), null)
					.setImplementation(LoginService.class)
				//.setCallbacks("added", "removed", null, null)
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
		// noop
	}
}

