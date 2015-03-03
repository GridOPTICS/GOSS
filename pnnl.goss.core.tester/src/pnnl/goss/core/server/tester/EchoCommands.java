package pnnl.goss.core.server.tester;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.authc.UsernamePasswordToken;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.tester.requests.EchoBlacklistCheckRequest;
import pnnl.goss.core.server.tester.requests.EchoRequest;
import pnnl.goss.core.server.tester.requests.EchoTestData;

@Component(properties={
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gt"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, value={"echo", "echoOpenwire", 
																	"echoBlacklist", "connect", 
																	"doUpload", "help"})
}, provides=Object.class)
public class EchoCommands {

	@ServiceDependency
	private volatile RequestHandlerRegistry registry;
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	private Client client;
	
	public void help(){
		StringBuilder sb = new StringBuilder();
		sb.append("Echo Commands for gt\n");
		sb.append("  echo string - Tests handler registration and handling of echo response\n");
		sb.append("  echoOpenwire string - Test sending of request through queue://request to the server listener\n");
		sb.append("  connect string string - Changes the client credentials.\n");
		sb.append("  echoBlacklist string - echoes words except for the word code unless the user has allword permisison (allword, allword has that permission\n");
		sb.append("  doUpload - tests upload of a EchoTestData object with arbitrary datatype\n");
		
		System.out.println(sb.toString());
		
	}
	
	public void connect(String uname, String pass) {
		if (client != null){
			client.close();
		}
		client = clientFactory.create(PROTOCOL.OPENWIRE);
		client.setCredentials(new UsernamePasswordCredentials(uname, pass));
		System.out.println("Setup to use connection: "+uname+":"+pass);
	}
	
	public void doUpload(){
		getClient();
		EchoTestData data = new EchoTestData()
			.setBoolData(true)
			.setDoubleData(104.345)
			.setIntData(505)
			.setStringData("a cow jumps over the moon.")
			.setFloatData(52.9f)
			.setByteData(hexStringToByteArray("0b234ae51114"));
		System.out.println("Sending different data datatypes across the wire");
		UploadRequest request = new UploadRequest(data, "Test Datatype Upload");
		Response response = client.getResponse(request);
		if (response instanceof UploadResponse){
			UploadResponse ures = (UploadResponse)response;
			if (ures.isSuccess()){
				System.out.println("Successful upload");
			}
			else{
				System.out.println("Un-Successful upload");	
			}
		}
		else{
			System.out.println("Invalid response type found!");
		}
		
	}
	
	public void echo(String message) {
		Response response = null;
		try {
			response = registry.handle(new EchoRequest(message));
		} catch (HandlerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
	}
	
	public void echoBlacklist(String message){
		getClient();
		
		Response response = client.getResponse(new EchoBlacklistCheckRequest(message));
		
		
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
		
	}
	
	public void echoOpenwire(String message){
		
		getClient();
		
		Response response = client.getResponse(new EchoRequest(message));
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
	}

	private void getClient() {
		if (client == null){
			client = clientFactory.create(PROTOCOL.OPENWIRE);
			client.setCredentials(new UsernamePasswordCredentials("darkhelmet", "ludicrousspeed"));
		}
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
	
	@Stop
	public void stop(){
		if (client != null){
			client.close();
		}
	}
}
