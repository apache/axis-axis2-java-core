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
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.ws.commons.om.impl.llom.serialize.StreamingOMSerializer;
import org.apache.ws.commons.soap.SOAPBody;
import org.apache.ws.commons.soap.SOAPEnvelope;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileReader;

public class OMSerializerTest extends AbstractTestCase {
    private XMLStreamReader reader;
    private XMLStreamWriter writer;
    private File tempFile;

    public OMSerializerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        reader =
                XMLInputFactory.newInstance().
                        createXMLStreamReader(
                                new FileReader(
                                        getTestResourceFile("soap/soapmessage.xml")));
        tempFile = File.createTempFile("temp", "xml");
//        writer =
//                XMLOutputFactory.newInstance().
//                        createXMLStreamWriter(new FileOutputStream(tempFile));
        writer =
                XMLOutputFactory.newInstance().
                        createXMLStreamWriter(System.out);
    }

    public void testRawSerializer() throws Exception {
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        //serializer.setNamespacePrefixStack(new Stack());
        serializer.serialize(reader, writer);
        writer.flush();


    }

    public void testElementPullStream1() throws Exception {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                OMAbstractFactory.getSOAP11Factory(),
                reader);
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        serializer.serialize(env.getXMLStreamReaderWithoutCaching(), writer);
        writer.flush();
    }

    public void testElementPullStream1WithCacheOff() throws Exception {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                OMAbstractFactory.getSOAP11Factory(),
                reader);

        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        env.serializeAndConsume(writer);
        writer.flush();

        //now we should not be able to serilaize anything ! this should throw
        //an error
        try {
           env.serializeAndConsume(writer);
           fail();
        } catch (XMLStreamException e) {
           assertTrue(true);
        } catch (Exception e) {
           assertTrue(true);
        }
    }

    public void testElementPullStream2() throws Exception {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                OMAbstractFactory.getSOAP11Factory(),
                reader);
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        SOAPBody body = env.getBody();
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        serializer.serialize(body.getXMLStreamReaderWithoutCaching(),
                writer);
        writer.flush();
    }

    protected void tearDown() throws Exception {
        tempFile.delete();
    }
}
