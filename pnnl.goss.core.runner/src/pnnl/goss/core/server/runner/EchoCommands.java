package pnnl.goss.core.server.runner;

import javax.jms.JMSException;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.runner.datasource.CommandLogDataSource;
import pnnl.goss.core.server.runner.requests.EchoBlacklistCheckRequest;
import pnnl.goss.core.server.runner.requests.EchoRequest;
import pnnl.goss.core.server.runner.requests.EchoTestData;

@Component(properties={
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="gt"),
		@Property(name=CommandProcessor.COMMAND_FUNCTION, value={"echo", "echoOpenwire", 
																	"echoBlacklist", "connect", 
																	"doUpload", "help",
																	"listCommands", "clearCommands"})
}, provides=Object.class)
public class EchoCommands {

	@ServiceDependency
	private volatile RequestHandlerRegistry registry;
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	@ServiceDependency
	private volatile DataSourceRegistry dsRegistry;
	
	private Client client;

	private CommandLogDataSource getCommandStore(){
		String key = CommandLogDataSource.class.getName();
		return (CommandLogDataSource) dsRegistry.get(key);
	}
	private void addCommand(String commandText){
		CommandLogDataSource ds = getCommandStore();
		if (ds != null){
			ds.log(commandText);
		}
	}
	
	public void clearCommands(){
		CommandLogDataSource ds = getCommandStore();
		if (ds != null){
			ds.clear();
		}
	}
	
	public void listCommands(){
		CommandLogDataSource ds = getCommandStore();
		if (ds != null){
			int i=0;
			for (String d: ds.getList()){
				System.out.println((i+1)+") " + d);
				i++;
			}
		}
		else{
			System.out.println("Datasource log not found.");
		}
	}
	
	public void help(){
		StringBuilder sb = new StringBuilder();
		sb.append("Echo Commands for gt\n");
		sb.append("  echo string - Tests handler registration and handling of echo response\n");
		sb.append("  echoOpenwire string - Test sending of request through queue://request to the server listener\n");
		sb.append("  connect string string - Changes the client credentials.\n");
		sb.append("  echoBlacklist string - echoes words except for the words(this, that or code) unless the user has allword permisison (allword, allword has that permission\n");
		sb.append("  doUpload - tests upload of a EchoTestData object with arbitrary datatype\n");
		sb.append("  listCommands - Lists all of the commands that have been run in the session\n");
		sb.append("  clearCommands - Clear the commands from the session\n");
		
		System.out.println(sb.toString());
		
		addCommand("help");
	}
	
	
	public void connect(String uname, String pass) {
		connect(uname, pass, false);
	}
	public void connect(String uname, String pass, boolean useToken) {
		try{
			if (client != null){
				client.close();
			}
			Credentials credentials = new UsernamePasswordCredentials(uname, pass);
			client = clientFactory.create(PROTOCOL.OPENWIRE, credentials, useToken);
			System.out.println("Setup to use connection: "+uname);
			
			addCommand("connect "+ uname);
		}catch(Exception e){
			e.printStackTrace();
		}
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
		Response response;
		try {
			response = (Response)client.getResponse(request,"Request", null);
		
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
		addCommand("doUpload");
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		addCommand("echo "+message);
	}
	
	public void echoBlacklist(String message){
		getClient();
		
		Response response;
		try {
			response = (Response)client.getResponse(new EchoBlacklistCheckRequest(message),"Request",null);
		
		
		
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
		addCommand("echoBlacklist "+ message);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void echoOpenwire(String message){
		
		getClient();
		
		Response response;
		try {
			response = (Response)client.getResponse(new EchoRequest(message),"Request",null);
		
		if (response instanceof DataResponse){
			System.out.println("Response was: " + ((DataResponse)response).getData());
		}
		else{
			System.out.println("Response wasn't DataResponse it was: "+response.getClass().getName());
		}
		
		addCommand("echoOpenwire "+ message);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getClient() {
		try{
			if (client == null){
				Credentials credentials = new UsernamePasswordCredentials("darkhelmet", "ludicrousspeed");
				client = clientFactory.create(PROTOCOL.OPENWIRE, credentials, false);
			}
		}catch(Exception e){
			e.printStackTrace();
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
