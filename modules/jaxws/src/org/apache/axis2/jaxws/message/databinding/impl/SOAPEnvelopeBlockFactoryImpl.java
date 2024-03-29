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
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockFactoryImpl;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import jakarta.xml.ws.WebServiceException;

/** Creates a SOAPEnvelopeBlock */
public class SOAPEnvelopeBlockFactoryImpl extends BlockFactoryImpl implements
        SOAPEnvelopeBlockFactory {

    /** Default Constructor required for Factory */
    public SOAPEnvelopeBlockFactoryImpl() {
        super();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.BlockFactory#createFrom(org.apache.axiom.om.OMElement, java.lang.Object, javax.xml.namespace.QName)
      */
    public Block createFrom(OMElement omElement, Object context, QName qName)
            throws XMLStreamException {
        return new SOAPEnvelopeBlockImpl(omElement, qName, this);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.BlockFactory#createFrom(java.lang.Object, java.lang.Object, javax.xml.namespace.QName)
      */
    public Block createFrom(Object businessObject, Object context, QName qName)
            throws WebServiceException {
        return new SOAPEnvelopeBlockImpl((SOAPEnvelope)businessObject, qName, this);
    }

    public boolean isElement() {
        return true;
    }

}
