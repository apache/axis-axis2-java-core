package org.apache.axis.impl.llom.serialize;

import org.apache.axis.om.*;
import org.apache.axis.impl.llom.OMNodeImpl;
import org.apache.axis.impl.llom.OMElementImpl;
import org.apache.axis.impl.llom.util.StreamWriterToContentHandlerConverter;
import org.apache.axis.impl.llom.builder.ObjectToOMBuilder;
import org.xml.sax.ContentHandler;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;
import java.util.Stack;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * todo - A pretty specific builder. To be improved!
 */
public class SimpleObjectOMSerializer {

    private Stack prefixStack = new Stack();
    private ObjectToOMBuilder builder;

    public void serialize(Object omNode, XMLStreamWriter writer,boolean cache) throws XMLStreamException {

        if (!(omNode instanceof OMElementImpl)) {
            throw new UnsupportedOperationException("Unsupported input object. Must be of the the type OMElementImpl!!");
        }

        OMElementImpl node = (OMElementImpl) omNode;

        if (!(node.getBuilder() instanceof ObjectToOMBuilder)){
            throw new UnsupportedOperationException("Unsupported builder for this serializer");
        }

        this.builder = (ObjectToOMBuilder)node.getBuilder();
        ContentHandler externalhandler = new StreamWriterToContentHandlerConverter(writer);
        this.builder.setExternalContentHandler(externalhandler);
        this.builder.setCache(cache);

        serializeNode(node, writer);
    }

    protected void serializeNode(OMNode node, XMLStreamWriter writer) throws XMLStreamException {
        short nodeType = node.getType();
        if (nodeType == OMNode.ELEMENT_NODE) {
            serializeElement((OMElement) node, writer);
        } else if (nodeType == OMNode.ATTRIBUTE_NODE) {
            serializeAttribute((OMAttribute) node, writer);
        } else if (nodeType == OMNode.TEXT_NODE) {
            serializeText((OMText) node, writer);
        } else if (nodeType == OMNode.COMMENT_NODE) {
            serializeComment((OMText) node, writer);
        } else if (nodeType == OMNode.CDATA_SECTION_NODE) {
            serializeCData((OMText) node, writer);
        }
        writer.flush();
    }

    /**
     * @param element
     */
    protected void serializeElement(OMElement element, XMLStreamWriter writer) throws XMLStreamException {

        int nsPushCount = 0;
        OMNamespace ns = element.getNamespace();
        String prefix = null;
        String nameSpaceName = null;

        if (ns != null) {
            prefix = ns.getPrefix();
            nameSpaceName = ns.getName();

            if (prefix != null) {
                writer.writeStartElement(prefix, element.getLocalName(), nameSpaceName);
                if (serializeNamespace(ns, writer))
                        nsPushCount++;
            } else {
                writer.writeStartElement(nameSpaceName, element.getLocalName());
                //add the own namespace
                // writer.writeDefaultNamespace(nameSpaceName);
            }
        }

        //add the elements attributes
        Iterator attributes = element.getAttributes();
        while (attributes.hasNext()) {
            serializeAttribute((OMAttribute) attributes.next(), writer);
        }

        //add the namespaces
        Iterator namespaces = element.getAllDeclaredNamespaces();
        while (namespaces.hasNext()) {
            if (serializeNamespace((OMNamespace) namespaces.next(), writer))
                    nsPushCount++;
        }

        //add the children
        Iterator children = element.getChildren();
        while (children.hasNext()) {
            Object node = children.next();
            if (node != null) {
                serializeNode((OMNode) node, writer);
            }
        }

        writer.writeEndElement();
        //remove the namespace prefixes from the stack
        for (int i=0;i<nsPushCount;i++){
            prefixStack.pop();
        }
    }

    /**
     */
    protected void serializeText(OMText text, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters(text.getValue());
    }

    protected void serializeCData(OMText text, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCData(text.getValue());
    }


    protected void serializeComment(OMText text, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeComment(text.getValue());
    }

    /**
     * @param attr
     * @param writer
     * @throws javax.xml.stream.XMLStreamException
     */


    protected void serializeAttribute(OMAttribute attr, XMLStreamWriter writer) throws XMLStreamException {

        //first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        String prefix = null;
        String namespaceName = null;
        if (ns != null) {
            //add the prefix if it's availble
            prefix = ns.getPrefix();
            namespaceName = ns.getName();

            if (prefix != null)
                writer.writeAttribute(prefix, namespaceName, attr.getLocalName(), attr.getValue());
            else
                writer.writeAttribute(namespaceName, attr.getLocalName(), attr.getValue());
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getValue());
        }

    }

    protected boolean serializeNamespace(OMNamespace namespace, XMLStreamWriter writer) throws XMLStreamException {
        boolean nsWritten = false;
        if (namespace != null) {
            String prefix = namespace.getPrefix();
            if (!prefixStack.contains(prefix)) {
                writer.writeNamespace(prefix, namespace.getName());
                prefixStack.push(prefix);
                nsWritten = true;
            }
        }

        return nsWritten;
    }

}
