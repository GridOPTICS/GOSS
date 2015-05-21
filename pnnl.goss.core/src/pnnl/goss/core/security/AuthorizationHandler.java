package pnnl.goss.core.security;

import java.util.Set;

import pnnl.goss.core.Request;
import pnnl.goss.core.server.RequestHandlerInterface;

public interface AuthorizationHandler extends RequestHandlerInterface {

	boolean isAuthorized(Request request, Set<String> permissions);
	
}
