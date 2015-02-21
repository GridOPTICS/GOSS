package pnnl.goss.core.server.tester;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.Request;
import pnnl.goss.core.server.AuthorizationHandler;

@Component
public class EchoAuthorizationHandler implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, String userPrincipals) {
		// TODO Auto-generated method stub
		return false;
	}

}
