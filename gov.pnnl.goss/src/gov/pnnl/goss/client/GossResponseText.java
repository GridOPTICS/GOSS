package gov.pnnl.goss.client;

public class GossResponseText extends GossResponse {
	
	private static final long serialVersionUID = 3101381364901500884L;
	
	private String text;
	
	public GossResponseText(String text){
		this.text = text;
	}
	
	public String getText(){
		return this.text;
	}

}
