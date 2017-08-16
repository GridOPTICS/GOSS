package gov.pnnl.goss.client.api;


import gov.pnnl.goss.exception.ErrorCode;

@SuppressWarnings("restriction")
public enum ClientErrorCode implements ErrorCode{
	
	NULL_REQUEST_ERROR(401);
	
	private final int number;

	private ClientErrorCode(int number) {
		this.number = number;
	}
	
	@Override
	public int getNumber() {
		return number;
	}

}
