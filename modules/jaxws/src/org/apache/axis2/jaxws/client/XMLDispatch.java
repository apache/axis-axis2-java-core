/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.client;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XMLDispatch<T> extends BaseDispatch<T> {
    private static final Log log = LogFactory.getLog(XMLDispatch.class);
    
    private Class type;
    private Class blockFactoryType;
    
    //public XMLDispatch() {
    //    super();
    //}
    
    public XMLDispatch(PortData pd) {
        super(pd);
    }
    
    public Class getType() {
        return type;
    }
    
    public void setType(Class c) {
        type = c;
    }
    
    public AsyncListener createAsyncListener() {
        if (log.isDebugEnabled()) {
            log.debug("Creating new AsyncListener for XMLDispatch");
        }
        XMLDispatchAsyncListener al = new XMLDispatchAsyncListener();
        al.setMode(mode);
        al.setType(type);
        al.setBlockFactoryType(blockFactoryType);
        return al;
    }
    
    public Message createMessageFromValue(Object value) {
        type = value.getClass();
        if (log.isDebugEnabled()) {
            log.debug("Parameter type: " + type.getName());
            log.debug("Message mode: " + mode.name());
        }
        
        Block block = null;
        blockFactoryType = getBlockFactory(value);
        BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(blockFactoryType);
        if (log.isDebugEnabled()) {
            log.debug("Loaded block factory type [" + blockFactoryType.getName());
        }
        
        Message message = null;
        if (mode.equals(Mode.PAYLOAD)) {
            try {
                MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
                block = factory.createFrom(value, null, null);
                
                // FIXME: The protocol should actually come from the binding information included in
                // either the WSDL or an annotation.
                message = mf.create(Protocol.soap11);
                message.setBodyBlock(0, block);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (MessageException e) {
                e.printStackTrace();
            }
        }
        else if (mode.equals(Mode.MESSAGE)) {
            try {
                QName soapEnvQname = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
                block = factory.createFrom(value, null, soapEnvQname);
                
                MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
                message = mf.createFrom(block.getXMLStreamReader(true));
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (MessageException e) {
                e.printStackTrace();
            }
        }

        return message;
    }

    public Object getValueFromMessage(Message message) {
        Object value = null;
        Block block = null;
        
        if (log.isDebugEnabled()) {
            log.debug("Attempting to get the value object from the returned message");
        }
        
        try {
            if (mode.equals(Mode.PAYLOAD)) {
                    BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(blockFactoryType);
                    block = message.getBodyBlock(0, null, factory);
            }
            else if (mode.equals(Mode.MESSAGE)) {
                    // TODO: Make this conversion more efficient
                    OMElement messageOM = message.getAsOMElement();
                    QName soapEnvQname = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
                    
                    XMLStringBlockFactory stringFactory = (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
                    Block stringBlock = stringFactory.createFrom(messageOM.toString(), null, soapEnvQname);
                    
                    BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(blockFactoryType);
                    block = factory.createFrom(stringBlock, null);
            }

            if (log.isDebugEnabled()) {
                if (block == null) {
                    log.debug("Block type: " + block.getClass());
                    log.debug("Block contents: " + block.traceString(""));
                }
                else {
                    log.debug("A null block was created");
                }
            }

            value = block.getBusinessObject(true);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("An error occured while creating the block");
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        if (log.isDebugEnabled()) {
            if (value == null) 
                log.debug("Returning a null value");
            else 
                log.debug("Returning value of type: " + value.getClass().getName());
        }
        
        return value;
    }
    
    private Class getBlockFactory(Object o) {
        if (o instanceof String) {
            System.out.println(">> returning XMLStringBlockFactory");
            return XMLStringBlockFactory.class;
        }
        else if (Source.class.isAssignableFrom(o.getClass())) {
            System.out.println(">> returning SourceBlockFactory");
            return SourceBlockFactory.class;
        }
        
        System.out.println(">> ERROR: Factory not found");
        return null;
    }
}
