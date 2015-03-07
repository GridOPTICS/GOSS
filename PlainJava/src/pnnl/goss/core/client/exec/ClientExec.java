package pnnl.goss.core.client.exec;

import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.ResponseError;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.ClientServiceFactory;
import pnnl.goss.core.server.tester.requests.EchoRequest;

public class ClientExec {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ClientServiceFactory factory = new ClientServiceFactory();
		factory.setOpenwireUri("tcp://localhost:61616");
		
		Client client = factory.create(PROTOCOL.OPENWIRE);
		client.setCredentials(new UsernamePasswordCredentials("goss", "goss"));
		Response echoResponse = client.getResponse(new EchoRequest("hello world!"));
		if (echoResponse instanceof ResponseError){
			System.err.println("Error: "+((ResponseError)echoResponse).getMessage());
		}
		else{
			DataResponse dataResp = (DataResponse) echoResponse;
			System.out.println("Woot! "+ dataResp.getData());
		}
		client.close();

	}

}
