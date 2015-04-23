package pnnl.goss.core.itests;

import static org.amdatu.testing.configurator.TestConfigurator.cleanUp;
import static org.amdatu.testing.configurator.TestConfigurator.configuration;
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.amdatu.testing.configurator.TestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import pnnl.goss.core.server.DataSourceBuilder;
//import pnnl.goss.core.security.PermissionAdapter;
import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.DataSourceType;
import pnnl.goss.core.testutil.CoreConfigSteps;

public class DataSourceTesting {

	public volatile DataSourceRegistry registry;
	public volatile DataSourceBuilder builder;
	
	private TestConfiguration testConfig;
	
	@Before
	public void before() throws InterruptedException{
		testConfig = configure(this)
				.add(CoreConfigSteps.configureServerAndClientPropertiesConfig())
				
				.add(configuration("pnnl.goss.core.security.propertyfile")
					.set("reader", "reader,queue:*,topic:*,temp-queue:*"))
				//.add(configureServerAndClientPropertiesConfig())
				//.add(serviceDependency(SecurityManager.class))
				//.add(serviceDependency(PermissionAdapter.class))
				//.add(serviceDependency(ServerControl.class))
				//.add(serviceDependency(ClientFactory.class))				
//				.add(TestSteps.configureServerAndClientPropertiesConfig())
				.add(serviceDependency(DataSourceBuilder.class))
				.add(serviceDependency(DataSourceRegistry.class));
				//.add(serviceDependency(SecurityManager.class));
		testConfig.apply();
		
		// Configuration update is asyncronous, so give a bit of time to catch up
		TimeUnit.MILLISECONDS.sleep(500);
	}
	
	@Test
	public void canGetLogDataSource(){
		System.out.println("TEST: canGetLogDataSource");
		assertNotNull(registry);
		Map<String, DataSourceType> available = registry.getAvailable();
		assertNotNull(available);
		assertTrue(available.size() > 0);
		assertNotNull(available.get("pnnl.goss.core.server.runner.datasource.CommandLogDataSource"));
		DataSourceObject obj = registry.get("pnnl.goss.core.server.runner.datasource.CommandLogDataSource");
		assertEquals(DataSourceType.DS_TYPE_OTHER, obj.getDataSourceType());
		System.out.println("TEST_END: canGetLogDataSource");
	}
	
	@Test
	@Ignore
	public void canCreateTableOnBasicDataSourceConnection(){
		System.out.println("TEST: canCreateTableOnBasicDataSourceConnection");
		assertNotNull("Builder was null", builder);
		String dbName = "A Special Database"; // key for looking up the datasourceobject.
		try {
			builder.create(dbName, "jdbc:h2:mem:fusion3", "sa", "sa", "org.h2.Driver");
		} catch (Exception e) {
			e.printStackTrace();
			fail("An exception occurred creating the datasource.");
		}
		
		assertNotNull("Datasource registry null", registry);
		
		DataSourcePooledJdbc obj = (DataSourcePooledJdbc) registry.get(dbName);
		assertNotNull("DataSourcePooledJdbc was null after registry.get", obj);
		
		assertEquals(DataSourceType.DS_TYPE_JDBC, obj.getDataSourceType());
		assertTrue(obj instanceof DataSourcePooledJdbc);
		DataSourcePooledJdbc ds = (DataSourcePooledJdbc)obj;
		try (Connection conn = ds.getConnection()) {
			try (Statement stmt = conn.createStatement()){
				stmt.execute(
					"CREATE TABLE actual_wind_total "
						+ "(TimeStamp datetime NOT NULL, Wind decimal(28,10) DEFAULT NULL, PRIMARY KEY (TimeStamp));");
				stmt.execute("INSERT INTO actual_wind_total VALUES('2009-01-20 05:05:05', 20203.4232);");

			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			fail();
		}
		System.out.println("TEST_END: canCreateTableOnBasicDataSourceConnection");
	}
	
	@Test
	public void canCreateTableOnConnection(){
		System.out.println("TEST: canCreateTableOnConnection");
		DataSourceObject obj = registry.get("pnnl.goss.core.server.runner.datasource.H2TestDataSource");
		assertNotNull(obj);
		assertEquals(DataSourceType.DS_TYPE_JDBC, obj.getDataSourceType());
		assertTrue(obj instanceof DataSourcePooledJdbc);
		DataSourcePooledJdbc ds = (DataSourcePooledJdbc)obj;
		try (Connection conn = ds.getConnection()) {
			try (Statement stmt = conn.createStatement()){
				stmt.execute(
					"CREATE TABLE actual_wind_total "
						+ "(TimeStamp datetime NOT NULL, Wind decimal(28,10) DEFAULT NULL, PRIMARY KEY (TimeStamp));");
				stmt.execute("INSERT INTO actual_wind_total VALUES('2009-01-20 05:05:05', 20203.4232);");

			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			fail();
		}
		System.out.println("TEST_END: canCreateTableOnConnection");
	}
	
	
	@After
	public void after(){
		cleanUp(this);
	}
}
