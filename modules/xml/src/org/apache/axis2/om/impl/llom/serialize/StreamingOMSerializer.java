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
package org.apache.axis2.om.impl.llom.serialize;

import org.apache.axis2.om.OMOutput;
import org.apache.axis2.om.OMSerializer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Class StreamingOMSerializer
 */
public class StreamingOMSerializer implements XMLStreamConstants, OMSerializer {

    /*
     * The behavior of the serializer is such that it returns when it encounters the
     * starting element for the second time. The depth variable tracks the depth of the
     * serilizer and tells it when to return.
     * Note that it is assumed that this serilization starts on an Element
     */

    /**
     * Field depth
     */
    private int depth = 0;

    /**
     * Method serializeWithCache
     *
     * @param obj
     * @param omOutput
     * @throws XMLStreamException
     */
    public void serialize(Object obj, OMOutput omOutput)
            throws XMLStreamException {
        if (!(obj instanceof XMLStreamReader)) {
            throw new UnsupportedOperationException(
                    "Unsupported input object. Must be of the the type XMLStreamReader");
        }
        XMLStreamReader node = (XMLStreamReader) obj;
        serializeNode(node, omOutput);
    }

    /**
     * Method serializeNode
     *
     * @param reader
     * @param omOutput
     * @throws XMLStreamException
     */
    protected void serializeNode(XMLStreamReader reader, OMOutput omOutput)
            throws XMLStreamException {
    	//TODO We get the StAXWriter at this point and uses it hereafter assuming that this is the only entry point to this class.
    	// If there can be other classes calling methodes of this we might need to change methode signatures to OMOutputer
    	XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == START_ELEMENT) {
                serializeElement(reader, writer);
                depth++;
            } else if (event == ATTRIBUTE) {
                serializeAttributes(reader, writer);
            } else if (event == CHARACTERS) {
                serializeText(reader, writer);
            } else if (event == COMMENT) {
                serializeComment(reader, writer);
            } else if (event == CDATA) {
                serializeCData(reader, writer);
            } else if (event == END_ELEMENT) {
                serializeEndElement(writer);
                depth--;
            } else if (event == END_DOCUMENT) {
                try {
                    serializeEndElement(writer);
                } catch (Exception e) {
                    //TODO: log exceptions
                }
            }
            if (depth == 0) {
                break;
            }
        }
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeElement(
            XMLStreamReader reader, XMLStreamWriter writer)
            throws XMLStreamException {
        String prefix = reader.getPrefix();
        String nameSpaceName = reader.getNamespaceURI();
        String writer_prefix = writer.getPrefix(nameSpaceName);
        if (nameSpaceName != null) {
            if (writer_prefix != null) {
                writer.writeStartElement(nameSpaceName, reader.getLocalName());
            } else {
                if (prefix != null) {
                    writer.writeStartElement(prefix, reader.getLocalName(),
                            nameSpaceName);
                    writer.writeNamespace(prefix, nameSpaceName);
                    writer.setPrefix(prefix, nameSpaceName);
                } else {
                    writer.writeStartElement(nameSpaceName,
                            reader.getLocalName());
                    writer.writeDefaultNamespace(nameSpaceName);
                    writer.setDefaultNamespace(nameSpaceName);
                }
            }
        } else {
//            throw new OMException(
//                    "Non namespace qualified elements are not allowed");
        }

        // add attributes
        serializeAttributes(reader, writer);

        // add the namespaces
        int count = reader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            serializeNamespace(reader.getNamespacePrefix(i),
                    reader.getNamespaceURI(i), writer);
        }
    }

    /**
     * Method serializeEndElement
     *
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeEndElement(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeEndElement();
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeText(XMLStreamReader reader, XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeCharacters(reader.getText());
    }

    /**
     * Method serializeCData
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeCData(
            XMLStreamReader reader, XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeCData(reader.getText());
    }

    /**
     * Method serializeComment
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeComment(
            XMLStreamReader reader, XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeComment(reader.getText());
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeAttributes(
            XMLStreamReader reader, XMLStreamWriter writer)
            throws XMLStreamException {
        int count = reader.getAttributeCount();
        String prefix = null;
        String namespaceName = null;
        for (int i = 0; i < count; i++) {
            prefix = reader.getAttributePrefix(i);
            namespaceName = reader.getAttributeNamespace(i);
            if ((prefix != null) && !namespaceName.equals("")) {
                writer.writeAttribute(prefix, namespaceName,
                        reader.getAttributeLocalName(i),
                        reader.getAttributeValue(i));
            } else {
                writer.writeAttribute(reader.getAttributeLocalName(i),
                        reader.getAttributeValue(i));
            }
        }
    }

    /**
     * Method serializeNamespace
     *
     * @param prefix
     * @param URI
     * @param writer
     * @throws XMLStreamException
     */
    private void serializeNamespace(
            String prefix, String URI, XMLStreamWriter writer)
            throws XMLStreamException {
        String prefix1 = writer.getPrefix(URI);
        if (prefix1 == null) {
            writer.writeNamespace(prefix, URI);
            writer.setPrefix(prefix, URI);
        }
    }
}
