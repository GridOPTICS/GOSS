package pnnl.goss.core.server.web;

import javax.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;

import pnnl.goss.core.server.TokenIdentifierMap;

@Path("/login")
public class LoginService {

	// Injected from Activator
	private volatile SecurityManager securityManager;

	// Injected from Activator.
	private volatile TokenIdentifierMap tokenMap;

	public void start() {
		// System.out.println("I AM STARTING!");
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
	@Produces(MediaType.APPLICATION_JSON)
	public String authenticate(@Context HttpServletRequest request, UsernamePasswordToken params) {
		String sessionToken = null;
		try {
			@SuppressWarnings("unused")
			AuthenticationInfo info = securityManager.authenticate(params);
			sessionToken = tokenMap.registerIdentifier(request.getRemoteAddr(), params.getUsername());

		} catch (AuthenticationException e) {
			return "{\"error\": \"Invalid Login\"}";
		}

		return "{\"token\": \"" + sessionToken + "\"}";
	}

}
