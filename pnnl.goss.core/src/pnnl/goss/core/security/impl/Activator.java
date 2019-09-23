package pnnl.goss.core.security.impl;


import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.shiro.mgt.SecurityManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.SecurityConfig;



@Component
public class Activator extends DependencyActivatorBase {

	
  @ServiceDependency
  private DependencyManager manager;
  private static final Logger log = LoggerFactory.getLogger(Activator.class);

  private static final String CONFIG_PID = "pnnl.goss.security";

  
	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		
		manager.add(createComponent()
	        .setInterface(
	        		SecurityConfig.class.getName(), null)
	        .setImplementation(SecurityConfigImpl.class)            
	        .add(createConfigurationDependency().setPid(CONFIG_PID)));
		manager.add(createComponent()
		          .setInterface(
		  				SecurityManager.class.getName(), null)
		          .setImplementation(SecurityManagerImpl.class)            
		          .add(createConfigurationDependency().setPid(CONFIG_PID)));
		
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
		// 
	}
	

	
	
	
}
