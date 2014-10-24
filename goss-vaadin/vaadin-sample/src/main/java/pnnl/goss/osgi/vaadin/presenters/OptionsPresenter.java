package pnnl.goss.osgi.vaadin.presenters;

import java.util.List;

import pnnl.goss.osgi.vaadin.beans.User;
import pnnl.goss.osgi.vaadin.handlers.LoginHandler;
import pnnl.goss.osgi.vaadin.handlers.OptionsHandler;
import pnnl.goss.osgi.vaadin.service.LoginService;
import pnnl.goss.osgi.vaadin.service.OptionsService;
import pnnl.goss.osgi.vaadin.util.ServiceException;
import pnnl.goss.osgi.vaadin.views.LoginView;
import pnnl.goss.osgi.vaadin.views.OptionsView;

import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;

public class OptionsPresenter implements OptionsHandler {

	
	private OptionsView view;
    private OptionsService service;

    public OptionsPresenter(OptionsView view, OptionsService service) {
        this.view = view;
        this.service = service;
    }

	@Override
	public List<String> requestPMUList() {
		return service.requestPMUList();
	}

} 

