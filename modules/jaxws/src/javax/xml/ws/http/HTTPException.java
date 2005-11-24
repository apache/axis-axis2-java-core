package javax.xml.ws.http;

import java.net.ProtocolException;

public class HTTPException extends ProtocolException {

	/**
	 * Constructor for the HTTPException
	 * @param statusCode - int for the HTTP status code
	 */
	public HTTPException(int statusCode){
		this.statusCode = statusCode;
	}
	
	/**
	 * Gets the HTTP status code.
	 * @return HTTP status code
	 */
	public int getStatusCode(){
		return statusCode;
	}
	
	private int statusCode;
}
