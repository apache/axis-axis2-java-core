package org.apache.axis.impl.llom.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

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
    private Log log = LogFactory.getLog(getClass());
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
         try {
            writer.writeNamespace(prefix,uri);
            writer.setPrefix(prefix,uri);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    private String getPrefix(String qName){
        if (qName!=null){
            return qName.substring(0,qName.indexOf(":"));
        }
        return null;
    }
    
    
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            log.info("writing element {"+namespaceURI+'}'+ localName +" directly to stream ");
            
            String prefix = getPrefix(qName);
            //it is only the prefix we want to learn from the QName! so we can get rid of the 
            //spliting QName
            if (prefix ==null){
                writer.writeStartElement(namespaceURI,localName);            
            }else{
                writer.writeStartElement(prefix,localName,namespaceURI);            
            }
            if (atts!=null){
                int attCount = atts.getLength();
                for (int i = 0; i < attCount; i++) {
                    
                    writer.writeAttribute(atts.getURI(i),
                            localName,
                            atts.getValue(i));
                }
            }
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

}
