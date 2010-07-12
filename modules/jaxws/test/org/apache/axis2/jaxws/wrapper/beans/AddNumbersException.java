
package org.apache.axis2.jaxws.wrapper.beans;

import javax.xml.ws.WebFault;

@WebFault(name = "AddNumbersFault", targetNamespace = "http://org/test/addnumbers")
public class AddNumbersException extends Exception
{

	private String message = null;
	public AddNumbersException(){}
	public AddNumbersException(String message){
		this.message = message;
	}
	public String getInfo(){
		return message;
	}
}
