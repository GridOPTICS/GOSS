package gov.pnnl.goss.client;

import gov.pnnl.goss.client.api.Error;

public class GossDataError implements Error {

    /**
     * Serialized object data
     */
    private static final long serialVersionUID = 8779199763024982724L;


    private String message;

    public GossDataError(String message){
        this.setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return (message != null)? message: super.toString();
    }

}
