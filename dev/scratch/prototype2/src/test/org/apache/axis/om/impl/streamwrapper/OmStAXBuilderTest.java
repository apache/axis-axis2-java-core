package org.apache.axis.om.impl.streamwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.serialize.SimpleOMSerializer;
import org.apache.axis.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.OMXMLParserWrapper;

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
 * Date: Nov 18, 2004
 * Time: 3:54:54 PM
 * 
 */
public class OmStAXBuilderTest extends AbstractTestCase{

    private OMFactory factory =null;
    private OMXMLParserWrapper builder;
    private SimpleOMSerializer serilizer;
    private File tempFile;

    public OmStAXBuilderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        factory = OMFactory.newInstance();
        XMLStreamReader reader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage1.xml")));
        builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(factory,reader);
        serilizer = new SimpleOMSerializer();

        tempFile = File.createTempFile("temp", "xml");
    }

    public void testStaxBuilder()throws Exception{

        SOAPEnvelope envelope = (SOAPEnvelope)builder.getDocumentElement();
        assertNotNull(envelope);
        serilizer.serialize(envelope,new FileOutputStream(tempFile));


    }

    protected void tearDown() throws Exception {
        tempFile.delete();
    }


}
