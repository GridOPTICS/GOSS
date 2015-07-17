package pnnl.goss.core.server.web;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;

import pnnl.goss.core.server.TokenIdentifierMap;

@Path("/loginTest")
public class LoginTestService {
	
	
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
	@Produces(MediaType.APPLICATION_JSON)
	public String authenticate(@Context HttpServletRequest request){
		
		return "{\"status\": \"Success\"}";
	}
	

}
