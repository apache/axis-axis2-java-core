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

package org.apache.ws.commons.om.impl.serializer;

import org.apache.ws.commons.om.AbstractTestCase;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.ws.commons.soap.SOAPBody;
import org.apache.ws.commons.soap.SOAPEnvelope;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class ElementSerializerTest extends AbstractTestCase {
    private XMLStreamReader reader;
    private XMLStreamWriter writer;
    private OMXMLParserWrapper builder;
    private File tempFile;

    public ElementSerializerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        reader =
                XMLInputFactory.newInstance().
                createXMLStreamReader(
                        new FileReader(
                                getTestResourceFile("soap/soapmessage.xml")));
        tempFile = File.createTempFile("temp", "xml");
        writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(new FileOutputStream(tempFile));
        builder =
                OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                        OMAbstractFactory.getSOAP11Factory(), reader);
    }

    public void testElementSerilization() throws Exception {
        OMElement elt = builder.getDocumentElement();
        elt.serialize(writer);

    }

    public void testElementSerilizationCacheOff() throws Exception {
        OMElement elt = builder.getDocumentElement();
        elt.serialize(writer);

    }

    public void testElementSerilizationChild() throws Exception {
        OMElement elt = builder.getDocumentElement();
        OMNode node = elt.getFirstOMChild().getNextOMSibling();
        node.serialize(writer);

    }

    public void testElementSerilizationSOAPBodyCacheOff() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        OMNode node = env.getBody();
        node.serialize(writer);
    }

    public void testElement() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        SOAPBody body = env.getBody();
        body.serialize(writer);
    }

    public void testCompleteElement() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        env.serialize(writer);
    }

    public void testDualNamespaces1() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = factory.createOMNamespace("bar", "x");
        OMNamespace ns2 = factory.createOMNamespace("bar", "y");
        OMElement root = factory.createOMElement("root", ns1);
        OMElement elt11 = factory.createOMElement("foo1", ns1);
        OMElement elt12 = factory.createOMElement("foo2", ns1);
        OMElement elt21 = factory.createOMElement("yuck", ns2);
        OMElement elt22 = factory.createOMElement("yuck", ns2);
        elt11.addChild(elt21);
        elt12.addChild(elt22);
        root.addChild(elt11);
        root.addChild(elt12);
        root.serialize(writer);
    }

    public void testDualNamespaces2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = factory.createOMNamespace("bar", "x");
        OMElement root = factory.createOMElement("root", ns1);
        OMNamespace ns2 = root.declareNamespace("bar", "y");
        OMElement elt1 = factory.createOMElement("foo", ns1);
        OMElement elt2 = factory.createOMElement("yuck", ns2);
        OMText txt1 = factory.createText(elt2, "blah");
        elt2.addChild(txt1);
        elt1.addChild(elt2);
        root.addChild(elt1);
        root.serialize(writer);
    }

    protected void tearDown() throws Exception {
        tempFile.delete();
    }
}
