package pnnl.goss.core.security.impl;

import java.util.List;
import java.util.Set;

import pnnl.goss.core.Request;
import pnnl.goss.core.security.AuthorizationHandler;

public abstract class AbstractAuthorizeAll implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, Set<String> permissions) {
		return true;
	}

}
