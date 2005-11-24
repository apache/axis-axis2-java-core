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

package javax.xml.ws.handler;

import javax.xml.ws.LogicalMessage;

/**
 * public interface LogicalMessageContext
 * extends <code>MessageContext</code>
 * <p>
 * The LogicalMessageContext interface extends MessageContext to provide access to a the contained message as a
 * protocol neutral LogicalMessage
 * @since JAX-WS 2.0
 * @author shaas02
 *
 */
public interface LogicalMessageContext extends MessageContext {
	
	/**
	 * Gets the message from this message context
	 * @return The contained message; returns null if no message is present in this message context
	 */
	LogicalMessage getMessage();
	
}
