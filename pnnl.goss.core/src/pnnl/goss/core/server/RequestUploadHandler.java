package pnnl.goss.core.server;

import java.io.Serializable;
import java.util.List;

import pnnl.goss.core.Response;

public interface RequestUploadHandler extends RequestHandlerInterface {

	/**
	 * List all of the datatypes that are handled by the
	 * handler.  Ideally this should be full class names
	 * with perhaps version information, however this is
	 * not a requirement.  In order for GOSS to understand
	 * how to route the request however it does need to be
	 * unique system wide.
	 * 
	 * Example: pnnl.gov.powergrid.Bus.getClass().getName()
	 * 
	 * @return
	 */
	List<String> getHandlerDataTypes();
	
	/**
	 * Handle the upload of data and return a response
	 * 
	 * @param request
	 */
	Response upload(String dataType, Serializable data);	
	
	
}
