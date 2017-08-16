package gov.pnnl.goss.client.api;

import java.io.Serializable;

public interface Request extends Serializable{

	String getId();
	void setId(String id);
	
	String getUrl();
	void setUrl(String url);
	
	ResponseFormat getResponseFormat();
	void setResponseFormat(ResponseFormat responseFormat);
}
