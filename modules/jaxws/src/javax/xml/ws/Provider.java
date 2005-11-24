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

/**
 * Interface Provider<T>
 * Service endpoints may implement the Provider interface as a dynamic 
 * alternative to an SEI. Implementations are required to support 
 * Provider<Source> and Provider<SOAPMessage>. The ServiceMode annotation can 
 * be used to control whether the Provider instance will receive entire 
 * protocol messages or just message payloads.
 * 
 * @since JAX-WS 2.0
 * @author sunja07
 */
public interface Provider<T> {
	
	/**
	 * Method invoke
	 * Invokes an operation occording to the contents of the request message.
	 * 
	 * @param request - The request message or message payload.
	 * @param context - The context of the request message. This provides 
	 * access to the properties of the underlying handler MessageContext that 
	 * have a scope of APPLICATION. The content of the context may be modified 
	 * and will be reused as the context for any response message.
	 * @return The response message or message payload. May be null if there 
	 * is no response.
	 * @throws java.rmi.RemoteException - if there is an error processing 
	 * request. The cause of the RemoteException may be set to a subclass of 
	 * ProtocolException to control the protocol level representation of the 
	 * exception.
	 */
	T invoke(T request, java.util.Map<java.lang.String,java.lang.Object> context)
	         throws java.rmi.RemoteException;

}
