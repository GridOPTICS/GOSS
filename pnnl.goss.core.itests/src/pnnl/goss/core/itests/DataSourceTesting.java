package pnnl.goss.core.itests;

import static org.amdatu.testing.configurator.TestConfigurator.cleanUp;
import static org.amdatu.testing.configurator.TestConfigurator.configuration;
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;
import static pnnl.goss.core.itests.TestSteps.configureServerAndClientPropertiesConfig;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.amdatu.testing.configurator.TestConfiguration;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.After;
import org.junit.Before;




import org.junit.Test;

import pnnl.goss.core.ClientFactory;
//import pnnl.goss.core.security.PermissionAdapter;
import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.DataSourceType;
import pnnl.goss.core.server.ServerControl;

public class DataSourceTesting {

	public volatile DataSourceRegistry registry;
	private TestConfiguration testConfig;
	
	@Before
	public void before() throws InterruptedException{
		TestConfiguration testConfig = configure(this)
				.add(TestSteps.configureServerAndClientPropertiesConfig())
				
				.add(configuration("pnnl.goss.core.security.propertyfile")
					.set("reader", "reader,queue:*,topic:*,temp-queue:*"))
				//.add(configureServerAndClientPropertiesConfig())
				//.add(serviceDependency(SecurityManager.class))
				//.add(serviceDependency(PermissionAdapter.class))
				//.add(serviceDependency(ServerControl.class))
				//.add(serviceDependency(ClientFactory.class))				
//				.add(TestSteps.configureServerAndClientPropertiesConfig())
				.add(serviceDependency(DataSourceRegistry.class));
				//.add(serviceDependency(SecurityManager.class));
		testConfig.apply();
		
		// Configuration update is asyncronous, so give a bit of time to catch up
		TimeUnit.MILLISECONDS.sleep(500);
	}
	
	@Test
	public void canGetLogDataSource(){
		assertNotNull(registry);
		Map<String, DataSourceType> available = registry.getAvailable();
		assertNotNull(available);
		assertTrue(available.size() == 1);
		assertNotNull(available.get("pnnl.goss.core.server.runner.datasource.CommandLogDataSource"));
		DataSourceObject obj = registry.get("pnnl.goss.core.server.runner.datasource.CommandLogDataSource");
		assertEquals(DataSourceType.DS_TYPE_OTHER, obj.getDataSourceType());
		//CommandLogDataSource ds = available.get("pnnl.goss.core.server.runner.datasource.CommandLogDataSource");
		
	}
	
	
	@After
	public void after(){
		cleanUp(this);
	}
}
