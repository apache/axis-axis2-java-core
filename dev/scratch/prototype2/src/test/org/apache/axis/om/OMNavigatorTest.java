package org.apache.axis.om;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.OMNavigator;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
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
 */
public class OMNavigatorTest extends AbstractTestCase {

    private SOAPEnvelope envelope = null;
    private OMXMLParserWrapper builder;
    private File tempFile;
    private XMLStreamWriter writer;

    public OMNavigatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage1.xml")));
        OMFactory factory = OMFactory.newInstance();
        builder = new StAXSOAPModelBuilder(factory, xmlStreamReader);
        envelope = (SOAPEnvelope)builder.getDocumentElement();
        tempFile = File.createTempFile("temp", "xml");
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream(tempFile));


    }


    public void testnavigatorFullyBuilt() throws Exception {

        assertNotNull(envelope);
        //dump the out put to a  temporary file
        envelope.serialize(writer,true);

        //now the OM is fully created test the navigation
        OMNavigator navigator = new OMNavigator(envelope);
        OMNode node = null;


        while (navigator.isNavigable()) {
            node = navigator.next();
            assertNotNull(node);
        }
    }

    public void testnavigatorHalfBuilt() {
        assertNotNull(envelope);
        //now the OM is not fully created. Try to navigate it
        OMNavigator navigator = new OMNavigator(envelope);
        OMNode node = null;

        while (navigator.isNavigable()) {
            node = navigator.next();
            assertNotNull(node);
        }
    }

    public void testnavigatorHalfBuiltStep() {
        assertNotNull(envelope);

        //now the OM is not fully created
        OMNavigator navigator = new OMNavigator(envelope);
        OMNode node = null;

        while (!navigator.isCompleted()) {
            if (navigator.isNavigable()) {
                node = navigator.next();
            } else {
                builder.next();
                navigator.step();
                node = navigator.next();
            }
            assertNotNull(node);

        }

    }

    protected void tearDown() throws Exception {
        tempFile.delete();
    }

}
