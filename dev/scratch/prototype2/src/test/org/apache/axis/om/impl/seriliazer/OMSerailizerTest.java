package org.apache.axis.om.impl.seriliazer;

import org.apache.axis.impl.llom.serialize.StreamingOMSerializer;
import org.apache.axis.impl.llom.serialize.SimpleOMSerializer;
import org.apache.axis.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.AbstractTestCase;
import org.apache.axis.om.*;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import java.io.FileReader;
import java.io.File;
import java.io.FileOutputStream;

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
public class OMSerailizerTest extends AbstractTestCase {

    private XMLStreamReader reader;
    private XMLStreamWriter writer;
    private File tempFile;

    public OMSerailizerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        reader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage.xml")));
        tempFile = File.createTempFile("temp", "xml");
        writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(new FileOutputStream(tempFile));
//        writer = XMLOutputFactory.newInstance().
//                createXMLStreamWriter(System.out);
    }

    public void testRawSerializer() throws Exception {
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        serializer.serialize(reader, writer);
    }

    public void testElementPullStream1() throws Exception {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory.newInstance(),
                reader);
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        serializer.serialize(env.getPullParser(false), writer);
    }

    public void testElementPullStream2() throws Exception {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory.newInstance(),
                reader);
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        SOAPBody body = env.getBody();
        StreamingOMSerializer serializer = new StreamingOMSerializer();

        serializer.serialize(body.getPullParser(false), writer);
    }

    public void testElement() throws Exception {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory.newInstance(),
                reader);
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        SOAPBody body = env.getBody();
        SimpleOMSerializer serializer = new SimpleOMSerializer();

        serializer.serialize(body, writer);
    }

    public void testCompleteElement() throws Exception {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory.newInstance(),
                reader);
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        SimpleOMSerializer serializer = new SimpleOMSerializer();
        serializer.serialize(env, writer);
    }

    public void testDualNamespaces1() throws Exception {
        OMFactory factory = OMFactory.newInstance();
        OMNamespace ns1 = factory.createOMNamespace("bar","x");
        OMNamespace ns2 = factory.createOMNamespace("bar","y");

        OMElement root = factory.createOMElement("root",ns1);
        OMElement elt11 = factory.createOMElement("foo1",ns1);
        OMElement elt12 = factory.createOMElement("foo2",ns1);
        OMElement elt21 = factory.createOMElement("yuck",ns2);
        OMElement elt22 = factory.createOMElement("yuck",ns2);

//        OMText txt1 = factory.createText(elt2,"blah");

        elt11.addChild(elt21);
        elt12.addChild(elt22);
        root.addChild(elt11);
        root.addChild(elt12);


        SimpleOMSerializer serializer = new SimpleOMSerializer();
        serializer.serialize(root, writer);
    }

    public void testDualNamespaces2() throws Exception {
        OMFactory factory = OMFactory.newInstance();

        OMNamespace ns1 = factory.createOMNamespace("bar","x");
        OMElement root = factory.createOMElement("root",ns1);

        OMNamespace ns2 = root.declareNamespace("bar","y");

        OMElement elt1 = factory.createOMElement("foo",ns1);
        OMElement elt2 = factory.createOMElement("yuck",ns2);


        OMText txt1 = factory.createText(elt2,"blah");

        elt2.addChild(txt1);
        elt1.addChild(elt2);
        root.addChild(elt1);

        SimpleOMSerializer serializer = new SimpleOMSerializer();
        serializer.serialize(root, writer);
    }
    protected void tearDown() throws Exception {
        tempFile.delete();
    }
}
