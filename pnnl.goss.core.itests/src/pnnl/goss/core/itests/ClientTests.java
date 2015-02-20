package pnnl.goss.core.itests;

import static org.amdatu.testing.configurator.TestConfigurator.cleanUp;
import static org.amdatu.testing.configurator.TestConfigurator.configuration;
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.amdatu.testing.configurator.TestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.ResponseError;
import pnnl.goss.core.server.ServerControl;
import pnnl.goss.core.server.tester.requests.EchoRequest;

public class ClientTests {
	
	private static final Logger log = LoggerFactory.getLogger(ClientTests.class);
	private TestConfiguration testConfig;
	private volatile ClientFactory clientFactory;
	private volatile ServerControl serverControl;
	
	
	private static final String OPENWIRE_CLIENT_CONNECTION = "tcp://localhost:6000";
	private static final String STOMP_CLIENT_CONNECTION = "tcp://localhost:6000";

	@Before
	public void before() throws InterruptedException{	
		testConfig = configure(this)
						.add(configuration("pnnl.goss.core.server")
								.set("goss.openwire.uri", "tcp://localhost:6000")
								.set("goss.stomp.uri",  "tcp://localhost:6001") //vm:(broker:(tcp://localhost:6001)?persistent=false)?marshal=false")
								.set("goss.start.broker", "true")
								.set("goss.broker.uri", "tcp://localhost:6000"))
						.add(serviceDependency(ServerControl.class))
						.add(configuration(ClientFactory.CONFIG_PID)
								.set("goss.openwire.uri", "tcp://localhost:6000")
								.set("goss.stomp.uri",  "tcp://localhost:6001"))
						.add(serviceDependency(ClientFactory.class));
		testConfig.apply();
		
		// Configuration update is asyncronous, so give a bit of time to catch up
		TimeUnit.MILLISECONDS.sleep(500);
	}
	
	@Test
	public void serverCanStartSuccessfully() {
		assertNotNull(serverControl);
	}
		
	@Test
	public void clientFactoryRegistryOk(){
		
		assertNotNull(clientFactory);	
		Client client = clientFactory.create(PROTOCOL.OPENWIRE);
		assertNotNull(client);
		assertEquals(PROTOCOL.OPENWIRE, client.getProtocol());
		
	}
	
	@Test
	public void clientCanGetEcho(){
		try{
			String message = "hello world!";
			Client client = clientFactory.create(PROTOCOL.OPENWIRE);
			EchoRequest request = new EchoRequest(message);
			Response response = client.getResponse(request);
			
			assertNotNull(response);
			assertTrue(response instanceof DataResponse);
			DataResponse dataResponse = (DataResponse)response;
			assertEquals(message, dataResponse.getData().toString());
		}
		catch (SystemException e){
			e.printStackTrace();
			throw(e);
		}
	}	
	
	@Test
	public void clientReceivesRequestErrorOnNullRequest(){
		
		Client client =  clientFactory.create(PROTOCOL.OPENWIRE);
		Response response = client.getResponse(null);
		assertTrue(response instanceof ResponseError);
		ResponseError err = (ResponseError)response;
		assertTrue(err.getMessage().equals("Cannot route a null request"));
				
	}
	
		
	@After
	public void after(){
		try {
			if (serverControl != null) {serverControl.stop();}
			cleanUp(this);
		}
		catch (Exception e) {
			System.err.println("Ignoring exception!");
		}
		finally {
			if (clientFactory != null){
				clientFactory.destroy();
			}
		}
	}
}
