package org.apache.axis.impl.llom.serialize;

import org.apache.axis.om.OMSerializer;
import org.apache.axis.om.OMException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
//import java.util.Stack;


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
 */
public class StreamingOMSerializer implements XMLStreamConstants, OMSerializer {

//    private Stack namespacePrefixStack = new Stack();
//    private Stack namespaceCountStack = new Stack();

//    public Stack getNamespacePrefixStack() {
//        return namespacePrefixStack;
//    }
//
//    public void setNamespacePrefixStack(Stack namespacePrefixStack) {
//        if (namespacePrefixStack != null)
//            this.namespacePrefixStack = namespacePrefixStack;
//    }

    public void serialize(Object obj, XMLStreamWriter writer) throws XMLStreamException {
        if (!(obj instanceof XMLStreamReader)) {
            throw new UnsupportedOperationException("Unsupported input object. Must be of the the type XMLStreamReader");
        }

        XMLStreamReader node = (XMLStreamReader) obj;
        serializeNode(node, writer);
    }


    protected void serializeNode(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == START_ELEMENT) {
                serializeElement(reader, writer);
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
            } else if (event == END_DOCUMENT) {
                try {
                    serializeEndElement(writer);
                } catch (Exception e) {
                    //this is eaten
                }
            }
        }
    }

    /**

     */
    protected void serializeElement(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {

        String prefix = reader.getPrefix();
        String nameSpaceName = reader.getNamespaceURI();
        String writer_prefix = writer.getPrefix(nameSpaceName);

        if (nameSpaceName != null) {
            if (writer_prefix!=null){
                writer.writeStartElement(nameSpaceName, reader.getLocalName());
            }else{
                if (prefix!=null){
                        writer.writeStartElement(prefix, reader.getLocalName(),nameSpaceName);
                        writer.writeNamespace(prefix, nameSpaceName);
                        writer.setPrefix(prefix,nameSpaceName);
                    }else{
                        writer.writeStartElement(nameSpaceName,reader.getLocalName());
                        writer.writeDefaultNamespace(nameSpaceName);
                        writer.setDefaultNamespace(nameSpaceName);
                    }
            }
        } else {
            throw new OMException("Non namespace qualified elements are not allowed");
        }

        //add attributes
        serializeAttributes(reader, writer);
        //add the namespaces
        int count = reader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            serializeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i), writer);
        }

        //namespaceCountStack.push(new Integer(nsPushCount));

    }

    protected void serializeEndElement(XMLStreamWriter writer) throws XMLStreamException {
//        if (!namespaceCountStack.isEmpty()) {
//            Integer removeCount = (Integer) namespaceCountStack.pop();
//            int count = removeCount.intValue();
//            for (int i = 0; i < count; i++)
//                namespacePrefixStack.pop();
//        }
        writer.writeEndElement();

    }

    /**
     */
    protected void serializeText(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters(reader.getText());
    }

    protected void serializeCData(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCData(reader.getText());
    }


    protected void serializeComment(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeComment(reader.getText());
    }

    /**
     * @param writer
     * @throws XMLStreamException
     */


    protected void serializeAttributes(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {

        int count = reader.getAttributeCount();
        String prefix = null;
        String namespaceName = null;
        for (int i = 0; i < count; i++) {
            prefix = reader.getAttributePrefix(i);
            namespaceName = reader.getAttributeNamespace(i);
            if (prefix != null && !namespaceName.equals("")) {
                writer.writeAttribute(prefix,
                        namespaceName,
                        reader.getAttributeLocalName(i),
                        reader.getAttributeValue(i));
            } else {
                writer.writeAttribute(reader.getAttributeLocalName(i),
                        reader.getAttributeValue(i));
            }
        }
    }

    private void serializeNamespace(String prefix, String URI, XMLStreamWriter writer) throws XMLStreamException {
        String prefix1 = writer.getPrefix(URI);
        if (prefix1==null) {
            writer.writeNamespace(prefix, URI);
            writer.setPrefix(prefix,URI);
        }

    }

}
