package gov.pnnl.goss.server.api;

import java.util.Map;

import gov.pnnl.goss.client.api.Request;
import gov.pnnl.goss.client.api.Response;
import gov.pnnl.goss.security.api.AuthorizationHandler;

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
