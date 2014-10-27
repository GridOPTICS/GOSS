package pnnl.goss.osgi.vaadin.views;

import pnnl.goss.osgi.vaadin.handlers.LoginHandler;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;

public interface LoginView extends View{
	void setHandler(LoginHandler handler);
    void init();

    TextField getTxtUsername();

    PasswordField getTxtPassword();
    Button getBtnLogin();

    void afterSuccessfulLogin();
	void afterFailedLogin();
	void afterServiceException();

}
