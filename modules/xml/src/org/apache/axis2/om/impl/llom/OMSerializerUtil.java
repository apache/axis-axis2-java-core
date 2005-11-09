/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.om.impl.llom;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.serialize.StreamingOMSerializer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

public class OMSerializerUtil {
    private static int namespaceCounter = 0;

    /**
     * Method serializeEndpart
     *
     * @param omOutput
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public static void serializeEndpart(OMOutputImpl omOutput)
            throws XMLStreamException {
        omOutput.getXmlStreamWriter().writeEndElement();
    }

    /**
     * Method serializeAttribute
     *
     * @param attr
     * @param omOutput
     * @throws XMLStreamException
     */
    public static void serializeAttribute(OMAttribute attr, OMOutputImpl omOutput)
            throws XMLStreamException {

        // first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        String prefix = null;
        String namespaceName = null;
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (ns != null) {

            // add the prefix if it's availble
            prefix = ns.getPrefix();
            namespaceName = ns.getName();
            if (prefix != null) {
                writer.writeAttribute(prefix, namespaceName,
                        attr.getLocalName(), attr.getAttributeValue());
            } else {
                writer.writeAttribute(namespaceName, attr.getLocalName(),
                        attr.getAttributeValue());
            }
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getAttributeValue());
        }
    }

    /**
     * Method serializeNamespace
     *
     * @param namespace
     * @param omOutput
     * @throws XMLStreamException
     */
    public static void serializeNamespace(OMNamespace namespace, org.apache.axis2.om.impl.OMOutputImpl omOutput)
            throws XMLStreamException {

        if (namespace != null) {
            XMLStreamWriter writer = omOutput.getXmlStreamWriter();
            String uri = namespace.getName();
            String prefix = writer.getPrefix(uri);
            String ns_prefix = namespace.getPrefix();
            if (ns_prefix != null && !ns_prefix.equals(prefix)) {
                writer.writeNamespace(ns_prefix, namespace.getName());
            }
        }
    }


    /**
     * Method serializeStartpart
     *
     * @param omOutput
     * @throws XMLStreamException
     */
    public static void serializeStartpart(OMElement element, OMOutputImpl omOutput)
            throws XMLStreamException {
        String nameSpaceName = null;
        String writer_prefix = null;
        String prefix = null;
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (element.getNamespace() != null) {
            nameSpaceName = element.getNamespace().getName();
            writer_prefix = writer.getPrefix(nameSpaceName);
            prefix = element.getNamespace().getPrefix();
            if (nameSpaceName != null) {
                if (writer_prefix != null) {
                    writer.writeStartElement(nameSpaceName,
                            element.getLocalName());
                } else {
                    prefix = (prefix == null) ? "" : prefix;
                    writer.writeStartElement(prefix, element.getLocalName(),
                            nameSpaceName);
                    writer.writeNamespace(prefix, nameSpaceName);
                    writer.setPrefix(prefix, nameSpaceName);
                }
            } else {
                writer.writeStartElement(element.getLocalName());
            }
        } else {
            writer.writeStartElement(element.getLocalName());
            // we need to check whether there's a default namespace visible at this point because
            // otherwise this element will go into that namespace unintentionally. So we check
            // whether there is a default NS visible and if so turn it off.
            if (writer.getNamespaceContext().getNamespaceURI("") != null) {
                writer.writeDefaultNamespace("");
            }
        }

        // add the namespaces
        serializeNamespaces(element, omOutput);

        // add the elements attributes
        serializeAttributes(element, omOutput);
    }

    public static void serializeNamespaces(OMElement element,
                                           org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        Iterator namespaces = element.getAllDeclaredNamespaces();
        if (namespaces != null) {
            while (namespaces.hasNext()) {
                serializeNamespace((OMNamespace) namespaces.next(), omOutput);
            }
        }
    }

    public static void serializeAttributes(OMElement element,
                                           org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        if (element.getAllAttributes() != null) {
            Iterator attributesList = element.getAllAttributes();
            while (attributesList.hasNext()) {
                serializeAttribute((OMAttribute) attributesList.next(),
                        omOutput);
            }
        }
    }


    /**
     * Method serializeNormal
     *
     * @param omOutput
     * @param cache
     * @throws XMLStreamException
     */
    public static void serializeNormal(OMElement element, OMOutputImpl omOutput, boolean cache)
            throws XMLStreamException {

        if (cache) {
            element.build();
        }

        serializeStartpart(element, omOutput);
        OMNode firstChild = element.getFirstOMChild();
        if (firstChild != null) {
            if (cache) {
                firstChild.serialize(omOutput);
            } else {
                firstChild.serializeAndConsume(omOutput);
            }
        }
        serializeEndpart(omOutput);
    }

    public static void serializeByPullStream(OMElement element, org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        serializeByPullStream(element, omOutput, false);
    }

    public static void serializeByPullStream(OMElement element, org.apache.axis2.om.impl.OMOutputImpl omOutput, boolean cache) throws XMLStreamException {
        StreamingOMSerializer streamingOMSerializer = new StreamingOMSerializer();
        if (cache) {
            streamingOMSerializer.serialize(element.getXMLStreamReader(),
                    omOutput);
        } else {
            XMLStreamReader xmlStreamReaderWithoutCaching = element.getXMLStreamReaderWithoutCaching();
            streamingOMSerializer.serialize(xmlStreamReaderWithoutCaching,
                    omOutput);
        }
    }
}
