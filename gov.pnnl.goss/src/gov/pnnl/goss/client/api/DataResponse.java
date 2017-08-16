package gov.pnnl.goss.client.api;

import java.io.Serializable;

import javax.jms.Destination;

public interface DataResponse extends Response {
	boolean wasDataError();
	boolean isError();
	Error toError();
	Serializable getData();
	void setData(Serializable data);
	boolean isResponseComplete();
	void setResponseComplete(boolean responseComplete);
	void setReplyDestination(Destination replyDestination);
	Destination getReplyDestination();
	void setDestination(String destination);
	String getDestination();
	
}
