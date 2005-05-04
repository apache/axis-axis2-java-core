package org.apache.axis.wsdl.tojava;

/**
 * @author chathura@opensource.lk
 *
 */
public class CodeGenerationException extends Exception {

	public CodeGenerationException(String message){
		super(message);
	}
	
	public CodeGenerationException(String message, Throwable throwable){
		super(message, throwable);
	}
	
	public CodeGenerationException(Throwable throwable){
		super(throwable);
	}
}
