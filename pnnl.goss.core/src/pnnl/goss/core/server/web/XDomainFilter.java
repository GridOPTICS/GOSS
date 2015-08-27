package pnnl.goss.core.server.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.http.api.ExtHttpService;


public class XDomainFilter implements Filter {
	
	// Injected from Activator
    private volatile ExtHttpService httpService;

    public void start() throws ServletException{
    	try{
    		httpService.registerFilter(this, ".*",  null,  100,  null);
		} catch (ServletException e) {
			e.printStackTrace();
			throw e;
		}
    }
    
    public void stop(){
    	httpService.unregisterFilter(this);
    }

	@Override
	public void destroy() {
		
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse)resp;
		response.addHeader("Access-Control-Allow-Origin", "*");
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		System.out.println("FILTER CONFIGURED: "+ config.toString());
		
	}

}
