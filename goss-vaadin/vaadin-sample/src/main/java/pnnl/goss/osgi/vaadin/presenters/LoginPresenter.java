package pnnl.goss.osgi.vaadin.presenters;

import pnnl.goss.osgi.vaadin.beans.User;
import pnnl.goss.osgi.vaadin.handlers.LoginHandler;
import pnnl.goss.osgi.vaadin.service.LoginService;
import pnnl.goss.osgi.vaadin.util.ServiceException;
import pnnl.goss.osgi.vaadin.views.LoginView;

import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;

public class LoginPresenter implements LoginHandler {

	
	private LoginView view;
    private LoginService service;

    public LoginPresenter(LoginView view, LoginService service) {
        this.view = view;
        this.service = service;
    }

    @Override
    public void login() {
        TextField txtUsername = view.getTxtUsername();
        PasswordField txtPassword = view.getTxtPassword();

        String username = txtUsername.getValue();
        String password = txtPassword.getValue();
        
        try {
            User user = service.login(username, password);
            System.out.println("USER "+user);
            if (user != null) {
                view.afterSuccessfulLogin(); 
            } else {
                view.afterFailedLogin();
            }
        } catch (ServiceException e) {
//            log.error(e);
            view.afterServiceException();
        }
    }
} 

