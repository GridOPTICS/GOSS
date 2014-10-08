package pnnl.goss.osgi.vaadin.service;

import pnnl.goss.osgi.vaadin.ServiceException;
import pnnl.goss.osgi.vaadin.beans.User;

public interface LoginService {

	 User login(String username, String password) throws ServiceException;
}