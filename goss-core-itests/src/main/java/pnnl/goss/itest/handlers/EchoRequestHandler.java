package pnnl.goss.itest.handlers;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.server.AbstractRequestHandler;
import pnnl.goss.core.server.annotations.RequestHandler;
import pnnl.goss.core.server.annotations.RequestItem;
import pnnl.goss.itest.requests.EchoRequest;

@RequestHandler(value={
		@RequestItem(value=EchoRequest.class)
})
public class EchoRequestHandler extends AbstractRequestHandler {

	@Override
	public Response handle(Request request) {
		
		EchoRequest echo = (EchoRequest)request;
		
		DataResponse response = new DataResponse();
		response.setData(echo.getMessage());
		response.setResponseComplete(true);
		return response;
	}

}
