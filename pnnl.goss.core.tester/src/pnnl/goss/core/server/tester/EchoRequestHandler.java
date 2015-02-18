package pnnl.goss.core.server.tester;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.tester.requests.EchoAuthorizedRequest;
import pnnl.goss.core.server.tester.requests.EchoRequest;

@Component
public class EchoRequestHandler implements RequestHandler {

	@Override
	public List<String> getHandles() {
		
		List<String> requests = new ArrayList<>();
		requests.add(EchoRequest.class.getName());
		requests.add(EchoAuthorizedRequest.class.getName());
		return requests;
	}

	@Override
	public Response handle(Request request) {
		
		EchoRequest echo = (EchoRequest) request;
		System.out.println("Echoing: "+echo.getMessage());
        DataResponse response = new DataResponse();
        response.setData(echo.getMessage());
        response.setResponseComplete(true);
        return response;

	}

}
