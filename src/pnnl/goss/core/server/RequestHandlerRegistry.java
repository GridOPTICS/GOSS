package pnnl.goss.core.server;

import java.util.List;
import java.util.Optional;

import pnnl.goss.core.Request;

public interface RequestHandlerRegistry {
	
	public Optional<RequestHandler> getHandler(Class<? extends Request> request);
	
	public List<RequestHandler> list();
	
	public void handle(Request request);

}
