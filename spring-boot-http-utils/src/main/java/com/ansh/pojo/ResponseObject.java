package com.ansh.pojo;

/**
 * Object that has status code and response from the request contained
 *
 */
public class ResponseObject {

	private int statusCode;

	private String message;
	
	/**
	 * takes status code and response message as input and return the object
	 * @param statusCode Http Status code
	 * @param message Response message in T format
	 */
	public ResponseObject(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
	
	/**
	 * 
	 * @return status code 
	 */
	public int getStatusCode() {
		return this.statusCode;
	}
	
	/**
	 * 
	 * @return response message
	 */
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return "ResponseObject{" +
				"statusCode=" + statusCode +
				", message='" + message + '\'' +
				'}';
	}
}
