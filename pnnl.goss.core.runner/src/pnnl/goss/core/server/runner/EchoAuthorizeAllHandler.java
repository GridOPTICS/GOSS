package pnnl.goss.core.server.runner;

import java.util.List;
import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.Request;
import  pnnl.goss.core.security.AuthorizationHandler;

@Component
public class EchoAuthorizeAllHandler implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, Set<String> permissions) {
		return true;
	}

}
