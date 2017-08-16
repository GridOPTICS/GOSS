package gov.pnnl.goss.security.api;

import java.util.Set;

import gov.pnnl.goss.client.api.Request;
import gov.pnnl.goss.server.api.RequestHandler;

public interface AuthorizationHandler extends RequestHandler {

	boolean isAuthorized(Request request, Set<String> permissions);
	
}
