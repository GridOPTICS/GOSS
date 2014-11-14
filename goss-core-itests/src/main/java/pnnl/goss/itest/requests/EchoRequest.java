package pnnl.goss.itest.requests;

import pnnl.goss.core.Request;

public class EchoRequest extends Request {
	
	String message;
	
	public EchoRequest(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}
}
