package org.apache.axis.impl.llom.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
public class StreamWriterToContentHandlerConverter implements ContentHandler {
    private XMLStreamWriter writer;

    public StreamWriterToContentHandlerConverter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void endDocument() throws SAXException {
        //do nothing
    }

    public void startDocument() throws SAXException {
        //          
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        try {
            writer.writeCharacters(ch, start, length);
            writer.flush();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        //throw new UnsupportedOperationException();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        //throw new UnsupportedOperationException();
    }

    public void skippedEntity(String name) throws SAXException {
        //throw new UnsupportedOperationException();
    }

    public void setDocumentLocator(Locator locator) {
        //throw new UnsupportedOperationException();
    }

    public void processingInstruction(String target, String data) throws SAXException {
        // throw new UnsupportedOperationException();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        //throw new UnsupportedOperationException();
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    private simpleQnameKeeper breakUpSaxQname(String qName){
        simpleQnameKeeper qNk=new simpleQnameKeeper();
        if (qName!=null){
            String[] text = qName.split(":");
            if (text.length>1){
                qNk.setPrefix(text[0]);
                qNk.setLocalName(text[1]);
            }else{
                qNk.setLocalName(text[0]);
            }
        }
        return qNk;
    }
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            simpleQnameKeeper qname = breakUpSaxQname(qName);

            if (qname.getPrefix()==null)
                writer.writeStartElement(namespaceURI,qname.getLocalName());
            else
                writer.writeStartElement(qname.getPrefix(), qname.getLocalName(),namespaceURI);

            int attCount = atts.getLength();
            for (int i = 0; i < attCount; i++) {
                qname = breakUpSaxQname(atts.getQName(i));
                writer.writeAttribute(atts.getURI(i),
                        qname.getLocalName(),
                        atts.getValue(i));
            }
            writer.flush();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    private class simpleQnameKeeper{
        private String prefix=null;
        private String localName=null;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getLocalName() {
            return localName;
        }

        public void setLocalName(String localName) {
            this.localName = localName;
        }

    }
}
