package pnnl.goss.core.server;

import java.util.List;
import java.util.Optional;

import pnnl.goss.core.Request;
import pnnl.goss.core.Response;

public interface RequestHandlerRegistry {
	
	public Optional<RequestHandler> getHandler(Class<? extends Request> request);
	
	public List<RequestHandler> list();
	
	public Response handle(Request request);

}
