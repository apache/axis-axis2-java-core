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

import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

public class LogicalMessageImpl implements LogicalMessage {

    private Message message;
    
    public LogicalMessageImpl(Message m) {
        message = m;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#getPayload()
     */
    public Source getPayload() {
        BlockFactory factory = (SourceBlockFactory) FactoryRegistry.getFactory(SourceBlockFactory.class);
        Source payload = (Source) _getPayload(null, factory);
        return payload;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#getPayload(javax.xml.bind.JAXBContext)
     */
    public Object getPayload(JAXBContext context) {
        BlockFactory factory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        JAXBBlockContext jbc = new JAXBBlockContext(context);
        Object payload = _getPayload(jbc, factory);
        return payload;
    }
    
    private Object _getPayload(Object context, BlockFactory factory) {
        Object payload = null;
        try {
            Block block = message.getBodyBlock(context, factory);
            payload = block.getBusinessObject(true);
            
            // For now, we have to create a new Block from the original content
            // and set that back on the message.  The Block is not currently
            // able to create a copy of itself just yet.
            Block cacheBlock = factory.createFrom(payload, context, block.getQName());
            message.setBodyBlock(cacheBlock);
        } catch (WebServiceException e) {
            ExceptionFactory.makeWebServiceException(e);
        } catch (XMLStreamException e) {
            ExceptionFactory.makeWebServiceException(e);
        }
        
        return payload;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#setPayload(java.lang.Object, javax.xml.bind.JAXBContext)
     */
    public void setPayload(Object obj, JAXBContext context) {
        BlockFactory factory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        JAXBBlockContext jbc = new JAXBBlockContext(context);
        _setPayload(obj, jbc, factory);
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.LogicalMessage#setPayload(javax.xml.transform.Source)
     */
    public void setPayload(Source source) {
        BlockFactory factory = (SourceBlockFactory) FactoryRegistry.getFactory(SourceBlockFactory.class);
        _setPayload(source, null, factory);
    }
    
    private void _setPayload(Object object, Object context, BlockFactory factory) {
        Block block = factory.createFrom(object, context, null);
        
        if (message != null) {
            message.setBodyBlock(block);
        }
    }
}