package gov.pnnl.goss.security.authall;

import java.util.Map;
import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;

import gov.pnnl.goss.client.api.Request;
import gov.pnnl.goss.client.api.Response;
import gov.pnnl.goss.security.api.AuthorizationHandler;

@Component
public class AuthorizeAll implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, Set<String> permissions) {
		return true;
	}

	@Override
	public Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> getHandles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response handle(Request request) {
		// TODO Auto-generated method stub
		return null;
	}
}
