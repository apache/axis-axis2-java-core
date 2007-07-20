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
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.jaxws.message.databinding.XMLStringBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;
import org.apache.axis2.jaxws.message.util.Reader2Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringReader;

/**
 * XMLStringBlock
 * <p/>
 * Block containing a business object that is a String of xml text
 */
public class XMLStringBlockImpl extends BlockImpl implements XMLStringBlock {

    /**
     * Constructor called from factory
     *
     * @param busObject
     * @param qName
     * @param factory
     */
    XMLStringBlockImpl(String busObject, QName qName, BlockFactory factory) {
        super(busObject, null, qName, factory);
    }


    /**
     * Constructor called from factory
     *
     * @param reader
     * @param qName
     * @param factory
     */
    public XMLStringBlockImpl(OMElement omElement, QName qName, BlockFactory factory) {
        super(omElement, null, qName, factory);
    }

    @Override
    protected Object _getBOFromReader(XMLStreamReader reader, Object busContext)
            throws XMLStreamException {
        // Create a Reader2Writer converter and get the output as a String
        Reader2Writer r2w = new Reader2Writer(reader);
        return r2w.getAsString();
    }

    @Override
    protected XMLStreamReader _getReaderFromBO(Object busObj, Object busContext)
            throws XMLStreamException {
        // Create an XMLStreamReader from the inputFactory using the String as the sources
        String str = (String)busObj;
        StringReader sr = new StringReader(str);
        return StAXUtils.createXMLStreamReader(sr);
    }

    @Override
    protected void _outputFromBO(Object busObject, Object busContext, XMLStreamWriter writer)
            throws XMLStreamException {
        // There is no fast way to output the String to a writer, so get the reader
        // and pass use the default reader->writer.
        XMLStreamReader reader = _getReaderFromBO(busObject, busContext);
        _outputFromReader(reader, writer);
    }


    public boolean isElementData() {
        return false;  // The text could be element or text or something else
    }
}
