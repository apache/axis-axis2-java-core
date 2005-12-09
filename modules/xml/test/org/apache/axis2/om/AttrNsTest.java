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

package org.apache.axis2.om;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;

import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;

public class AttrNsTest extends AbstractOMSerializationTest {

    private String attrNamespaceTestXML = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<foo xmlns:a=\"http://opensource.lk\">" +
            "    <bar1 b:attr=\"test attr value1\" xmlns:b=\"http://opensource.lk/ns1\">test1</bar1>" +
            "    <bar2 b:attr=\"test attr value2\" xmlns:b=\"http://opensource.lk/ns1\">test2</bar2>" +
            "</foo>";

    public void testAttributeNamespaces() throws Exception {
        ignoreXMLDeclaration = true;
        ignoreDocument = true;

        Document document1 = newDocument(attrNamespaceTestXML);
        String serializedOM = getSerializedOM(attrNamespaceTestXML);
        Document document2 = newDocument(serializedOM);

        Diff diff = compareXML(document1, document2);
        assertXMLEqual(diff, true);
    }
    

    /**
     * Test method to test the XML namespace
     * @throws Exception
     */
	public void testAttr() throws Exception{
		String xml = "<wsp:Policy xml:base=\"uri:thisBase\" " +
			"xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">" + 
		"</wsp:Policy>";
		
		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
		StAXOMBuilder builder = new StAXOMBuilder(bais);
		OMElement elem = builder.getDocumentElement();
		elem.build();
		assertEquals("Attribute value mismatch", "uri:thisBase", elem.getAttributeValue(new QName(OMConstants.XMLNS_URI,"base")));
		
		OMAttribute attr = elem.getAttribute(new QName(OMConstants.XMLNS_URI,"base"));
		
		assertEquals("Attribute namespace mismatch", OMConstants.XMLNS_URI, attr.getNamespace().getName());
	}


}
