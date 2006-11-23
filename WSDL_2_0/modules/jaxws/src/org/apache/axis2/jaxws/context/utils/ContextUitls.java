/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.context.utils;

import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.transport.http.HTTPConstants;

public class ContextUitls {

	public ContextUitls() {
		super();
	}

	public static void addProperties(SOAPMessageContext soapMessageContext, MessageContext jaxwsMessageContext){
		
		   org.apache.axis2.context.MessageContext axisMsgContext = jaxwsMessageContext.getAxisMessageContext();
		   //Set wsdl related properties.
		   ServiceDescription sd = jaxwsMessageContext.getServiceDescription();
		   WSDLWrapper wsdlWrapper = ((ServiceDescriptionWSDL) sd).getWSDLWrapper();
		   if(wsdlWrapper!=null){
			   soapMessageContext.put(javax.xml.ws.handler.MessageContext.WSDL_DESCRIPTION, wsdlWrapper.getDefinition());
			   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_DESCRIPTION, Scope.APPLICATION);
		   }
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.WSDL_OPERATION, axisMsgContext.getAxisOperation().getName());
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_OPERATION, Scope.APPLICATION);
			   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.WSDL_PORT, null);
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_PORT, Scope.APPLICATION);
			   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.WSDL_SERVICE, axisMsgContext.getAxisService().getName());
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_SERVICE,Scope.APPLICATION );
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.WSDL_INTERFACE, null);
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_INTERFACE, Scope.APPLICATION);
		  
		   
		   //Setup all Servlet properties
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT));
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT, Scope.APPLICATION);
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_REQUEST, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST));
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_REQUEST, Scope.APPLICATION);
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE));
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE, Scope.APPLICATION);
		   
		   //All HTTP properties
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS, axisMsgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS, Scope.APPLICATION);
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD, axisMsgContext.getProperty(Constants.Configuration.HTTP_METHOD));
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD, Scope.APPLICATION);
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE));
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE, Scope.APPLICATION);
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS, null);
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS, Scope.APPLICATION);
		   
		   //Message Properties
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY, null);
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY, Scope.APPLICATION);
		   
		   soapMessageContext.put(javax.xml.ws.handler.MessageContext.MESSAGE_ATTACHMENTS, null);
		   soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.MESSAGE_ATTACHMENTS, Scope.APPLICATION);
		  
	}
}
