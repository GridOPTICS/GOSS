package pnnl.goss.core.server;

import java.util.List;

import pnnl.goss.core.Request;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadRequest;

public interface RequestHandlerRegistry {
	
	public RequestHandler getHandler(Class<? extends Request> request) throws HandlerNotFoundException;
	
	public List<RequestHandler> list();
	
	public Response handle(Request request) throws HandlerNotFoundException;
	
	public Response handle(UploadRequest request, String datatype) throws HandlerNotFoundException;
	
	public Response handle(RequestAsync request) throws HandlerNotFoundException;

}
