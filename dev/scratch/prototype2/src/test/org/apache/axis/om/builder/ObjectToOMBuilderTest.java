package org.apache.axis.om.builder;

import junit.framework.TestCase;
import org.apache.axis.om.*;
import org.apache.axis.om.builder.dummy.DummyOutObject;
import org.apache.axis.testUtils.Encoder;
import org.apache.axis.testUtils.ObjectToOMBuilder;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;

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
 * Time: 3:54:03 PM
 */
public class ObjectToOMBuilderTest extends TestCase {

    private Encoder outObject;


    private OMElement element;
    private XMLStreamWriter writer;
    private File tempFile;

    protected void setUp() throws Exception {
        super.setUp();
        Encoder outObject = new DummyOutObject();
        OMFactory omFactory = OMFactory.newInstance();
        OMNamespace ns = omFactory.createOMNamespace(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI, OMConstants.SOAPENVELOPE_NAMESPACE_PREFIX);
        element = omFactory.createOMElement("Body", ns);
        new ObjectToOMBuilder(element, outObject);
        tempFile = File.createTempFile("temp", "xml");
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream(tempFile));

    }

    public void testBuilding() {
        Iterator children = element.getChildren();
        while (children.hasNext()) {
            OMNode omNode = (OMNode) children.next();
            assertNotNull(omNode);
        }
    }

    public void testSerialization() throws Exception {
        element.serialize(writer, true);
    }


    protected void tearDown() throws Exception {
        tempFile.delete();
    }

}
