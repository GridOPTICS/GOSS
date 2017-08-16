package gov.pnnl.goss.client;

import java.io.Serializable;

import javax.jms.Destination;

import gov.pnnl.goss.client.api.DataResponse;
import gov.pnnl.goss.client.api.Error;

public class GossDataResponse extends GossResponse implements Serializable, DataResponse {

    private static final long serialVersionUID = 3555288982317165831L;
    Serializable data;

    boolean responseComplete;
    
    String destination;
    
    Destination replyDestination;
    
    public GossDataResponse(){

    }

    public GossDataResponse(Serializable data){
        setData(data);
    }

    public boolean wasDataError(){
        return isError();
    }

    public boolean isError() {
        return data.getClass().equals(GossDataError.class);
    }
    
    public Error toError(){
    	return (Error) data;
    }
    
    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

    /**
     * To check if response is complete in case of request with recurring responses.
     * @return True if this is the last response for the query, false otherwise.
     */
    public boolean isResponseComplete() {
        return responseComplete;
    }

    /**
     * To set if response is complete in case of request with recurring responses.
     * @param responseComplete: True if this is the last response for the query, false otherwise.
     */
    public void setResponseComplete(boolean responseComplete) {
        this.responseComplete = responseComplete;
    }
    
    public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public Destination getReplyDestination() {
		return replyDestination;
	}

	public void setReplyDestination(Destination replyDestination) {
		this.replyDestination = replyDestination;
	}

	@Override
    public String toString() {
        return (this.data != null)? this.data.toString(): super.toString();
    }

}