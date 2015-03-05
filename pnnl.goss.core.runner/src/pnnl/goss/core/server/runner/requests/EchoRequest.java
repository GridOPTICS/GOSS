package pnnl.goss.core.server.tester.requests;

import pnnl.goss.core.Request;

public class EchoRequest extends Request {

	private static final long serialVersionUID = 8015050320084135678L;
	
	protected String message;
	
	public EchoRequest(String message){
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
