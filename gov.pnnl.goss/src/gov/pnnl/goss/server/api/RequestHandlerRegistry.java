package gov.pnnl.goss.server.api;

import java.io.Serializable;
import java.util.List;

import com.northconcepts.exception.SystemException;
//import gov.pnnl.goss.core.security.AuthorizationRoleMapper;

import gov.pnnl.goss.client.api.Request;
import gov.pnnl.goss.client.api.RequestAsync;
import gov.pnnl.goss.client.api.Response;

@SuppressWarnings("restriction")
public interface RequestHandlerRegistry {
	
	public RequestHandler getHandler(Class<? extends Request> request) throws HandlerNotFoundException;
	
	public RequestUploadHandler getUploadHandler(String dataType) throws HandlerNotFoundException;
	
	public List<RequestHandlerInterface> list();
	
	public Response handle(Request request) throws HandlerNotFoundException;
	
	public Response handle(String datatype, Serializable data) throws HandlerNotFoundException;
	
	public Response handle(RequestAsync request) throws HandlerNotFoundException;
	
	public boolean checkAccess(Request request, String identifier) throws SystemException;
}

