package pnnl.goss.core.security.impl;

import java.util.List;

import pnnl.goss.core.Request;
import pnnl.goss.core.security.AuthorizationHandler;

public abstract class AbstractAuthorizeAll implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, List<String> userRoles) {
		return true;
	}

}
