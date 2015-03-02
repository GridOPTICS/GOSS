package pnnl.goss.core.server.tester.requests;

import pnnl.goss.core.Request;

public class EchoAuthorizedRequest extends Request {
    private static final long serialVersionUID = 8676025639438515773L;

    String message;

    public EchoAuthorizedRequest(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
}
