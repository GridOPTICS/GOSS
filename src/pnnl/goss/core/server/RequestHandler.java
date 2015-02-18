package pnnl.goss.core.server;

import java.util.List;

import pnnl.goss.core.Request;

public interface RequestHandler {

	/**
	 * Provides a list of Request classes that the implemented
	 * class will handle.
	 * 
	 * @return 
	 */
	List<String> getHandles();
	
	/**
	 * Handle a request of a specific type of service.
	 * 
	 * @param request
	 */
	void handle(Request request);	
	
}
