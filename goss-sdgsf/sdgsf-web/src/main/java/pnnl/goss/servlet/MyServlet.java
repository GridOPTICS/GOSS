package pnnl.goss.servlet;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;


import pnnl.goss.osgi.vaadin.securitydemo.impl.DemoUI;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderFinder;
@WebServlet(value = "/*")
@Component(provide = Servlet.class, properties = { "alias=/securitydemo" }, name="SecurityDemo",servicefactory=true)
@VaadinServletConfiguration(resourceCacheTime=1000,closeIdleSessions=true, ui=DemoUI.class, productionMode=false)
public class MyServlet extends VaadinServlet {
	private static final long serialVersionUID = 1L;
	


}