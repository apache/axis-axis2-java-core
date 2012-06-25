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
package org.apache.axis2.jaxbri;

import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.SAXOMBuilder;

public class JaxbRIDataSource implements org.apache.axiom.om.OMDataSource {
    private final JAXBContext context;
    
    /**
     * Bound object for output.
     */
    private final Object outObject;

    /**
     * Constructor from context and object.
     *
     * @param context
     * @param obj
     */
    public JaxbRIDataSource(JAXBContext context, Object obj) {
        this.context = context;
        this.outObject = obj;
    }

    public void serialize(java.io.OutputStream output, org.apache.axiom.om.OMOutputFormat format) throws XMLStreamException {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(outObject, output);
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }

    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(outObject, writer);
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(outObject, xmlWriter);
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        try {
            SAXOMBuilder builder = new SAXOMBuilder();
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(outObject, builder);

            return builder.getRootElement().getXMLStreamReader();
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }
}
