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

import javax.xml.bind.JAXBContext;

import org.apache.axis2.jaxws.AxisController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

public class JAXBDispatch<T> extends BaseDispatch<T> {

    private JAXBContext jaxbContext;
    
    public JAXBDispatch() {
        super();
    }
    
    public JAXBDispatch(AxisController ac) {
        super(ac);
    }
    
    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }
    
    public void setJAXBContext(JAXBContext jbc) {
        jaxbContext = jbc;
    }
    
    public AsyncListener createAsyncListener() {
        JAXBDispatchAsyncListener listener = new JAXBDispatchAsyncListener();
        listener.setJAXBContext(jaxbContext);
        listener.setMode(mode);
        return listener;
    }
    
    public Message createMessageFromValue(Object value) {
        Message message = null;
        try {
            MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);

            // FIXME: The protocol should actually come from the binding information included in
            // either the WSDL or an annotation.
            message = mf.create(Protocol.soap11);
            
            JAXBBlockFactory factory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
            Block block = factory.createFrom(value, jaxbContext, null);
            
            message.setBodyBlock(0, block);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        return message;
    }

    public Object getValueFromMessage(Message message) {
        Object value = null;
        try {
            JAXBBlockFactory factory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
            
            Block block = message.getBodyBlock(0, jaxbContext, factory);
            value = block.getBusinessObject(true);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        return value;
    }
}
