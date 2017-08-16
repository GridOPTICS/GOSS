package gov.pnnl.goss.client;

import java.io.Serializable;
import java.util.UUID;

import gov.pnnl.goss.client.api.Request;
import gov.pnnl.goss.client.api.ResponseFormat;

public class GossRequest implements Request, Serializable {
	
	private static final long serialVersionUID = 7480441703135671635L;
	
	protected String id = UUID.randomUUID().toString();
	
	/**
	 * Allows the request to be specified by a url.
	 */
	protected String url = null;
	
	/**
	 * Default to xml responses 
	 */
	private ResponseFormat reponseFormat = ResponseFormat.XML;
		
	/**
	 * A requested url
	 * @return string url for a resource
	 */
	public String getUrl(){
		return this.url;
	}
	
	/**
	 * Sets a resource url.
	 * @param url
	 */
	public void setUrl(String url){
		this.url = url;
	}

	public ResponseFormat getResponseFormat() {
		return reponseFormat;
	}

	public void setResponseFormat(ResponseFormat responseFormat) {
		this.reponseFormat = responseFormat;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void setId(String id) {
		this.id = id;		
	}
}
