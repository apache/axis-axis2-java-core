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

package javax.xml.ws;

import java.io.Serializable;

/**
 * Class ServiceException
 * The javax.xml.rpc.ServiceException is thrown from the methods in the 
 * javax.xml.rpc.Service interface and ServiceFactory class.
 * 
 * @version 1.0
 * @author sunja07
 */
public class ServiceException extends Exception implements Serializable {
	
	/**
	 * Empty Constructor
	 * Constructs a new exception with null as its detail message.
	 */
	public ServiceException() {
		super();
	}
	
	/**
	 * Constructor
	 * Constructs a new exception with the specified detail message.
	 * @param message - The detail message which is later retrieved using the 
	 * getMessage method
	 */
	public ServiceException(java.lang.String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message - The detail message which is later retrieved using the 
	 * getMessage method
	 * @param cause - The cause which is saved for the later retrieval throw by
	 *  the getCause method
	 */
	public ServiceException(java.lang.String message,
            java.lang.Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * Constructs a new exception with the specified cause and a detail message
	 * of (cause==null ? null : cause.toString()) (which typically contains 
	 * the class and detail message of cause).
	 * @param cause - The cause which is saved for the later retrieval throw by
	 *  the getCause method. (A null value is permitted, and indicates that the
	 *  cause is nonexistent or unknown.)
	 */
	public ServiceException(java.lang.Throwable cause) {
		super(cause==null ? null : cause.toString(), cause);
	}
	
	/**
	 * Method getLinkedCause
	 * Gets the Linked cause
	 * @return The cause of this Exception or null if the cause is noexistent
	 *  or unknown
	 */
	public java.lang.Throwable getLinkedCause() {
		return getCause();
	}
}
