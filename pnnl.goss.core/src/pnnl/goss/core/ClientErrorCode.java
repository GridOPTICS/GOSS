package pnnl.goss.core;

import com.northconcepts.exception.ErrorCode;

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
