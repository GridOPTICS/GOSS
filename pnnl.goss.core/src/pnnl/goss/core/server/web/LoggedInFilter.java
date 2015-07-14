//package pnnl.goss.core.server.web;
//
//import java.io.IOException;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.felix.dm.annotation.api.Component;
//import org.apache.felix.dm.annotation.api.ServiceDependency;
//import org.apache.felix.dm.annotation.api.Start;
//import org.apache.felix.http.api.ExtHttpService;
//
//import pnnl.goss.core.server.TokenIdentifierMap;
//
///**
// * This filter tests that a user has logged in before allowing
// * access to the requested resource.  It does this by using a
// * {@link TokenIdentifierMap} based service that will check the
// * ip address and the pressence of a valid token.
// *
// * If a valid token is present then the request will modified to
// * include an "identifier" parameter that can be used in a web request
// * to authenticate a user's permissions.
// *
// * @author Craig Allwardt
// *
// */
//@Component
//public class LoggedInFilter implements Filter
//{
//	@ServiceDependency
//    private volatile ExtHttpService httpService;
//	
//	@Start
//    public void start() throws ServletException{
//    	System.out.println("Starting "+this.getClass().getName());
//    	try {
//			httpService.registerFilter(this, "/.*",  null,  100,  null);
//		} catch (ServletException e) {
//			e.printStackTrace();
//			throw e;
//		}
//
//    }
//
//	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//		try{
//		HttpServletResponse response = (HttpServletResponse) res;
//		response.setHeader("Access-Control-Allow-Origin", "*");
//		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//		response.setHeader("Access-Control-Max-Age", "3600");
//		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
//		
//		
//		MultiReadHttpServletRequestWrapper wrapper = new MultiReadHttpServletRequestWrapper((HttpServletRequest)req);
//		wrapper.getRequest();
//    	
//		
//		chain.doFilter(wrapper.getRequest(), res);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//
//	public void init(FilterConfig filterConfig) {}
//
//	public void destroy() {}
//}