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

import javax.xml.ws.ProtocolException;

/**
 * public interface Handler<C extends MessageContext>
 * <p>
 * The Handler interface is the base interface for JAX-WS handlers.
 * @since .0
 * @author shaas02
 *
 */
public interface Handler<C extends MessageContext> {
	

	/**
	 * The handleMessage method is invoked for normal processing of inbound and outbound messages. Refer to the
	 * description of the handler framework in the JAX-WS specification for full details.
	 * <p>
	 * @param context - the message context.
	 * @return An indication of whether handler processing should continue for the current message
	 *  - Return true to continue processing.
	 *  - Return false to block processing.
	 * @throws java.lang.RuntimeException - Causes the JAX-WS runtime to cease handler processing and generate a
	 * fault.
	 * @throws ProtocolException
	 */
	boolean handleMessage(C context) throws java.lang.RuntimeException, ProtocolException;
	
	
	/**
	 * The handleFault method is invoked for fault message processing. Refer to the description of the handler framework in
	 * the JAX-WS specification for full details.
	 * @param context - the message context
	 * @return An indication of whether handler fault processing should continue for the current message
	 *  - Return true to continue processing.
	 *  - Return false to block processing.
	 * @throws java.lang.RuntimeException - Causes the JAX-WS runtime to cease handler fault processing and dispatch
	 * the fault.
	 * @throws ProtocolException - Causes the JAX-WS runtime to cease handler fault processing and dispatch the fault.
	 */
	boolean handleFault(C context) throws java.lang.RuntimeException, ProtocolException;
	
	/**
	 * Called at the conclusion of a message exchange pattern just prior to the JAX-WS runtime disptaching a message, fault or
	 * exception. Refer to the description of the handler framework in the JAX-WS specification for full details.
	 * <p>
	 * @param context  - the message context
	 */
	void close(MessageContext context);
	
	/**
	 * The init method enables the handler instance to initialize itself. The init method passes the handler configuration as a
	 * Map instance. The map is used to configure the handler (for example: setup access to an external resource or service)
	 * during the initialization.
	 * <p>
	 * In the init method, the handler class may get access to any resources (for example; access to a logging service or
	 * database) and maintain these as part of its instance variables. Note that these instance variables must not have any state
	 * specific to the message processing performed in the various handle methods.
	 * <p>
	 * @param config - Configuration for the initialization of this handler
	 * @throws RuntimeException - If initialization of the handler fails
	 */
	void init(java.util.Map<java.lang.String,java.lang.Object> config) throws RuntimeException;
	
	/**
	 * The destroy method indicates the end of lifecycle for a Handler instance. The handler implementation class should
	 * release its resources and perform cleanup in the implementation of the destroy method.
	 * <p>
	 * @throws RuntimeException - If any error during destroy
	 */
	void destroy() throws RuntimeException;

}
