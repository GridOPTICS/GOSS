package pnnl.goss.osgi.vaadin.service.impl;

import java.util.ArrayList;
import java.util.List;

import pnnl.goss.osgi.vaadin.beans.User;
import pnnl.goss.osgi.vaadin.service.LoginService;
import pnnl.goss.osgi.vaadin.service.OptionsService;
import pnnl.goss.osgi.vaadin.util.ServiceException;

public class OptionsServiceImpl implements OptionsService {

	@Override
	public List<String> requestPMUList() {
		// TODO implement using goss client
		List<String> result = new ArrayList<String>();
		result.add("MEAD.phasor0");
		result.add("DCPP.phasor0");
		result.add("ML50.phasor0");
		result.add("TSL5.phasor0");
		result.add("MDW5.phasor0");
		
		return result;
	}

	

}
