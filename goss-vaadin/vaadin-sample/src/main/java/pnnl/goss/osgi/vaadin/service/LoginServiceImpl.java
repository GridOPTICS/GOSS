package pnnl.goss.osgi.vaadin.service;

import pnnl.goss.osgi.vaadin.ServiceException;
import pnnl.goss.osgi.vaadin.beans.User;

public class LoginServiceImpl implements LoginService {

	@Override
	public User login(String username, String password) throws ServiceException {
		// TODO Auto-generated method stub
		return new User();
	}

}
