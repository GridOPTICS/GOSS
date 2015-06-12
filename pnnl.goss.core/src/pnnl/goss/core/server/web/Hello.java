package pnnl.goss.core.server.web;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.osgi.service.http.HttpService;


@Component(
		provides = {Servlet.class}, 
		properties = {@Property(name="alias", value="/hello")})
public class Hello extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().write("Hello World");
	}
	
	@Start
	public void starting(){
		System.out.println("Starting servlet");
	}
	
	@Stop
	public void stopping(){
		System.out.println("Stopping servilt");
	}
}
