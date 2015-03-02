package pnnl.goss.core.server.tester;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.RequestUploadHandler;
import pnnl.goss.core.server.tester.requests.EchoBlacklistCheckRequest;
import pnnl.goss.core.server.tester.requests.EchoData;
import pnnl.goss.core.server.tester.requests.EchoDownloadRequest;
import pnnl.goss.core.server.tester.requests.EchoRequest;

@Component(provides={RequestUploadHandler.class, RequestHandler.class})
public class EchoRequestHandler implements RequestHandler, RequestUploadHandler {

	private volatile EchoData receivedData;
	
	@Override
	public Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> getHandles() {
		Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> requests = new HashMap<>();
		
		requests.put(EchoRequest.class, EchoAuthorizeAllHandler.class);
		requests.put(EchoBlacklistCheckRequest.class, EchoBlacklistedWordsHandler.class);
		requests.put(EchoDownloadRequest.class, EchoAuthorizeAllHandler.class);
		
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
	public Map<String, Class<? extends AuthorizationHandler>> getHandlerDataTypes() {
		Map<String, Class<? extends AuthorizationHandler>> dataTypes = new HashMap<>();
		dataTypes.put(EchoData.class.getName(), EchoAuthorizeAllHandler.class);
		//dataTypes.put(EchoBlacklistCheckRequest.class.getName(), EchoBlacklistedWordsHandler.class);
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
