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
package org.apache.axis2.jaxws.handler;

import org.apache.axis2.jaxws.core.MEPContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import java.util.HashSet;
import java.util.Set;

/**
 * The SOAPMessageContext is the context handed to SOAP-based application handlers.  It provides
 * access to the SOAP message that represents the request or response via SAAJ.  It also allows
 * access to any properties that have been registered and set on the MessageContext.
 */
public class SoapMessageContext extends BaseMessageContext implements
        javax.xml.ws.handler.soap.SOAPMessageContext {

    public SoapMessageContext(MessageContext messageCtx) {
        super(messageCtx);
    }

    public Object[] getHeaders(QName qname, JAXBContext jaxbcontext, boolean flag) {
        return null;
    }

    public SOAPMessage getMessage() {
        Message msg = messageCtx.getMEPContext().getMessageObject();
        return msg.getAsSOAPMessage();
    }

    public Set<String> getRoles() {
        // TODO implement better.  We should be doing smarter checking of the header,
        // especially for the Ultimate receiver actor/role

        /*
         * JAVADOC to help get this implemented correctly:
         * 
         * Gets the SOAP actor roles associated with an execution of the handler
         * chain. Note that SOAP actor roles apply to the SOAP node and are
         * managed using SOAPBinding.setRoles and SOAPBinding.getRoles. Handler
         * instances in the handler chain use this information about the SOAP
         * actor roles to process the SOAP header blocks. Note that the SOAP
         * actor roles are invariant during the processing of SOAP message
         * through the handler chain.
         */
        
        HashSet<String> roles = new HashSet<String>(3);
        // JAX-WS 10.1.1.1 defaults:
        // SOAP 1.1
        roles.add(SOAPConstants.URI_SOAP_ACTOR_NEXT);
        // SOAP 1.2
        roles.add(SOAPConstants.URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER);
        roles.add(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
        return roles;
    }

    public void setMessage(SOAPMessage soapmessage) {
        // TODO I don't like this at all.
        try {
            Message msg =
                    ((MessageFactory) FactoryRegistry.getFactory(MessageFactory.class)).createFrom(soapmessage);
            messageCtx.getMEPContext().setMessage(msg);
        } catch (XMLStreamException e) {
            // TODO log it, and throw something?
        }
    }
}
