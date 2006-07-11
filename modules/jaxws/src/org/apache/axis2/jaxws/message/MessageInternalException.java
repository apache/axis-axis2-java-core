/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message;

/**
 * MessageInternalException
 * The apis defined in the JAX-WS Message sub-component may 
 * throw this unchecked Exception.  This exception is only thrown
 * in situations that should never occur. 
 * Consumers of the Message sub-component should
 * provide sufficient try/catch logic to handle these situtations
 * as they would other unchecked exceptions.
 * (@see org.apache.axis2.jaxws.message.MessageException)
 */
public class MessageInternalException extends RuntimeException {

	/**
	 * @param message
	 */
	public MessageInternalException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MessageInternalException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public MessageInternalException(Throwable cause) {
		super(cause);
	}

}
