//package pnnl.goss.core.server.web;
//
//import java.util.Dictionary;
//
//import javax.servlet.Servlet;
//import javax.servlet.ServletException;
//
//import org.apache.felix.dm.annotation.api.Component;
//import org.apache.felix.dm.annotation.api.Property;
//import org.osgi.service.http.HttpContext;
//import org.osgi.service.http.HttpService;
//import org.osgi.service.http.NamespaceException;
//
//@Component(
//		provides={Servlet.class},
//		properties={@Property(name="alias", value="/hello2")})
//public class HelloService implements HttpService {
//
//	@Override
//	public void registerServlet(String alias, Servlet servlet,
//			Dictionary initparams, HttpContext context)
//			throws ServletException, NamespaceException {
//		// TODO Auto-generated method stub
//		System.out.println("Registering servlet");
//	}
//
//	@Override
//	public void registerResources(String alias, String name, HttpContext context)
//			throws NamespaceException {
//		// TODO Auto-generated method stub
//		System.out.println("Register Resource");
//		
//	}
//
//	@Override
//	public void unregister(String alias) {
//		// TODO Auto-generated method stub
//		System.out.println("Unregister");
//	}
//
//	@Override
//	public HttpContext createDefaultHttpContext() {
//		// TODO Auto-generated method stub
//		System.out.println("Create Context!");
//		return null;
//	}
//
//}
