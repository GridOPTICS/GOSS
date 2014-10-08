package pnnl.goss.osgi.vaadin.securitydemo.impl;

import org.apache.felix.ipojo.annotations.Component;

import pnnl.goss.osgi.vaadin.presenters.LoginPresenter;
import pnnl.goss.osgi.vaadin.service.LoginServiceImpl;
import pnnl.goss.osgi.vaadin.views.LoginView;
import pnnl.goss.osgi.vaadin.views.LoginViewImpl;
import pnnl.goss.osgi.vaadin.views.ProtectedView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

@Theme("vaadinsample")
@Component(name="TestUI", immediate=true)
public class TestUI extends UI {
	@Override
    protected void init(VaadinRequest request) {
        Navigator navigator = new Navigator(this, this);

        LoginView loginView = new LoginViewImpl();
        LoginPresenter loginPresenter = new LoginPresenter(loginView, new LoginServiceImpl());
        loginView.setHandler(loginPresenter);
        loginView.init();
        navigator.addView(LoginPresenter.DEFAULT_VIEW, loginView);
        
        
        ProtectedView pv = new ProtectedView();
        pv.init();
        navigator.addView(LoginPresenter.PROTECTED_VIEW, pv);

        setNavigator(navigator);
        navigator.navigateTo(loginPresenter.DEFAULT_VIEW);
    }
}
