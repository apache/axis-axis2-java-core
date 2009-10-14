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

package org.apache.axis2.jaxws.handler;

import org.apache.axiom.soap.RolePlayer;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The SOAPMessageContext is the context handed to SOAP-based application handlers.  It provides
 * access to the SOAP message that represents the request or response via SAAJ.  It also allows
 * access to any properties that have been registered and set on the MessageContext.
 */
public class SoapMessageContext extends BaseMessageContext implements
        javax.xml.ws.handler.soap.SOAPMessageContext {
    private static final Log log = LogFactory.getLog(SoapMessageContext.class);
    
    // cache the message object after transformation --- see getMessage and setMessage methods
    Message cachedMessage = null;
    SOAPMessage cachedSoapMessage = null;
    
    public SoapMessageContext(MessageContext messageCtx) {
        super(messageCtx);
    }

    public Object[] getHeaders(QName qname, JAXBContext jaxbcontext, boolean allRoles) {
        if(log.isDebugEnabled()){
            log.debug("Getting all Headers for Qname: "+qname);
        }

        if(qname == null){
            if(log.isDebugEnabled()){
                log.debug("Invalid QName, QName cannot be null");
            }
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("soapMessageContextErr1"));
        }
        if(jaxbcontext == null){
            if(log.isDebugEnabled()){
                log.debug("Invalid JAXBContext, JAXBContext cannot be null");
            }
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("SOAPMessageContextErr2"));
        }

        // The header information is returned as a list of jaxb objects
        List<Object> list = new ArrayList<Object>();
        String namespace = qname.getNamespaceURI();
        String localPart = qname.getLocalPart();
        BlockFactory blockFactory = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        Message m = messageCtx.getMessage();
        JAXBBlockContext jbc = new JAXBBlockContext(jaxbcontext);
        
        // If allRoles is not specified, pass in a set of roles.
        // The headers must support that role.
        RolePlayer rolePlayer = null;
        if (allRoles == false) {
            rolePlayer = getRolePlayer();
        }
        
        if(m.getNumHeaderBlocks()>0){
            // Get the list of JAXB Blocks
            List<Block> blockList = m.getHeaderBlocks(namespace, 
                                               localPart, 
                                               jbc, 
                                               blockFactory,
                                               rolePlayer);
            
            // Create list of JAXB objects
            if(blockList!=null && blockList.size() > 0){
                try{
                    Iterator it = blockList.iterator();
                    while (it.hasNext()) {
                        Block block = (Block) it.next();
                        Object bo = block.getBusinessObject(false);
                        if(bo!=null){
                            if(log.isDebugEnabled()){
                                log.debug("Extracted BO from Header Block");
                            }
                            list.add(bo);
                        }
                    }

                }catch(XMLStreamException e){
                    throw ExceptionFactory.makeWebServiceException(e);
                }
            }
        }
        return list.toArray(new Object[0]);
        
    }

    public SOAPMessage getMessage() {
        // set a property to indicate that we are accessing the message
        if(log.isDebugEnabled()){
            log.debug("getMessage - accessing message.");
        }
        this.put("jaxws.isMessageAccessed", true);
        Message msg = messageCtx.getMEPContext().getMessageObject();
        if (msg != cachedMessage) {
            cachedMessage = msg;
            cachedSoapMessage = msg.getAsSOAPMessage();
        } 
        return cachedSoapMessage;
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
            cachedMessage = msg;
            cachedSoapMessage = soapmessage;
        } catch (XMLStreamException e) {
            // TODO log it, and throw something?
        }
    }
    
    private RolePlayer getRolePlayer() {
        List roles = new ArrayList(getRoles());
        return new SMCRolePlayer(roles);
    }
    
    class SMCRolePlayer implements RolePlayer {
        private List roles;
        SMCRolePlayer(List roles) {
            this.roles = roles;
        }
        
        public List getRoles() {
            return roles;
        }
        public boolean isUltimateDestination() {
            return true;
        }
    }
}
