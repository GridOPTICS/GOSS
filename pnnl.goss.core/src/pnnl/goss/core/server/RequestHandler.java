package pnnl.goss.core.server;

import java.util.List;

import pnnl.goss.core.Request;
import pnnl.goss.core.Response;

public interface RequestHandler extends RequestHandlerInterface {

	/**
	 * Provides a list of Request classes that the implemented
	 * class will handle.
	 * 
	 * @return 
	 */
	List<Class<? extends Request>> getHandles();
	
	/**
	 * Handle a request of a specific type of service.
	 * 
	 * @param request
	 */
	Response handle(Request request);	
	
}
