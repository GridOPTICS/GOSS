package pnnl.goss.core.server.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.http.api.ExtHttpService;


public class XDomainFilter implements Filter {
	
	@Override
	public void destroy() {
			
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse)resp;
		HttpServletRequest request = (HttpServletRequest)req;

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", 
					"Origin, X-Requested-With, Content-Type, Accept,AuthToken");			
		response.setHeader("Access-Control-Allow-Methods", 
				"GET,PUT,POST,DELETE,OPTIONS");
			
		// if its an optionss requrest. we allow it to return successful.
		if (request.getMethod().equalsIgnoreCase("options")){
			response.setStatus(200); // ok
			return;
		}
		
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		System.out.println("FILTER CONFIGURED: "+ config.toString());
		
	}

}
