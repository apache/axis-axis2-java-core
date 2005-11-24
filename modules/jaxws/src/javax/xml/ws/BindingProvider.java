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
 * Interface BindingProvider
 * The javax.xml.rpc.BindingProvider interface provides access to the protocol
 * binding and associated JAXRPCContext objects for request and response 
 * message processing.
 * 
 * @since JAX-WS 2.0
 * @author sunja07
 */
public interface BindingProvider {
	
	/**
	 * Standard property: Target service endpoint address. The URI scheme for 
	 * the endpoint address specification must correspond to the 
	 * protocol/transport binding for the binding in use. 
	 */
	static final java.lang.String ENDPOINT_ADDRESS_PROPERTY = 
		"javax.xml.ws.service.endpoint.address";
	
	/**
	 * Standard property: This boolean property is used by a service client to 
	 * indicate whether or not it wants to participate in a session with a 
	 * service endpoint. If this property is set to true, the service client 
	 * indicates that it wants the session to be maintained. If set to false, 
	 * the session is not maintained. The default value for this property is 
	 * false.
	 */
	static final java.lang.String SESSION_MAINTAIN_PROPERTY = 
		"javax.xml.ws.session.maintain";
	
	/**
	 * Standard property for SOAPAction. This boolean property indicates 
	 * whether or not SOAPAction is to be used. The default value of this 
	 * property is false indicating that the SOAPAction is not used.
	 */
	static final java.lang.String SOAPACTION_USE_PROPERTY = 
		"javax.xml.ws.soap.http.soapaction.use";
	
	/**
	 * Standard property for SOAPAction. Indicates the SOAPAction URI if the 
	 * javax.xml.rpc.soap.http.soapaction.use property is set to true.
	 */
	static final java.lang.String SOAPACTION_URI_PROPERTY = 
		"javax.xml.ws.soap.http.soapaction.uri";
	
	/**
	 * Standard property: Password for authentication.
	 */
	static final java.lang.String PASSWORD_PROPERTY = 
		"javax.xml.ws.security.auth.password";
	
	/**
	 * Standard property: User name for authentication.
	 */
	static final java.lang.String USERNAME_PROPERTY = 
		"javax.xml.ws.security.auth.username";
	
	/**
	 * Method getRequestContext
	 * Get the Context that is used to initialize the message context 
	 * for request messages. Modifications to the request context do not 
	 * affect the message context of either synchronous or asynchronous 
	 * operations that have already been started.
	 * @return The context that is used in processing request messages.
	 */
	java.util.Map<java.lang.String,java.lang.Object> getRequestContext();
	
	/**
	 * Method getResponseContext
	 * Get the Context that resulted from processing a response message. 
	 * The returned context is for the most recently completed synchronous 
	 * operation. Subsequent synchronous operation invocations overwrite the 
	 * response context. Asynchronous operations return their response context 
	 * via the Response interface.
	 * @return The context that resulted from processing the latest response messages.
	 */
	java.util.Map<java.lang.String,java.lang.Object> getResponseContext();
	
	/**
	 * Method getBinding
	 * Get the Binding for this binding provider.
	 * @return The Binding for this binding provider.
	 */
	Binding getBinding();
}
