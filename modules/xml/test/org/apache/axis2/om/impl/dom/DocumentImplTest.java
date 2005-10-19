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
package org.apache.axis2.om.impl.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import junit.framework.TestCase;

public class DocumentImplTest extends TestCase {

	public DocumentImplTest() {
		super();
	}

	public DocumentImplTest(String name) {
		super(name);
	}
	
	public void testCreateElement() {
		String tagName = "LocalName";
		String namespace = "http://ws.apache.org/axis2/ns";
		DocumentImpl doc = new DocumentImpl();
		Element elem = doc.createElement(tagName);
		
		assertEquals("Local name misnatch",tagName,elem.getLocalName());
		
		elem = doc.createElementNS(tagName,namespace);
		assertEquals("Local name misnatch",tagName,elem.getLocalName());
		assertEquals("Namespace misnatch",namespace,elem.getNamespaceURI());
		
	}
	
	public void testCreateAttribute() {
		String attrName = "attrIdentifier";
		String attrValue = "attrValue";
		String attrNs = "http://ws.apache.org/axis2/ns";
		String attrNsPrefix = "axis2";
		
		DocumentImpl doc = new DocumentImpl();
		Attr attr = doc.createAttribute(attrName);

		assertEquals("Attr name mismatch",attrName,attr.getLocalName());
		assertNull("Namespace value should be null", attr.getNamespaceURI());
		
		
		attr = doc.createAttributeNS(attrNs,attrNsPrefix + ":" + attrName);
		assertEquals("Attr name mismatch",attrName,attr.getLocalName());
		assertNotNull("Namespace value should not be null", attr.getNamespaceURI());
		assertEquals("NamsspaceURI mismatch", attrNs, attr.getNamespaceURI());
		assertEquals("namespace prefix mismatch", attrNsPrefix, attr.getPrefix());
		
		attr.setValue(attrValue);
		
	}
	
	public void testCreateText() {
		
	}

}
