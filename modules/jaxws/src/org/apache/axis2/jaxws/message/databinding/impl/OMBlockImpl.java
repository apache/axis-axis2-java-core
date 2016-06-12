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

package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.databinding.OMBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

/** OMBlockImpl Block with a business object that is an OMElement */
public class OMBlockImpl extends BlockImpl<OMElement,Void> implements OMBlock {


    /**
     * Called by OMBlockFactory
     *
     * @param busObject
     * @param factory
     */
    OMBlockImpl(OMElement busObject, BlockFactory factory) {
        super(null,
              busObject,
              busObject.getQName(),
              factory);
    }

    @Override
    protected XMLStreamReader _getReaderFromBO(OMElement busObj, Void busContext)
            throws XMLStreamException, WebServiceException {
        return busObj.getXMLStreamReader();
    }
    
    @Override
    protected OMElement _getBOFromOM(OMElement om, Void busContext)
        throws XMLStreamException, WebServiceException {
        return om;
    }
    
    @Override
    protected OMElement _getOMFromBO(OMElement busObject, Void busContext)
        throws XMLStreamException, WebServiceException {
        return busObject;
    }

    @Override
    protected void _outputFromBO(OMElement busObject, Void busContext, XMLStreamWriter writer)
            throws XMLStreamException, WebServiceException {
        busObject.serialize(writer);
    }

    @Override
    public boolean isElementData() {
        return true;
    }

    @Override
    public void close() {
        return; // Nothing to close
    }

    @Override
    public Object getObject() {
        try {
            return getBusinessObject(false);
        } catch (XMLStreamException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    @Override
    public boolean isDestructiveRead() {
        return false;
    }

    @Override
    public boolean isDestructiveWrite() {
        return false;
    }
}
