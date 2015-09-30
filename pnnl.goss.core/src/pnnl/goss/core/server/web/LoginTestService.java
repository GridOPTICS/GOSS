package pnnl.goss.core.server.web;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Path("/api")
public class LoginTestService {
	
	@Context 
	private HttpServletRequest request;
		
	@POST
	@Path("/echo")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
	@Produces(MediaType.APPLICATION_JSON)
	public Response runTest(String body){
		
		Gson gson = new Gson();
		JsonObject bodyObj = null;
		JsonObject obj = new JsonObject();
		
		try{
			bodyObj = gson.fromJson(body, JsonObject.class);
			obj.add("data", bodyObj);
		}
		catch(Exception ex){
			obj.addProperty("data", "Non JSON :"+body);
		}
		
		obj.addProperty("Status", "Success");
		
		
		return Response.status(Status.OK).entity(obj.toString()).build();
	}
	

}
