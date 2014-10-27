package pnnl.goss.osgi.vaadin.views.impl;

import pnnl.goss.osgi.vaadin.handlers.LoginHandler;
import pnnl.goss.osgi.vaadin.util.DemoConstants;
import pnnl.goss.osgi.vaadin.views.LoginView;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class LoginViewImpl extends VerticalLayout implements LoginView {
	private static final long serialVersionUID = 1L;

	private LoginHandler handler;

    private TextField txtUsername;
    private PasswordField txtPassword;
    
    private Label statusLabel;

    private Button btnLogin;

    @Override
    public void setHandler(LoginHandler handler) {
        this.handler = handler;
    }

    @Override
    public void init() {
        txtUsername = new TextField("Username:");
        addComponent(txtUsername);
        txtPassword = new PasswordField("Password:");
        addComponent(txtPassword);

        btnLogin = new Button("Login");
        addComponent(btnLogin);
        btnLogin.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handler.login();
            }
        });
        
        statusLabel = new Label();
        addComponent(statusLabel);
    }

    @Override
    public TextField getTxtUsername() {
        return txtUsername;
    }

    @Override
    public PasswordField getTxtPassword() {
        return txtPassword;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

    @Override
    public Button getBtnLogin() {
        return btnLogin;
    }

    @Override
    public void afterSuccessfulLogin() {
    	Notification.show("Wunderbar");
        UI.getCurrent().getNavigator().navigateTo(DemoConstants.OPTIONS_VIEW);
    }

	@Override
	public void afterFailedLogin() {
		statusLabel.setCaption("Login Failed, please try again");
	}

	@Override
	public void afterServiceException() {
		statusLabel.setCaption("Login Failed due to exception");
		
	}
}
