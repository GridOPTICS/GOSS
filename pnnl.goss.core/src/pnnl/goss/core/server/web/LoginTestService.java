package pnnl.goss.core.server.web;

import javax.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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
    public Response runTest(String body) {

        Gson gson = new Gson();
        JsonObject bodyObj = null;
        JsonObject obj = new JsonObject();

        try {
            bodyObj = gson.fromJson(body, JsonObject.class);
            obj.add("data", bodyObj);
        } catch (Exception ex) {
            obj.addProperty("data", "Non JSON :" + body);
        }

        obj.addProperty("Status", "Success");

        return Response.status(Status.OK).entity(obj.toString()).build();
    }

    @POST
    @Path("/loginTest")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_JSON)
    public String authenticate(@Context HttpServletRequest request) {

        return "{\"status\": \"Success\"}";
    }

}
