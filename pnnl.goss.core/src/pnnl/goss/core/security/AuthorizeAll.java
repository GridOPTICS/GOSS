package pnnl.goss.core.security;

import java.util.Set;

import pnnl.goss.core.Request;

public class AuthorizeAll implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, Set<String> permissions) {
		return true;
	}
}
