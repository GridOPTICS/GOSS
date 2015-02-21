package pnnl.goss.core.server;

import pnnl.goss.core.Request;

public abstract class AbstractAuthorizeAll implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, String userRoles) {
		return true;
	}

}
