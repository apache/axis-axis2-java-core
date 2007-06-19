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
package org.apache.axis2.jaxws.context;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

import java.security.Principal;

public class WebServiceContextImpl implements WebServiceContext {

    private MessageContext soapMessageContext;

    public WebServiceContextImpl() {
        super();
        // TODO Auto-generated constructor stub
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
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.ws.WebServiceContext#isUserInRole(java.lang.String)
      */
    public boolean isUserInRole(String s) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSoapMessageContext(MessageContext soapMessageContext) {
        this.soapMessageContext = soapMessageContext;
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        T jaxwsEPR = null;
        
        
        return jaxwsEPR;
    }

    public EndpointReference getEndpointReference(Element... arg0) {
        return getEndpointReference(W3CEndpointReference.class, arg0);
    }
}
