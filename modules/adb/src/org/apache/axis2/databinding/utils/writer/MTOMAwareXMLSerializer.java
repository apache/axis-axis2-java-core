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

package org.apache.axis2.databinding.utils.writer;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.llom.OMTextImpl;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * this class wrapps the existing xmlStreamWriter and implements the new method
 * writeDataHandler
 */
public class MTOMAwareXMLSerializer implements MTOMAwareXMLStreamWriter {

    private XMLStreamWriter xmlStreamWriter;

    public MTOMAwareXMLSerializer(XMLStreamWriter xmlStreamWriter) {
        this.xmlStreamWriter = xmlStreamWriter;
    }

    public void writeStartElement(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeStartElement(string);
    }

    public void writeStartElement(String string, String string1) throws XMLStreamException {
        this.xmlStreamWriter.writeStartElement(string, string1);
    }

    public void writeStartElement(String string, String string1, String string2) throws XMLStreamException {
        this.xmlStreamWriter.writeStartElement(string, string1, string2);
    }

    public void writeEmptyElement(String string, String string1) throws XMLStreamException {
        this.xmlStreamWriter.writeEmptyElement(string, string1);
    }

    public void writeEmptyElement(String string, String string1, String string2) throws XMLStreamException {
        this.xmlStreamWriter.writeEmptyElement(string, string1, string2);
    }

    public void writeEmptyElement(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeEmptyElement(string);
    }

    public void writeEndElement() throws XMLStreamException {
        this.xmlStreamWriter.writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        this.xmlStreamWriter.writeEndDocument();
    }

    public void close() throws XMLStreamException {
        this.xmlStreamWriter.close();
    }

    public void flush() throws XMLStreamException {
        this.xmlStreamWriter.flush();
    }

    public void writeAttribute(String string, String string1) throws XMLStreamException {
        this.xmlStreamWriter.writeAttribute(string, string1);
    }

    public void writeAttribute(String string, String string1, String string2, String string3) throws XMLStreamException {
        this.xmlStreamWriter.writeAttribute(string, string1, string2, string3);
    }

    public void writeAttribute(String string, String string1, String string2) throws XMLStreamException {
        this.xmlStreamWriter.writeAttribute(string, string1, string2);
    }

    public void writeNamespace(String string, String string1) throws XMLStreamException {
        this.xmlStreamWriter.writeNamespace(string, string1);
    }

    public void writeDefaultNamespace(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeDefaultNamespace(string);
    }

    public void writeComment(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeComment(string);
    }

    public void writeProcessingInstruction(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeProcessingInstruction(string);
    }

    public void writeProcessingInstruction(String string, String string1) throws XMLStreamException {
        this.xmlStreamWriter.writeProcessingInstruction(string, string1);
    }

    public void writeCData(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeCData(string);
    }

    public void writeDTD(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeDTD(string);
    }

    public void writeEntityRef(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeEntityRef(string);
    }

    public void writeStartDocument() throws XMLStreamException {
        this.xmlStreamWriter.writeStartDocument();
    }

    public void writeStartDocument(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeStartDocument(string);
    }

    public void writeStartDocument(String string, String string1) throws XMLStreamException {
        this.xmlStreamWriter.writeStartDocument(string, string1);
    }

    public void writeCharacters(String string) throws XMLStreamException {
        this.xmlStreamWriter.writeCharacters(string);
    }

    public void writeCharacters(char[] chars, int i, int i1) throws XMLStreamException {
        this.xmlStreamWriter.writeCharacters(chars, i, i1);
    }

    public String getPrefix(String string) throws XMLStreamException {
        return this.xmlStreamWriter.getPrefix(string);
    }

    public void setPrefix(String string, String string1) throws XMLStreamException {
        this.xmlStreamWriter.setPrefix(string, string1);
    }

    public void setDefaultNamespace(String string) throws XMLStreamException {
        this.xmlStreamWriter.setDefaultNamespace(string);
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        this.xmlStreamWriter.setNamespaceContext(namespaceContext);
    }

    public NamespaceContext getNamespaceContext() {
        return this.xmlStreamWriter.getNamespaceContext();
    }

    public Object getProperty(String string) throws IllegalArgumentException {
        return this.xmlStreamWriter.getProperty(string);
    }

    public void writeDataHandler(DataHandler dataHandler) throws XMLStreamException {
        OMTextImpl omText = new OMTextImpl(dataHandler, OMAbstractFactory.getOMFactory());
        omText.internalSerializeAndConsume(this.xmlStreamWriter);
    }
}
