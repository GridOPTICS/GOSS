package pnnl.goss.security.core;

import pnnl.goss.core.Request;

public interface GossSecurityHandler {

	public abstract boolean checkAccess(Request request, String userPrincipals,
			String tempDestination);

	public abstract void addHandlerMapping(String requestClass,
			String handlerClass);

	public abstract void removeHandlerMapping(Class request);

	public abstract void addHandlerMapping(Class request, Class handler);

}