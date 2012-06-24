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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
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
     * Bound class for output.
     */
    private final Class outClazz;

    /**
     * Marshaller.
     */
    private final Marshaller marshaller;

    /**
     * Namespace
     */
    private String nsuri;

    /**
     * Local name
     */
    private String name;

    /**
     * Constructor from object and marshaller.
     *
     * @param obj
     * @param marshaller
     */
    public JaxbRIDataSource(JAXBContext context, Class clazz, Object obj, Marshaller marshaller, String nsuri, String name) {
        this.context = context;
        this.outClazz = clazz;
        this.outObject = obj;
        this.marshaller = marshaller;
        this.nsuri = nsuri;
        this.name = name;
    }

    public void serialize(java.io.OutputStream output, org.apache.axiom.om.OMOutputFormat format) throws XMLStreamException {
        try {
            marshaller.marshal(new JAXBElement(
                    new QName(nsuri, name), outObject.getClass(), outObject), output);
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }

    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            marshaller.marshal(new JAXBElement(
                    new QName(nsuri, name), outObject.getClass(), outObject), writer);
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            marshaller.marshal(new JAXBElement(
                    new QName(nsuri, name), outObject.getClass(), outObject), xmlWriter);
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }

    public javax.xml.stream.XMLStreamReader getReader() throws javax.xml.stream.XMLStreamException {
        try {
            SAXOMBuilder builder = new SAXOMBuilder();
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(new JAXBElement(
                    new QName(nsuri, name), outObject.getClass(), outObject), builder);

            return builder.getRootElement().getXMLStreamReader();
        } catch (JAXBException e) {
            throw new XMLStreamException("Error in JAXB marshalling", e);
        }
    }
}
