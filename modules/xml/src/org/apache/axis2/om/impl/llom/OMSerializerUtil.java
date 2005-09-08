package org.apache.axis2.om.impl.llom;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.serialize.StreamingOMSerializer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

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
*
*
*/

public class OMSerializerUtil {

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
                        attr.getLocalName(), attr.getValue());
            } else {
                writer.writeAttribute(namespaceName, attr.getLocalName(),
                        attr.getValue());
            }
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getValue());
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
    public static void serializeStartpart(OMElementImpl element, OMOutputImpl omOutput)
            throws XMLStreamException {
        String nameSpaceName = null;
        String writer_prefix = null;
        String prefix = null;
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (element.ns != null) {
            nameSpaceName = element.ns.getName();
            writer_prefix = writer.getPrefix(nameSpaceName);
            prefix = element.ns.getPrefix();
            if (nameSpaceName != null) {
                if (writer_prefix != null) {
                    writer.writeStartElement(nameSpaceName,
                            element.getLocalName());
                } else {
                    if (prefix == null) {
                        prefix = "";
                    }
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
        }

        // add the namespaces
        serializeNamespaces(element, omOutput);

        // add the elements attributes
        serializeAttributes(element, omOutput);
    }

    public static void serializeNamespaces(OMElementImpl element,
                                           org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        Iterator namespaces = element.getAllDeclaredNamespaces();
        if (namespaces != null) {
            while (namespaces.hasNext()) {
                serializeNamespace((OMNamespace) namespaces.next(), omOutput);
            }
        }
    }

    public static void serializeAttributes(OMElementImpl element,
                                           org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        if (element.getAttributes() != null) {
            Iterator attributesList = element.getAttributes();
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
    public static void serializeNormal(OMElementImpl element, OMOutputImpl omOutput, boolean cache)
            throws XMLStreamException {

        if (cache) {
            element.build();
        }

        serializeStartpart(element, omOutput);
        OMNode firstChild = element.firstChild;
        if (firstChild != null) {
            if (cache) {
                firstChild.serializeWithCache(omOutput);
            } else {
                firstChild.serialize(omOutput);
            }
        }
        serializeEndpart(omOutput);
    }

    public static void serializeByPullStream(OMElementImpl element, org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        serializeByPullStream(element,omOutput,false);
    }

     public static void serializeByPullStream(OMElementImpl element, org.apache.axis2.om.impl.OMOutputImpl omOutput,boolean cache) throws XMLStreamException {
        StreamingOMSerializer streamingOMSerializer = new StreamingOMSerializer();
        if (cache){
               streamingOMSerializer.serialize(element.getXMLStreamReader(),
                omOutput);
        }else{
            streamingOMSerializer.serialize(element.getXMLStreamReaderWithoutCaching(),
                omOutput);
        }
    }
}
