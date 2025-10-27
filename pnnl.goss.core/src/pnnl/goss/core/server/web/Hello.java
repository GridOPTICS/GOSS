package pnnl.goss.core.server.web;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

@Component(service = Servlet.class, property = {"osgi.http.whiteboard.servlet.pattern=/hello"})
public class Hello extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().write("Hello World");
	}
	
	@Activate
	public void starting(){
		System.out.println("Starting servlet");
	}
	
	@Deactivate
	public void stopping(){
		System.out.println("Stopping servlet");
	}
}
