package pnnl.goss.core.itests;

import static org.amdatu.testing.configurator.TestConfigurator.cleanUp;
import static org.amdatu.testing.configurator.TestConfigurator.configuration;
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.amdatu.testing.configurator.TestConfiguration;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.ResponseError;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.server.ServerControl;
import pnnl.goss.core.server.runner.requests.EchoDownloadRequest;
import pnnl.goss.core.server.runner.requests.EchoRequest;
import pnnl.goss.core.server.runner.requests.EchoTestData;

import com.northconcepts.exception.SystemException;

public class ClientTests {
	
	private static Logger log = LoggerFactory.getLogger(ClientTests.class);
	private TestConfiguration testConfig;
	private volatile ClientFactory clientFactory;
	private volatile ServerControl serverControl;
	
	
	private static final String OPENWIRE_CLIENT_CONNECTION = "tcp://localhost:6000";
	private static final String STOMP_CLIENT_CONNECTION = "tcp://localhost:6000";

	@Before
	public void before() throws InterruptedException{	
		testConfig = configure(this)
						.add(TestSteps.configureServerAndClientPropertiesConfig())
						.add(serviceDependency().setService(Logger.class))
						.add(serviceDependency().setService(SecurityManager.class))
						.add(serviceDependency().setService(ServerControl.class))
						.add(serviceDependency().setService(ClientFactory.class));
		testConfig.apply();
		
		// Configuration update is asyncronous, so give a bit of time to catch up
		TimeUnit.MILLISECONDS.sleep(500);
	}
	
	@Test
	public void serverCanStartSuccessfully() {
		log.debug("TEST: serverCanStartSuccessfully");
		System.out.println("TEST: serverCanStartSuccessfully");
		assertNotNull(serverControl);
	}
		
	@Test
	public void clientFactoryRegistryOk(){
		System.out.println("TEST: clientFactoryRegistryOk");
		assertNotNull(clientFactory);	
		Client client = clientFactory.create(PROTOCOL.OPENWIRE);
		assertNotNull(client);
		assertEquals(PROTOCOL.OPENWIRE, client.getProtocol());
		
	}
	
	@Test
	public void clientCanGetEcho(){
		System.out.println("TEST: clientCanGetEcho");
		try{
			String message = "hello world!";
			Client client = clientFactory.create(PROTOCOL.OPENWIRE);
			client.setCredentials(new UsernamePasswordCredentials("darkhelmet", "ludicrousspeed"));
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
		System.out.println("TEST: clientReceivesRequestErrorOnNullRequest");
		Client client =  clientFactory.create(PROTOCOL.OPENWIRE);
		Response response = client.getResponse(null);
		assertTrue(response instanceof ResponseError);
		ResponseError err = (ResponseError)response;
		assertTrue(err.getMessage().equals("Cannot route a null request"));
				
	}
	
	@Test
	public void clientCanUploadData(){
		System.out.println("TEST: clientCanUploadData");
		Client client = clientFactory.create(PROTOCOL.OPENWIRE);
		// This is in the BlaclistRealm.java in the runner project.
		client.setCredentials(new UsernamePasswordCredentials("darkhelmet", "ludicrousspeed"));
		
		EchoTestData data = new EchoTestData()
			.setBoolData(true)
			.setDoubleData(104.345)
			.setIntData(505)
			.setStringData("a cow jumps over the moon.")
			.setFloatData(52.9f)
			.setByteData(hexStringToByteArray("0b234ae51114"));
		
		UploadRequest request = new UploadRequest(data, "Test Datatype Upload");
		Response response = client.getResponse(request);
		assertTrue("response is a "+response.getClass(), response instanceof UploadResponse);
		UploadResponse uresponse = (UploadResponse)response;
		assertTrue(uresponse.isSuccess());
		response = client.getResponse(new EchoDownloadRequest());
		assertTrue(response instanceof DataResponse);
		DataResponse received = (DataResponse)response;
		assertEquals(data.toString(), received.toString());
		
		
		
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
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
