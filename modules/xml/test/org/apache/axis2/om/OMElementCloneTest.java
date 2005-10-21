package org.apache.axis2.om;

import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class OMElementCloneTest extends XMLTestCase {

    File dir = new File("test-resources", "soap");

    public void testElementCloning() throws Exception {
        SOAPEnvelope soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        new File(dir, "soapmessage.xml"))
                        .getDocumentElement();
        SOAPBody body = soapEnvelope.getBody();

        OMElement firstClonedBodyElement = new StAXOMBuilder(body.getXMLStreamReader()).getDocumentElement();
        OMElement secondClonedBodyElement = new StAXOMBuilder(body.getXMLStreamReader()).getDocumentElement();

        // first check whether both have the same information
        assertXMLEqual(newDocument(body.toString()), newDocument(firstClonedBodyElement.toString()));
        assertXMLEqual(newDocument(body.toString()), newDocument(secondClonedBodyElement.toString()));
        assertXMLEqual(newDocument(firstClonedBodyElement.toString()), newDocument(secondClonedBodyElement.toString()));

        // lets check some links. They must not be equal
        assertNotSame(body.getParent(), firstClonedBodyElement.getParent());
        assertNotSame(body.getParent(), secondClonedBodyElement.getParent());
        assertNotSame(firstClonedBodyElement.getParent(), secondClonedBodyElement.getParent());

    }

    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
