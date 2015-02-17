package pnnl.goss.itest.requests;

import pnnl.goss.core.Request;

public class EchoRequest extends Request {
	
	
	private static final long serialVersionUID = 8676025639438515776L;
	
	String message;
	
	public EchoRequest(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}
}
