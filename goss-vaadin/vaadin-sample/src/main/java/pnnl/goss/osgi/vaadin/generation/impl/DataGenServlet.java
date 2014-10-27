package pnnl.goss.osgi.vaadin.generation.impl;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderFinder;

@Component(provide = Servlet.class, properties = { "alias=/datagenStart" }, name="DatagenStart",servicefactory=true)
@VaadinServletConfiguration(ui=DataGenStartUI.class, productionMode=false)
public class DataGenServlet extends VaadinServlet {
	private static final long serialVersionUID = 1L;
	private BundleContext context;
	
	@Activate
	public void activate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
		VaadinConfigurableResourceProviderFinder.add(context, "/themes/vaadinsample");
	}
	
	public BundleContext getBundleContext() {
		return context;
	}


}
