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

/**
 * The interface MessageContext abstracts the message context that is processed by a handler in the handle  method.
 * <p>
 * The MessageContext interface provides methods to manage a property set. MessageContext properties enable handlers in a
 * handler chain to share processing related state.
 * @since JAX-WS 2.0
 * @author shaas02
 */
public interface MessageContext extends
			java.util.Map<java.lang.String,java.lang.Object>{
	
	public static enum Scope {
		APPLICATION,
		HANDLER;
	}
	
	/**
	 * Standard property: message direction, true for outbound messages, false for inbound.
	 * <p>
	 * Type: boolean
	 */
	static final java.lang.String MESSAGE_OUTBOUND_PROPERTY = "javax.xml.ws.handler.message.outbound";
	
	/**
	 * Standard property: security configuration.
	 * <p>
	 * Type: javax.xml.rpc.security.SecurityConfiguration
	 */
	static final java.lang.String MESSAGE_SECURITY_CONFIGURATION = "javax.xml.ws.security.configuration";
	
	/**
	 * Standard property: Map of attachments to a message, key is the MIME Content-ID, value is a DataHandler.
	 * <p>
	 * Type:  java.util.Map
	 */
	static final java.lang.String MESSAGE_ATTACHMENTS = "javax.xml.ws.binding.attachments";
	
	/**
	 * Standard property: input source for WSDL document.
	 * <p>
	 * Type: org.xml.sax.InputSource
	 */
	static final java.lang.String WSDL_DESCRIPTION = "javax.xml.ws.wsdl.description";
	
	/**
	 * Standard property: name of WSDL service.
	 * <p>
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_SERVICE = "javax.xml.ws.wsdl.service";
	
	/**
	 * Standard property: name of WSDL port.
	 * <p>
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_PORT = "javax.xml.ws.wsdl.port";
	
	/**
	 * Standard property: name of wsdl interface (2.0) or port type (1.1).
	 * <p>
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_INTERFACE = "javax.xml.ws.wsdl.interface";
	
	/**
	 * Standard property: name of WSDL operation.
	 * <p>
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_OPERATION = "javax.xml.ws.wsdl.operation";
	
	/**
	 * Standard property: HTTP response status code.
	 * <p>
	 * Type: java.lang.Integer
	 */
	static final java.lang.String HTTP_RESPONSE_CODE = "javax.xml.ws.http.response.code";
	
	/**
	 * Standard property: HTTP request headers.
	 * <p>
	 * Type: java.util.Map
	 */
	static final java.lang.String HTTP_REQUEST_HEADERS = "javax.xml.ws.http.request.headers";
	
	/**
	 * Standard property: HTTP response headers.
	 * <p>
	 * Type: java.util.Map
	 */
	static final java.lang.String HTTP_RESPONSE_HEADERS = "javax.xml.ws.http.response.headers";
	
	/**
	 * Standard property: HTTP request method.
	 * <p>
	 * Type: java.lang.String
	 */
	static final java.lang.String HTTP_REQUEST_METHOD = "javax.xml.ws.http.request.method";
	
	/**
	 * Sets the scope of a property.
	 * @param name - Name of the property associated with the MessageContext
	 * @param scope - Desired scope of the property
	 * @throws java.lang.IllegalArgumentException - if an illegal property name is specified
	 */
	void setScope(java.lang.String name,
			 MessageContext.Scope scope) throws java.lang.IllegalArgumentException;
	
	/**
	 * Gets the scope of a property.
	 * @param name - Name of the property
	 * @return Scope of the property
	 * @throws java.lang.IllegalArgumentException - if a non-existant property name is specified
	 */
	MessageContext.Scope getScope(java.lang.String name)throws java.lang.IllegalArgumentException;

}
