package pnnl.goss.core;

public class ResponseText extends Response {
	
	private static final long serialVersionUID = 3101381364901500884L;
	
	private String text;
	
	public ResponseText(String text){
		this.text = text;
	}
	
	public String getText(){
		return this.text;
	}

}
