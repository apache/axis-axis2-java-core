package org.apache.axis.om.builder.dummy;

import org.apache.axis.om.OutObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.FileReader;
import java.io.IOException;

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
public class DummyOutObject implements OutObject {
    private XMLReader parser;
    String fileName = "src/test-resources/soapmessage.xml";


    public DummyOutObject() {
        setup();
    }

    private void setup() {
        try {
            parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
            System.out.println(System.getProperty("org.xml.sax.driver"));

        } catch (SAXException e) {
            e.printStackTrace();  //TODO implement this
        }

    }

    public void setContentHandler(ContentHandler contentHandler) {
        parser.setContentHandler(contentHandler);

    }

    public ContentHandler getContentHandler() {
        return parser.getContentHandler();
    }

    public void startBuilding() {
        try {
            parser.parse(new InputSource(new FileReader(fileName)));
        } catch (IOException e) {
            e.printStackTrace();  //TODO implement this
        } catch (SAXException e) {
            e.printStackTrace();  //TODO implement this
        }

    }
}
