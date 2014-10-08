package pnnl.goss.osgi.vaadin.presenters;

import pnnl.goss.osgi.vaadin.ServiceException;
import pnnl.goss.osgi.vaadin.beans.User;
import pnnl.goss.osgi.vaadin.handlers.LoginHandler;
import pnnl.goss.osgi.vaadin.service.LoginService;
import pnnl.goss.osgi.vaadin.views.LoginView;

import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;

public class LoginPresenter implements LoginHandler {
	public static final String DEFAULT_VIEW = "";
	public static final String PROTECTED_VIEW = "protected";
	
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
            service.login(username, password);

            view.afterSuccessfulLogin();
        } catch (ServiceException e) {
            // TODO: log exception
            // TODO: notify view about failure
        }
        
        try {
            User user = service.login(username, password);
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

