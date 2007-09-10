/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.context;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.addressing.metadata.WSDLLocation;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.factory.EndpointReferenceFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceBuilder;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

import java.net.URI;
import java.security.Principal;

public class WebServiceContextImpl implements WebServiceContext {

    private static final Log log = LogFactory.getLog(WebServiceContext.class);
    
    private MessageContext soapMessageContext;

    public WebServiceContextImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.WebServiceContext#getMessageContext()
     */
    public MessageContext getMessageContext() {
        return soapMessageContext;
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.WebServiceContext#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        if (soapMessageContext != null) {
            HttpServletRequest request = (HttpServletRequest) soapMessageContext.get(MessageContext.SERVLET_REQUEST);
            if (request != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Access to the user Principal was requested.");
                }
                return request.getUserPrincipal();    
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("No HttpServletRequest object was found, so no Principal can be found.");
                }
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.WebServiceContext#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String user) {
        if (soapMessageContext != null) {
            HttpServletRequest request = (HttpServletRequest) soapMessageContext.get(MessageContext.SERVLET_REQUEST);
            if (request != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Checking to see if the user in the role.");
                }
                return request.isUserInRole(user);    
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("No HttpServletRequest object was found, so no role check can be performed.");
                }
            }
        }
        
        return false;
    }

    public void setSoapMessageContext(MessageContext soapMessageContext) {
        this.soapMessageContext = soapMessageContext;
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        EndpointReference jaxwsEPR = null;
        String addressingNamespace = getAddressingNamespace(clazz);
        
        //TODO: Need to understand how the binding can influence the behaviour of this method.
        
        if (soapMessageContext != null) {
            QName service = (QName) soapMessageContext.get(MessageContext.WSDL_SERVICE);
            QName endpoint = (QName) soapMessageContext.get(MessageContext.WSDL_PORT);
            URI wsdlURI = (URI) soapMessageContext.get(MessageContext.WSDL_DESCRIPTION);
            
            org.apache.axis2.addressing.EndpointReference axis2EPR =
                new EndpointReferenceBuilder().createEndpointReference(null, service, endpoint, wsdlURI.toString(), addressingNamespace);
            
            try {
                if (referenceParameters != null) {
                    for (Element element : referenceParameters) {
                        OMElement omElement = XMLUtils.toOM(element);
                        axis2EPR.addReferenceParameter(omElement);
                    }            
                }
                
                jaxwsEPR = EndpointReferenceConverter.convertFromAxis2(axis2EPR, addressingNamespace);
            }
            catch (Exception e) {
                //TODO NLS enable.
                throw ExceptionFactory.makeWebServiceException("Error creating endpoint reference", e);
            }
        }
        else {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("Unable to create endpoint references.");        	
        }
        
        return clazz.cast(jaxwsEPR);
    }

    public EndpointReference getEndpointReference(Element... referenceParameters) {
        return getEndpointReference(W3CEndpointReference.class, referenceParameters);
    }

    private String getAddressingNamespace(Class clazz) {
        EndpointReferenceFactory eprFactory =
            (EndpointReferenceFactory) FactoryRegistry.getFactory(EndpointReferenceFactory.class);
        return eprFactory.getAddressingNamespace(clazz);
    }
}
