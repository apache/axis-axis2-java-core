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

/**
 * 
 */
package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.databinding.SOAPEnvelopeBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.WebServiceException;

/**
 * 
 *
 */
public class SOAPEnvelopeBlockImpl extends BlockImpl<SOAPEnvelope,Void> implements SOAPEnvelopeBlock {

    /**
     * Called by SOAPEnvelopeBlockFactory
     *
     * @param busObject
     * @param busContext
     * @param qName
     * @param factory
     */
    public SOAPEnvelopeBlockImpl(SOAPEnvelope busObject,
                                 QName qName, BlockFactory factory) {
        super(busObject,
              null,
              (qName == null) ? getQName(busObject) : qName,
              factory);
    }

    /**
     * Called by SOAPEnvelopeBlockFactory
     *
     * @param omElement
     * @param busContext
     * @param qName
     * @param factory
     */
    public SOAPEnvelopeBlockImpl(OMElement omElement,
                                 QName qName, BlockFactory factory) {
        super(omElement, null, qName, factory);
    }

    @Override
    protected SOAPEnvelope _getBOFromOM(OMElement omElement, Void busContext)
            throws XMLStreamException, WebServiceException {
        MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
        Message message = mf.createFrom(omElement.getXMLStreamReader(false), null);
        SOAPEnvelope env = message.getAsSOAPEnvelope();
        this.setQName(getQName(env));
        return env;
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.impl.BlockImpl#_getReaderFromBO(java.lang.Object, java.lang.Object)
      */
    @Override
    protected XMLStreamReader _getReaderFromBO(SOAPEnvelope busObj, Void busContext)
            throws XMLStreamException, WebServiceException {
        return OMXMLBuilderFactory.createOMBuilder(new DOMSource(busObj)).getDocument().getXMLStreamReader(false);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.impl.BlockImpl#_outputFromBO(java.lang.Object, java.lang.Object, javax.xml.stream.XMLStreamWriter)
      */
    @Override
    protected void _outputFromBO(SOAPEnvelope busObject, Void busContext,
                                 XMLStreamWriter writer)
            throws XMLStreamException, WebServiceException {
        XMLStreamReader reader = _getReaderFromBO(busObject, busContext);
        _outputFromReader(reader, writer);
    }

    /**
     * Get the QName of the envelope
     *
     * @param env
     * @return QName
     */
    private static QName getQName(SOAPEnvelope env) {
        return new QName(env.getNamespaceURI(), env.getLocalName(), env.getPrefix());
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
