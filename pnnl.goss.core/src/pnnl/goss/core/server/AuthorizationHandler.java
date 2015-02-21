package pnnl.goss.core.server;

import pnnl.goss.core.Request;

public interface AuthorizationHandler extends RequestHandlerInterface {

	boolean isAuthorized(Request request, String userRoles);
	
}
