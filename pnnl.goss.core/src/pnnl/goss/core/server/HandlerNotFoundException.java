package pnnl.goss.core.server;

import org.omg.CORBA.Request;

public class HandlerNotFoundException extends Exception {
	
	private static final long serialVersionUID = 5582363974612539305L;
	
	public HandlerNotFoundException(){
		super();
	}

	public HandlerNotFoundException(Class<? extends Request> request){
		this(String.format("Handler for %s request was not found!", request.getClass().getName()));
		
	}
	
	public HandlerNotFoundException(String message){
		super(message);
	}
}
