package pnnl.goss.itest.handlers;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.server.AbstractRequestHandler;
import pnnl.goss.core.server.annotations.RequestHandler;
import pnnl.goss.core.server.annotations.RequestItem;
import pnnl.goss.itest.requests.EchoAuthorizedRequest;
import pnnl.goss.itest.requests.EchoRequest;

@RequestHandler(value={
        @RequestItem(value=EchoRequest.class),
        @RequestItem(value=EchoAuthorizedRequest.class,
                    access=EchoAuthorizationHandler.class)
})
public class EchoRequestHandler extends AbstractRequestHandler {

    @Override
    public Response handle(Request request) throws Exception {

        String message = null;

        if (request instanceof EchoRequest){
            message = ((EchoRequest) request).getMessage();
        }
        else if(request instanceof EchoAuthorizedRequest){
            message = ((EchoAuthorizedRequest) request).getMessage();
        }
        else{
            throw new Exception("Invalid Request Type");
        }


        DataResponse response = new DataResponse();
        response.setData(message);
        response.setResponseComplete(true);
        return response;
    }

}
