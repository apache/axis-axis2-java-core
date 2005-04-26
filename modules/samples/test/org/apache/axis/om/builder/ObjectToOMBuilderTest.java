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
 
package org.apache.axis.om.builder;

import junit.framework.TestCase;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.impl.llom.SOAPConstants;
import org.apache.axis.om.impl.llom.soap11.SOAP11Constants;
import org.apache.axis.om.builder.dummy.DummyOutObject;
import org.apache.axis.testUtils.Encoder;
import org.apache.axis.testUtils.ObjectToOMBuilder;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;

public class ObjectToOMBuilderTest extends TestCase {

    private Encoder outObject;


    private OMElement element;
    private XMLStreamWriter writer;
    private File tempFile;

    protected void setUp() throws Exception {
        super.setUp();
        Encoder outObject = new DummyOutObject();
        OMFactory omFactory = OMFactory.newInstance();
        OMNamespace ns = omFactory.createOMNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX);
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
        element.serializeWithCache(writer);
    }


    protected void tearDown() throws Exception {
        tempFile.delete();
    }

}
