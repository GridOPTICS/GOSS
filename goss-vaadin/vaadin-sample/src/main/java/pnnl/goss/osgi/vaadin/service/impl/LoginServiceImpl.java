package pnnl.goss.osgi.vaadin.service.impl;

import pnnl.goss.osgi.vaadin.beans.User;
import pnnl.goss.osgi.vaadin.service.LoginService;
import pnnl.goss.osgi.vaadin.util.ServiceException;

public class LoginServiceImpl implements LoginService {

	@Override
	public User login(String username, String password) throws ServiceException {
		//TODO implement
		if(username.equals("tara"))
			return new User();
		else 
			return null;
	}

}
