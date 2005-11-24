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
 * Class ProtocolException
 * The javax.xml.ws.ProtocolException interface is a marker base class for 
 * exceptions related to a specific protocol binding. Subclasses are used to 
 * communicate protocol level fault information to clients and may be used on 
 * the server to control the protocol specific fault representation.
 * 
 * @since JAX-WS 2.0
 * @author sunja07
 */
public class ProtocolException extends WebServiceException implements 
Serializable {

	/**
	 * Empty Constructor
	 * Constructs a new protocol exception with null as its detail message.
	 */
	public ProtocolException() {
		super();
	}
	
	/**
	 * Constructor
	 * Constructs a new protocol exception with the specified detail message.
	 * @param message - the detail message. The detail message is saved for 
	 * later retrieval by the Throwable.getMessage() method.
	 */
	public ProtocolException(java.lang.String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * Constructs a new runtime exception with the specified detail message and
	 *  cause.
	 * @param message - the detail message (which is saved for later retrieval 
	 * by the Throwable.getMessage() method).
	 * @param cause - the cause (which is saved for later retrieval by the 
	 * Throwable.getCause() method). (A null value is permitted, and indicates 
	 * that the cause is nonexistent or unknown.)
	 */
	public ProtocolException(java.lang.String message,
            java.lang.Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * Constructs a new runtime exception with the specified cause and a detail
	 *  message of (cause==null ? null : cause.toString()) (which typically
	 *  contains the class and detail message of cause).
	 * @param cause - the cause (which is saved for later retrieval by the 
	 * Throwable.getCause() method). (A null value is permitted, and indicates 
	 * that the cause is nonexistent or unknown.)
	 */
	public ProtocolException(java.lang.Throwable cause) {
		super(cause==null ? null : cause.toString(), cause);
	}
	
}
