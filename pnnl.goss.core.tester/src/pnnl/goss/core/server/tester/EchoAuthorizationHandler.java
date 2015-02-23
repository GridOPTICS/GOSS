package pnnl.goss.core.server.tester;

import java.util.List;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.Request;
import pnnl.goss.core.security.AuthorizationHandler;

@Component
public class EchoAuthorizationHandler implements AuthorizationHandler {

	@Override
	public boolean isAuthorized(Request request, List<String> userPrincipals) {
		// TODO Auto-generated method stub
		return false;
	}

}
