package gov.pnnl.goss.server.api;

import gov.pnnl.goss.client.api.Request;

public class HandlerNotFoundException extends Exception {
	
	private static final long serialVersionUID = 5582363974612539305L;
	
	public HandlerNotFoundException(){
		super();
	}
	
	public HandlerNotFoundException(Class<? extends Request> request){
		this(String.format("Handler for %s request was not found!", request.getClass().getName()));
	}
//
//	public HandlerNotFoundException(Class<? extends RequestHandler> request){
//		this(String.format("Handler for %s request was not found!", request.getClass().getName()));
//		
//	}
	
	public HandlerNotFoundException(String message){
		super(message);
	}
}
