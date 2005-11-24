/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.xml.rpc;

/**
 * class JAXRPCException
 * The javax.xml.rpc.JAXRPCException is thrown from the core JAX-RPC APIs to 
 * indicate an exception related to the JAX-RPC runtime mechanisms.
 * 
 * @version 1.1
 * @author sunja07
 */
public class JAXRPCException extends RuntimeException implements 
java.io.Serializable {

	/**
	 * Empty Constructor
	 * Constructs a new exception with null as its detail message.
	 */
	public JAXRPCException() {
		super();
	}
	
	/**
	 * Constructor
	 * Constructs a new exception with the specified detail message.
	 * @param message - The detail message which is later retrieved using the 
	 * getMessage method
	 */
	public JAXRPCException(java.lang.String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message - The detail message which is later retrieved using the 
	 * getMessage method
	 * @param cause - The cause which is saved for the later retrieval throw 
	 * by the getCause method
	 */
	public JAXRPCException(java.lang.String message,
            java.lang.Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * Constructs a new JAXRPCException with the specified cause and a detail 
	 * message of (cause==null ? null : cause.toString()) (which typically 
	 * contains the class and detail message of cause).
	 * @param cause - The cause which is saved for the later retrieval throw 
	 * by the getCause method. (A null value is permitted, and indicates that 
	 * the cause is nonexistent or unknown.)
	 */
	public JAXRPCException(java.lang.Throwable cause){
		super(cause==null ? null : cause.toString(),cause);
	}
	
	/**
	 * Method getLinkedCause
	 * Gets the Linked cause
	 * @deprecated Retained for backwards compatility, new applications should
	 * use the standard cause mechanism.
	 * @return The cause of this Exception or null if the cause is noexistent 
	 * or unknown
	 */
	public java.lang.Throwable getLinkedCause() {
		return getCause();
	}
}
