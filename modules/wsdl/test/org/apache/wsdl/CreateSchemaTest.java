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
 */

package org.apache.wsdl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.axis.wsdl.builder.WOMBuilderFactory;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.wsdl.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author chathura@opensource.lk
 *  
 */
public class CreateSchemaTest extends AbstractTestCase {

	private WSDLDescription womDescription;

	private Definition wsdl4jDefinition;

	public CreateSchemaTest(String arg) {
		super(arg);
	}

	protected void setUp() throws Exception {
		InputStream in = new FileInputStream(getTestResourceFile("BookQuote.wsdl"));
		this.womDescription = WOMBuilderFactory.getBuilder(
				WOMBuilderFactory.WSDL11).build(in);

		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		Document doc = Utils.newDocument(new FileInputStream(getTestResourceFile(
				"BookQuote.wsdl")));
		this.wsdl4jDefinition = reader.readWSDL(null, doc);
	}

	public void testInsertedMultipartType() {
		WSDLTypes types = womDescription.getTypes();
		assertNotNull(types);
		Iterator iterator = types.getExtensibilityElements().iterator();
		WSDLExtensibilityElement element = null;
		while (iterator.hasNext()) {
			element = (WSDLExtensibilityElement) iterator.next();
			if (ExtensionConstants.SCHEMA.equals(element.getType()))
				break;
		}
		assertNotNull(element);
		Schema schema = (Schema) element;
		NodeList childNodes = schema.getElelment().getChildNodes();
		Element insertedElementForMessageReference = null;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item instanceof Element
					&& "complexType".equals(((Element) item).getTagName())
					&& "BookQuote_getBookPrice".equals(((Element) item)
							.getAttribute("name"))) {
				insertedElementForMessageReference = (Element) item;
			}
		}

		assertNotNull(insertedElementForMessageReference);

	}

}