package pnnl.goss.osgi.vaadin.securitydemo.impl;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Deactivate;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderFinder;

@Component(provide = Servlet.class, properties = { "alias=/login" }, name="LoginUI",servicefactory=true)
@VaadinServletConfiguration(resourceCacheTime=1000,closeIdleSessions=true, ui=LoginUI.class, productionMode=false)
public class LoginServlet extends VaadinServlet {
	private static final long serialVersionUID = 1L;
	private BundleContext context;
	@Activate
	public void activate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
		VaadinConfigurableResourceProviderFinder.add(context, "/themes/vaadinsample");
	}
	
	
	@Deactivate
	public void deactivate(){
//		context.
	}
	
	public BundleContext getBundleContext() {
		return context;
	}

}
