package org.apache.ws.commons.om;

import org.custommonkey.xmlunit.XMLTestCase;

import javax.xml.namespace.QName;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 *
 */

public class DefaultNamespaceTest extends XMLTestCase {

    public void testDefaultNamespace() throws Exception {

        String expectedXML = "<Foo xmlns=\"http://defaultNsUri.org\"><Bar xmlns=\"\"></Bar><Baz></Baz></Foo>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement foo = factory.createOMElement(new QName("http://defaultNsUri.org", "Foo"), null);
        factory.createOMElement("Bar", null, foo);
        factory.createOMElement(new QName("http://defaultNsUri.org", "Baz"), foo);
        assertXMLEqual(expectedXML, foo.toString());
    }

    public void test() {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        String nsURI = "http://test.org";
        String nsPrefix = "testPrefix";
        OMElement element = factory.createOMElement("DocElement", null);

        OMElement foo = factory.createOMElement(new QName(nsURI, "Foo", nsPrefix), element);
        factory.createOMElement(new QName(nsURI+1, "Bar", nsPrefix), element);

        factory.createOMElement(new QName(nsURI+2, "Baz", nsPrefix), foo);
        factory.createOMElement(new QName(nsURI, "Baz", nsPrefix), foo);

    }

}
