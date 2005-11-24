package org.apache.axis2.jaxws.server;

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;


/**
 * Class ServletEndpointContextImpl
 * 
 * Endpoint service implementation classes optionally implement ServiceLifecycle
 * interface to be notified of the lifecycle changes. When they do implement that
 * interface, the <code>init</code> method that is to be supported takes a parameter
 * context of type <code>Object</code>. Since the type of container into which the
 * service implementation class is deployed is variable, the input param type is
 * set as <code>Object</code>. But if the container is a servlet engine then the spec
 * mentions that the context should certainly be of the type ServletEndpointContext.
 * Hence this class.
 * @author sunja07
 */
public class ServletEndpointContextImpl implements ServletEndpointContext {

	private HttpSession  httpSession=null;
	private ServletContext servletContext=null;
	private MessageContext msgContext=null;
	
	//To support several getters I've increased the inputParams of the constructor
	//So be cautious in the underlying JAX-WS implementation as to how you would
	//instantiate an object of this type, before passing it as the input argument
	//to the service implementation class's init() method.
	public ServletEndpointContextImpl(HttpSession hs, ServletContext sc, MessageContext mc) {
		this.httpSession = hs;
		this.servletContext = sc;
		this.msgContext = mc;
	}

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
	public MessageContext getMessageContext() throws IllegalStateException {
		return msgContext;
	}

	/**
	 * Returns a java.security.Principal instance that contains the name of the authenticated user for the current method
	 * invocation on the endpoint instance. This method returns null if there is no associated principal yet. The underlying
	 * JAX-RPC runtime system takes the responsibility of providing the appropriate authenticated principal for a remote method
	 * nvocation on the service endpoint instance.
	 * 
	 * @return A java.security.Principal for the authenticated principal associated with the current invocation on the servlet  endpoint instance; Returns null if there no authenticated user associated with a method invocation.
	 */
	public Principal getUserPrincipal() {
		//This is a functionality that comes handy when implementing security.
		//I'm deferring this for now.
		
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * The getHttpSession method returns the current HTTP session (as a javax.servlet.http.HTTPSession). When
	 * invoked by the service endpoint within a remote method implementation, the getHttpSession returns the HTTP session
	 * associated currently with this method invocation. This method returns null if there is no HTTP session currently active
	 * and associated with this service endpoint. An endpoint class should not rely on an active HTTP session being always there;
	 * the underlying JAX-RPC runtime system is responsible for managing whether or not there is an active HTTP session.
	 * 
	 * @return The HTTP session associated with the current invocation or null if there is no active session.
	 * @throws WebServiceException - If this method invoked by any non-HTTP bound endpoint
	 */
	public HttpSession getHttpSession() throws WebServiceException {
		return httpSession;
	}

	/**
	 * The method getServletContext returns the ServletContext associated with the web application that contain this
	 * endpoint. According to the Servlet specification, There is one context per web application (installed as a WAR) per JVM .
	 * A servlet based service endpoint is deployed as part of a web application.
	 * 
	 * @return ServletContext
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param role - a String specifying the name of the role
	 * @return a boolean indicating whether the authenticated user associated with the current method invocation belongs to a
	 * given role; false if the user has not been authenticated
	 */
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		
		//We shold check for authentication of user, and if that isn't valid we would
		//straight away return false.
		//----------
		//code block
		//----------
		
		//Next we should check if the user associated with the current method incocation
		//belongs to the specified role
		//----------
		//code block
		//----------
		
		//This is also a functionality that comes handy when implementing security.
		//I'm deferring this for now.
		return false;
	}

}
