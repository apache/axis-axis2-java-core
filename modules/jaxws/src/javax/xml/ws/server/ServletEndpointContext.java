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

package javax.xml.rpc.server;

import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.JAXRPCException;

/**
 * public interface ServletEndpointContext
 * 
 * The ServletEndpointContext provides an endpoint context maintained by the underlying servlet container based JAX-RPC
 * runtime system. For service endpoints deployed on a servlet container based JAX-RPC runtime system, the context parameter in
 * the ServiceLifecycle.init method is required to be of the Java type javax.xml.rpc.server.ServletEndpointContext.
 * 
 * A servlet container based JAX-RPC runtime system implements the ServletEndpointContext interface. The JAX-RPC
 * runtime system is required to provide appropriate session, message context, servlet context and user principal information per
 * method invocation on the endpoint class.
 * 
 * @version 1.0
 * 
 * @author shaas02
 *
 */
public interface ServletEndpointContext {
	
	/**
	 * The method getMessageContext returns the MessageContext targeted for this endpoint instance. This enables the
	 * service endpoint instance to acccess the MessageContext propagated by request HandlerChain (and its contained
	 * Handler instances) to the target endpoint instance and to share any SOAP message processing related context. The
	 * endpoint instance can access and manipulate the MessageContext and share the SOAP message processing related
	 * context with the response HandlerChain.
	 * 
	 * @return MessageContext; If there is no associated MessageContext, this method returns null.
	 * @throws java.lang.IllegalStateException - if this method is invoked outside a remote method implementation by a service endpoint instance.
	 */
	MessageContext  getMessageContext() throws java.lang.IllegalStateException;

	/**
	 * Returns a java.security.Principal instance that contains the name of the authenticated user for the current method
	 * invocation on the endpoint instance. This method returns null if there is no associated principal yet. The underlying
	 * JAX-RPC runtime system takes the responsibility of providing the appropriate authenticated principal for a remote method
	 * nvocation on the service endpoint instance.
	 * 
	 * @return A java.security.Principal for the authenticated principal associated with the current invocation on the servlet  endpoint instance; Returns null if there no authenticated user associated with a method invocation.
	 */
	java.security.Principal getUserPrincipal();
	
	/**
	 * The getHttpSession method returns the current HTTP session (as a javax.servlet.http.HTTPSession). When
	 * invoked by the service endpoint within a remote method implementation, the getHttpSession returns the HTTP session
	 * associated currently with this method invocation. This method returns null if there is no HTTP session currently active
	 * and associated with this service endpoint. An endpoint class should not rely on an active HTTP session being always there;
	 * the underlying JAX-RPC runtime system is responsible for managing whether or not there is an active HTTP session.
	 * 
	 * @return The HTTP session associated with the current invocation or null if there is no active session.
	 * @throws JAXRPCException - If this method invoked by any non-HTTP bound endpoint
	 */
	javax.servlet.http.HttpSession getHttpSession() throws JAXRPCException;
	
	/**
	 * The method getServletContext returns the ServletContext associated with the web application that contain this
	 * endpoint. According to the Servlet specification, There is one context per web application (installed as a WAR) per JVM .
	 * A servlet based service endpoint is deployed as part of a web application.
	 * 
	 * @return ServletContext
	 */
	javax.servlet.ServletContext getServletContext();
	
	/**
	 * @param role - a String specifying the name of the role
	 * @return a boolean indicating whether the authenticated user associated with the current method invocation belongs to a
	 * given role; false if the user has not been authenticated
	 */
	boolean isUserInRole(java.lang.String role);
}
