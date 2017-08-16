package com.northconcepts.exception;

public enum ConnectionCode implements ErrorCode {
	SESSION_ERROR(301),
	DESTINATION_ERROR(302),
	CONNECTION_ERROR(303),
	CONSUMER_ERROR(304), 
	BROKER_START_ERROR(305), 
	CLOSING_ERROR(306);
	
	private final int number;

	private ConnectionCode(int number) {
		this.number = number;
	}
	
	@Override
	public int getNumber() {
		return number;
	}
}
