package org.apache.axis.om.impl.streamwrapper;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.impl.factory.OMLinkedListImplFactory;
import org.apache.axis.om.impl.serialize.SimpleOMSerializer;
import org.apache.axis.om.soap.SOAPMessage;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 *
 * @author Axis team
 * Date: Nov 19, 2004
 * Time: 5:23:01 PM
 * 
 */
public class OMStaxStreamingWrapperTest extends AbstractTestCase {

    private SOAPMessage document = null;
    private SimpleOMSerializer serilizer;
    private OMStAXBuilder omStAXBuilder;
    private File tempFile;

    public OMStaxStreamingWrapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage1.xml")));
        OMFactory factory = new OMLinkedListImplFactory();
        omStAXBuilder = new OMStAXBuilder(factory, xmlStreamReader);
        document = omStAXBuilder.getSOAPMessage();
        serilizer = new SimpleOMSerializer();

        tempFile = File.createTempFile("temp","xml");
    }


    public void testWrapperFullOM() {
        assertNotNull(document);
        //this serializing will cause the OM to fully build!
        try {
            serilizer.serialize(document.getEnvelope(), new FileOutputStream(tempFile));
        } catch (FileNotFoundException e) {
            assertFalse(true);
        }

        //now the OM is fully created. Create the wrapper and see
        OMStAXWrapper wrapper = new OMStAXWrapper(omStAXBuilder, document.getEnvelope());

        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event>0);
        }

    }

    public void testWrapperHalfOM() {
        assertNotNull(document);

        //now the OM is not fully created. Create the wrapper and see
        OMStAXWrapper wrapper = new OMStAXWrapper(omStAXBuilder, document.getEnvelope());

        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event>0);
            //System.out.println("returnEvent = " + getEventString(event));
        }

    }
    public void testWrapperHalfOMWithCacheOff() {
        assertNotNull(document);

        //now the OM is not fully created. Create the wrapper and see
        OMStAXWrapper wrapper = new OMStAXWrapper(omStAXBuilder, document.getEnvelope());
        //set the switching allowed flag
        wrapper.setAllowSwitching(true);
        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event>0);
            //System.out.println("returnEvent = " + getEventString(event));
        }
    }

    protected void tearDown() throws Exception {
        tempFile.delete();
    }
}
