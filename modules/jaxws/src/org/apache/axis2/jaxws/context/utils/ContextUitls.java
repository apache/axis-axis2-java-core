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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContextUitls {
    private static final Log log = LogFactory.getLog(ContextUitls.class);

    /**
     * Adds the appropriate properties to the MessageContext that the user will see
     * @param soapMessageContext 
     * @param jaxwsMessageContext
     */
	public static void addProperties(SOAPMessageContext soapMessageContext, MessageContext jaxwsMessageContext){
        org.apache.axis2.context.MessageContext axisMsgContext = jaxwsMessageContext.getAxisMessageContext();
           
        // Copy Axis2 MessageContext options.  It's possible that some set of Axis2 handlers
        // have run and placed some properties in the context that need to be visible.  
        // We don't, however, want to expose the Axis2 Operation/ServiceContext properties.
        Map props = axisMsgContext.getOptions().getProperties();
        soapMessageContext.putAll(props);
        
        // Set the WSDL properties
		ServiceDescription sd = jaxwsMessageContext.getServiceDescription();
		URL wsdlLocation = ((ServiceDescriptionWSDL) sd).getWSDLLocation();
		if (wsdlLocation != null && !"".equals(wsdlLocation)){
            URI wsdlLocationURI = null;
            try {
                wsdlLocationURI = wsdlLocation.toURI();
            }
            catch (URISyntaxException ex) {
                // TODO: NLS/RAS
                log.warn("Unable to convert WSDL location URL to URI.  URL: " + wsdlLocation.toString() + "; Service: " + sd.getServiceQName() , ex);
            }
		    soapMessageContext.put(javax.xml.ws.handler.MessageContext.WSDL_DESCRIPTION, wsdlLocationURI);
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
		  
		// If we are running within a servlet container, then JAX-WS requires that the
        // servlet related properties be set on the MessageContext
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT));
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT, Scope.APPLICATION);
		   
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_REQUEST, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST));
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_REQUEST, Scope.APPLICATION);
		   
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE));
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE, Scope.APPLICATION);
		   
		// Set the transport properties
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS, axisMsgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS, Scope.APPLICATION);
		   
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD, axisMsgContext.getProperty(Constants.Configuration.HTTP_METHOD));
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD, Scope.APPLICATION);
		   
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE, axisMsgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE));
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE, Scope.APPLICATION);
		   
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS, null);
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS, Scope.APPLICATION);
		   
		// Set the message properties
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY, null);
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY, Scope.APPLICATION);
		   
		soapMessageContext.put(javax.xml.ws.handler.MessageContext.MESSAGE_ATTACHMENTS, null);
		soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.MESSAGE_ATTACHMENTS, Scope.APPLICATION);
	}
}
