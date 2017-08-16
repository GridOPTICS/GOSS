//package gov.pnnl.goss.web;
//
//import java.util.Hashtable;
//
//import javax.servlet.Filter;
//
//import org.apache.felix.dm.DependencyActivatorBase;
//import org.apache.felix.dm.DependencyManager;
//import org.apache.shiro.mgt.SecurityManager;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceReference;
//import org.osgi.service.http.HttpContext;
//import org.osgi.service.http.HttpService;
//
//import gov.pnnl.goss.server.api.TokenIdentifierMap;
//
//
//
//public class Activator extends DependencyActivatorBase {
//
//	private static String WEB_CONFIG_PID = "pnnl.goss.core.server.web";
//	
//	@Override
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public void init(BundleContext context, DependencyManager manager)
//			throws Exception {
//		
//		
//		Hashtable xDomainProps = new Hashtable();
//		xDomainProps.put("pattern", ".*");
//		xDomainProps.put("service.ranking", 10);
//		
//		// Try and keep httpcontext of gosscontext across the board.
//		Hashtable loggedInFilterProps = new Hashtable();
//		loggedInFilterProps.put("pattern", ".*\\/api\\/.*");
//		loggedInFilterProps.put("contextId", "GossContext");
//		
//		Hashtable contextWrapperProps = new Hashtable();
//		contextWrapperProps.put("contextId", "GossContext");
//		contextWrapperProps.put("context.shared", true);
//		
//		ServiceReference<HttpService>httpRef = context.getServiceReference(HttpService.class);
//		HttpService httpService = context.getService(httpRef);
//		
//		if(httpService == null){
//			throw new Exception("HttpService not available.");
//		}
//		
//		manager.add(createComponent()
//				.setInterface(HttpContext.class.getName(), contextWrapperProps)
//				.setImplementation(httpService.createDefaultHttpContext()));
//				
//		manager.add(createComponent()
//				.setInterface(Filter.class.getName(), xDomainProps)
//				.setImplementation(XDomainFilter.class));
//		
//		manager.add(createComponent()
//				.setInterface(Filter.class.getName(),loggedInFilterProps)
//				.setImplementation(LoggedInFilter.class)
//					.add(createServiceDependency()
//						.setService(TokenIdentifierMap.class)));
//		
//		manager.add(createComponent()
//				.setInterface(Object.class.getName(), null)
//					.setImplementation(LoginService.class)
//				//.setCallbacks("added", "removed", null, null)
//				.add(createServiceDependency()
//						.setService(SecurityManager.class))
//				.add(createServiceDependency()
//						.setService(TokenIdentifierMap.class)));
//		
//		manager.add(createComponent()
//				.setInterface(Object.class.getName(), null).setImplementation(
//						LoginTestService.class));
//		
//	}
//
//	@Override
//	public void destroy(BundleContext context, DependencyManager manager)
//			throws Exception {
//		// noop
//	}
//}
//
