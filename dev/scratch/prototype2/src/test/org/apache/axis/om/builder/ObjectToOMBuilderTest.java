package org.apache.axis.om.builder;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.axis.impl.llom.builder.ObjectToOMBuilder;
import org.apache.axis.impl.llom.util.StreamWriterToContentHandlerConverter;
import org.apache.axis.impl.llom.serialize.SimpleObjectOMSerializer;
import org.apache.axis.om.*;
import org.apache.axis.om.builder.dummy.DummyOutObject;
import org.xml.sax.ContentHandler;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

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

    private OutObject outObject;
    private ObjectToOMBuilder objectToOMBuilder;
    private OMFactory omFactory;
    private OMElement element;
    private SimpleObjectOMSerializer serializer;

    protected void setUp() throws Exception {
        super.setUp();
        outObject = new DummyOutObject();
        omFactory = OMFactory.newInstance();

        OMNamespace ns = omFactory.createOMNamespace(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI, OMConstants.SOAPENVELOPE_NAMESPACE_PREFIX);

        element = omFactory.createOMElement("Body", ns);
        objectToOMBuilder = new ObjectToOMBuilder(element, outObject);
        omFactory.createOMElement(null, null, element, objectToOMBuilder);
        serializer = new SimpleObjectOMSerializer();
    }

    public void testBuilding() {
        objectToOMBuilder.next();
        Iterator children = element.getChildren();
        while (children.hasNext()) {
            OMNode omNode = (OMNode) children.next();
            assertNotNull(omNode);
        }


    }

    public void testSerialization() throws Exception {
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
        serializer.serialize(element, writer, true);
    }

    public void testSerializationWithCacheOff() throws Exception {
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
        serializer.serialize(element, writer, false);
    }

}
