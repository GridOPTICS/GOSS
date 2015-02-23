package pnnl.goss.core.server;

import java.util.List;
import java.util.Map;

import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.security.AuthorizationHandler;

public interface RequestHandler extends RequestHandlerInterface {

	/**
	 * Explicitly provide a map from request to authorization handler.
	 * 
	 * @return
	 */
	Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> getHandles();
	
	/**
	 * Handle a request of a specific type of service.
	 * 
	 * @param request
	 */
	Response handle(Request request);	
	
}
