package org.apache.axis.om.builder.dummy;

import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.encoding.Encoder;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 19, 2004
 * Time: 3:56:08 PM
 */
public class DummyOutObject implements Encoder {
    private XMLReader parser;
    String fileName = "src/test-resources/soapmessage.xml";


    public DummyOutObject() throws SAXException, ParserConfigurationException {
        setup();
    }

    private void setup() throws SAXException, ParserConfigurationException {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            parser = saxParserFactory.newSAXParser().getXMLReader();
           

    }


    public void serialize(ContentHandler contentHandler) throws OMException{
        try {
            parser.parse(new InputSource(new FileReader(fileName)));
        } catch (Exception e) {
           throw new OMException(e);
        }

    }
    /* (non-Javadoc)
     * @see org.apache.axis.encoding.Encoder#deSerialize(javax.xml.stream.XMLStreamReader)
     */
    public Object deSerialize(XMLStreamReader xpp) throws AxisFault {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis.encoding.Encoder#setObject(java.lang.Object)
     */
    public void setObject(Object obj) {
        // TODO Auto-generated method stub

    }

}
