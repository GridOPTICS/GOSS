package pnnl.goss.osgi.vaadin.service;

import java.util.List;

import pnnl.goss.osgi.vaadin.beans.User;
import pnnl.goss.osgi.vaadin.util.ServiceException;

public interface OptionsService {

//	 User login(String username, String password) throws ServiceException;
	List<String> requestPMUList();
}