package org.apache.axis.om.impl.streamwrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.impl.llom.serialize.SimpleOMSerializer;
import org.apache.axis.impl.llom.wrapper.OMStAXWrapper;
import org.apache.axis.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.StreamingWrapper;

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
 *
 * @author Axis team
 *         Date: Nov 19, 2004
 *         Time: 5:23:01 PM
 */
public class OMStaxStreamingWrapperTest extends AbstractTestCase {

    private SOAPEnvelope envelope = null;
    private SimpleOMSerializer serilizer;
    private OMXMLParserWrapper builder;
    private File tempFile;

    public OMStaxStreamingWrapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage1.xml")));
        builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory.newInstance(), xmlStreamReader);

        envelope = (SOAPEnvelope) builder.getDocumentElement();
        serilizer = new SimpleOMSerializer();
        tempFile = File.createTempFile("temp", "xml");
    }


    public void testWrapperFullOM() throws FileNotFoundException {
        assertNotNull(envelope);
        //this serializing will cause the OM to fully build!
        serilizer.serialize(envelope, new FileOutputStream(tempFile));

        //now the OM is fully created. Create the wrapper and see
        StreamingWrapper wrapper = new OMStAXWrapper(builder, envelope);

        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event > 0);
        }

    }

    public void testWrapperHalfOM() {
        assertNotNull(envelope);

        //now the OM is not fully created. Create the wrapper and see
        StreamingWrapper wrapper = new OMStAXWrapper(builder, envelope);
        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event > 0);
        }
    }

    public void testWrapperHalfOMWithCacheOff() {
        assertNotNull(envelope);
        //now the OM is not fully created. Create the wrapper and see
        StreamingWrapper wrapper = new OMStAXWrapper(builder, envelope);
        //set the switching allowed flag
        wrapper.setAllowSwitching(true);
        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event > 0);
        }
    }

    public void testWrapperElementEventGenerationWithHalfOMWithCacheOff() {
        assertNotNull(envelope);
        StreamingWrapper wrapper = new OMStAXWrapper(builder, envelope);
        wrapper.setAllowSwitching(true);

        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event > 0);
            if (event == XMLStreamReader.START_ELEMENT) {
                checkStartElement(wrapper);
            }else if (event == XMLStreamReader.CHARACTERS){
                checkCharacters(wrapper);
            }
        }


    }

    public void testWrapperElementEventGenerationWithHalfOM() {
        assertNotNull(envelope);
        StreamingWrapper wrapper = new OMStAXWrapper(builder, envelope);

        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event > 0);
            if (event == XMLStreamReader.START_ELEMENT) {
                checkStartElement(wrapper);
            }else if (event == XMLStreamReader.CHARACTERS){
                checkCharacters(wrapper);
            }
        }


    }

    private void checkCharacters(StreamingWrapper wrapper){
        assertFalse(wrapper.isStartElement());
        assertFalse(wrapper.isEndElement());
        assertFalse(wrapper.isWhiteSpace());
        assertFalse(wrapper.hasName());
        assertTrue(wrapper.isCharacters());

        assertNotNull(wrapper.getText());
        assertTrue(wrapper.getTextLength()>0);
    }

    private void checkStartElement(StreamingWrapper wrapper) {
        assertTrue(wrapper.isStartElement());
        assertTrue(wrapper.hasName());
        assertFalse(wrapper.isEndElement());
        assertFalse(wrapper.isCharacters());
        assertFalse(wrapper.isWhiteSpace());

        //at the start element event these need to be supplied
        assertNotNull(wrapper.getLocalName());
        assertNotNull(wrapper.getName());
        assertNotNull(wrapper.getNamespaceURI());
        //prefix may be null
        wrapper.getPrefix();

        //todo add the other checks here
        int attribCount = wrapper.getAttributeCount();
        for (int i = 0; i < attribCount; i++) {
            assertNotNull(wrapper.getAttributeLocalName(i));
            assertNotNull(wrapper.getAttributeValue(i));
            assertNotNull(wrapper.getAttributeName(i));
            wrapper.getAttributePrefix(i);
            wrapper.getAttributeNamespace(i);
            //todo add the other checks here
        }


    }


    protected void tearDown() throws Exception {
        tempFile.delete();
    }
}
