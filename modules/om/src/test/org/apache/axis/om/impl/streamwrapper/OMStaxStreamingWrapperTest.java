package org.apache.axis.om.impl.streamwrapper;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.factory.OMXMLBuilderFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileReader;

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
public class OMStaxStreamingWrapperTest extends AbstractTestCase {

    private SOAPEnvelope envelope = null;
    private File tempFile;
    private XMLStreamReader parser;

    public OMStaxStreamingWrapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage1.xml")));
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory.newInstance(), xmlStreamReader);

        envelope = (SOAPEnvelope) builder.getDocumentElement();

        tempFile = File.createTempFile("temp", "xml");

    }


//    public void testWrapperFullOM() throws Exception {
//        assertNotNull(envelope);
//        //this serializing will cause the OM to fully build!
//        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream(tempFile));
//        envelope.serialize(writer,true);
//        parser = envelope.getPullParser(false);
//        while (parser.hasNext()) {
//            int event = parser.next();
//            assertTrue(event > 0);
//        }
//
//    }

    public void testWrapperHalfOM() throws Exception {
        assertNotNull(envelope);
        parser = envelope.getPullParser(false);
        while (parser.hasNext()) {
            int event = parser.next();
            assertTrue(event > 0);
        }
    }

//    public void testWrapperHalfOMWithCacheOff() throws Exception {
//        assertNotNull(envelope);
//        parser = envelope.getPullParser(true);
//        while (parser.hasNext()) {
//            int event = parser.next();
//            assertTrue(event > 0);
//        }
//    }
//
//    public void testWrapperElementEventGenerationWithHalfOMWithCacheOff() throws XMLStreamException {
//        assertNotNull(envelope);
//        parser = envelope.getPullParser(true);
//        while (parser.hasNext()) {
//            int event = parser.next();
//            assertTrue(event > 0);
//            if (event == XMLStreamConstants.START_ELEMENT) {
//                checkStartElement(parser);
//            } else if (event == XMLStreamConstants.CHARACTERS) {
//                checkCharacters(parser);
//            }
//        }
//
//
//    }
//
//    public void testWrapperElementEventGenerationWithHalfOM() throws Exception {
//        assertNotNull(envelope);
//        parser = envelope.getPullParser(false);
//        while (parser.hasNext()) {
//            int event = parser.next();
//            assertTrue(event > 0);
//            if (event == XMLStreamConstants.START_ELEMENT) {
//                checkStartElement(parser);
//            } else if (event == XMLStreamConstants.CHARACTERS) {
//                checkCharacters(parser);
//            }
//        }
//
//
//    }
//
//    private void checkCharacters(XMLStreamReader wrapper) {
//        assertFalse(wrapper.isStartElement());
//        assertFalse(wrapper.isEndElement());
//        assertFalse(wrapper.isWhiteSpace());
//        assertFalse(wrapper.hasName());
//        assertTrue(wrapper.isCharacters());
//
//        assertNotNull(wrapper.getText());
//        assertTrue(wrapper.getTextLength() > 0);
//    }
//
//    private void checkStartElement(XMLStreamReader wrapper) {
//        assertTrue(wrapper.isStartElement());
//        assertTrue(wrapper.hasName());
//        assertFalse(wrapper.isEndElement());
//        assertFalse(wrapper.isCharacters());
//        assertFalse(wrapper.isWhiteSpace());
//
//        //at the start element event these need to be supplied
//        assertNotNull(wrapper.getLocalName());
//        assertNotNull(wrapper.getName());
//        assertNotNull(wrapper.getNamespaceURI());
//        //prefix may be null
//        wrapper.getPrefix();
//
//        //todo add the other checks here
//        int attribCount = wrapper.getAttributeCount();
//        for (int i = 0; i < attribCount; i++) {
//            assertNotNull(wrapper.getAttributeLocalName(i));
//            assertNotNull(wrapper.getAttributeValue(i));
//            assertNotNull(wrapper.getAttributeName(i));
//            wrapper.getAttributePrefix(i);
//            wrapper.getAttributeNamespace(i);
//            //todo add the other checks here
//        }
//
//
//    }
//
//    public void testWrapperElementEventGenerationWithHalfOMWithCacheOff() throws XMLStreamException {
//        assertNotNull(envelope);
//        parser = envelope.getPullParser(true);
//        while (parser.hasNext()) {
//            int event = parser.next();
//            assertTrue(event > 0);
//            if (event == XMLStreamConstants.START_ELEMENT) {
//                checkStartElement(parser);
//            } else if (event == XMLStreamConstants.CHARACTERS) {
//                checkCharacters(parser);
//            }
//        }
//
//
//    }
//
//    public void testWrapperElementEventGenerationWithHalfOM() throws Exception {
//        assertNotNull(envelope);
//        parser = envelope.getPullParser(false);
//        while (parser.hasNext()) {
//            int event = parser.next();
//            assertTrue(event > 0);
//            if (event == XMLStreamConstants.START_ELEMENT) {
//                checkStartElement(parser);
//            } else if (event == XMLStreamConstants.CHARACTERS) {
//                checkCharacters(parser);
//            }
//        }
//
//
//    }
//
//    private void checkCharacters(XMLStreamReader wrapper) {
//        assertFalse(wrapper.isStartElement());
//        assertFalse(wrapper.isEndElement());
//        assertFalse(wrapper.isWhiteSpace());
//        assertFalse(wrapper.hasName());
//        assertTrue(wrapper.isCharacters());
//
//        assertNotNull(wrapper.getText());
//        assertTrue(wrapper.getTextLength() > 0);
//    }
//
//    private void checkStartElement(XMLStreamReader wrapper) {
//        assertTrue(wrapper.isStartElement());
//        assertTrue(wrapper.hasName());
//        assertFalse(wrapper.isEndElement());
//        assertFalse(wrapper.isCharacters());
//        assertFalse(wrapper.isWhiteSpace());
//
//        //at the start element event these need to be supplied
//        assertNotNull(wrapper.getLocalName());
//        assertNotNull(wrapper.getName());
//        assertNotNull(wrapper.getNamespaceURI());
//        //prefix may be null
//        wrapper.getPrefix();
//
//        //todo add the other checks here
//        int attribCount = wrapper.getAttributeCount();
//        for (int i = 0; i < attribCount; i++) {
//            assertNotNull(wrapper.getAttributeLocalName(i));
//            assertNotNull(wrapper.getAttributeValue(i));
//            assertNotNull(wrapper.getAttributeName(i));
//            wrapper.getAttributePrefix(i);
//            wrapper.getAttributeNamespace(i);
//            //todo add the other checks here
//        }
//
//
//    }


    protected void tearDown() throws Exception {
        tempFile.delete();
    }
}
