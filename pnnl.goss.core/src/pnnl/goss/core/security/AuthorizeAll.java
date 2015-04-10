package pnnl.goss.core.security;

import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.Request;

@Component
public class AuthorizeAll implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, Set<String> permissions) {
		return true;
	}
}
