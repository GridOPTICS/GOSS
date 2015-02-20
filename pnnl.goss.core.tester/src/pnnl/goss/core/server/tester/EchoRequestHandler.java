package pnnl.goss.core.server.tester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.RequestUploadHandler;
import pnnl.goss.core.server.tester.requests.EchoAuthorizedRequest;
import pnnl.goss.core.server.tester.requests.EchoData;
import pnnl.goss.core.server.tester.requests.EchoDownloadRequest;
import pnnl.goss.core.server.tester.requests.EchoRequest;

@Component(provides={RequestUploadHandler.class, RequestHandler.class})
public class EchoRequestHandler implements RequestHandler, RequestUploadHandler {

	private volatile EchoData receivedData;
	
	@Override
	public List<Class<? extends Request>> getHandles() {
		
		List<Class<? extends Request>> requests = new ArrayList<>();
		requests.add(EchoRequest.class);
		requests.add(EchoAuthorizedRequest.class);
		requests.add(EchoDownloadRequest.class);
		return requests;
	}

	@Override
	public Response handle(Request request) {
		
		DataResponse response = new DataResponse();
		
		if (request instanceof EchoRequest){
			EchoRequest echo = (EchoRequest) request;
			response.setData(echo.getMessage());
		}
		else if(request instanceof EchoDownloadRequest){
			response.setData(receivedData);
		}
		
        response.setResponseComplete(true);
        return response;

	}

	@Override
	public List<String> getHandlerDataTypes() {
		List<String> dataTypes = new ArrayList<String>();
		dataTypes.add(EchoData.class.getName());
		return dataTypes;
	}

	@Override
	public Response upload(String dataType, Serializable data) {
		
		UploadResponse response = null; 
		
		if (dataType.equals(EchoData.class.getName())){
			receivedData = (EchoData)data;
			response = new UploadResponse(true);
		}
		else{
			response = new UploadResponse(false);
			response.setMessage("Unknown datatype arrived!");
		}
		
		return response;
	}

}
