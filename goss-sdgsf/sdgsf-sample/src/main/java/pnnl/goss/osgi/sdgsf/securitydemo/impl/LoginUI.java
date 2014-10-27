package pnnl.goss.osgi.vaadin.securitydemo.impl;

import org.apache.felix.ipojo.annotations.Component;

import pnnl.goss.osgi.vaadin.presenters.GraphPresenter;
import pnnl.goss.osgi.vaadin.presenters.LoginPresenter;
import pnnl.goss.osgi.vaadin.presenters.OptionsPresenter;
import pnnl.goss.osgi.vaadin.service.impl.GraphServiceImpl;
import pnnl.goss.osgi.vaadin.service.impl.LoginServiceImpl;
import pnnl.goss.osgi.vaadin.service.impl.OptionsServiceImpl;
import pnnl.goss.osgi.vaadin.util.DemoConstants;
import pnnl.goss.osgi.vaadin.views.GraphView;
import pnnl.goss.osgi.vaadin.views.LoginView;
import pnnl.goss.osgi.vaadin.views.OptionsView;
import pnnl.goss.osgi.vaadin.views.impl.GraphViewImpl;
import pnnl.goss.osgi.vaadin.views.impl.LoginViewImpl;
import pnnl.goss.osgi.vaadin.views.impl.OptionsViewImpl;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

@Theme("vaadinsample")
@Component(name="LoginUI", immediate=true)
public class LoginUI extends UI {
	
	LoginView loginView;
	
	
	@Override
    protected void init(VaadinRequest request) {
        Navigator navigator = new Navigator(this, this);

        loginView = new LoginViewImpl();
        LoginPresenter loginPresenter = new LoginPresenter(loginView, new LoginServiceImpl());
        loginView.setHandler(loginPresenter);
        loginView.init();
        navigator.addView(DemoConstants.DEFAULT_VIEW, loginView);
        
        OptionsView ov = new OptionsViewImpl();
        //TODO get options service from context
        OptionsPresenter opPresenter = new OptionsPresenter(ov, new OptionsServiceImpl());
        ov.setHandler(opPresenter);
        ov.init();
        navigator.addView(DemoConstants.OPTIONS_VIEW, ov);
        
        GraphView gv = new GraphViewImpl();
        //TODO get options service from context
        GraphPresenter graphPresenter = new GraphPresenter(gv, new GraphServiceImpl());
        gv.setHandler(graphPresenter);
        gv.init();
        navigator.addView(DemoConstants.GRAPH_VIEW, gv);
        

        setNavigator(navigator);
        navigator.navigateTo(DemoConstants.DEFAULT_VIEW);
    }
}
