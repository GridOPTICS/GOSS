package pnnl.goss.core.security;

import java.util.List;

import pnnl.goss.core.Request;
import pnnl.goss.core.server.RequestHandlerInterface;

public interface AuthorizationHandler extends RequestHandlerInterface {

	boolean isAuthorized(Request request, List<String> userRoles);
	
}
