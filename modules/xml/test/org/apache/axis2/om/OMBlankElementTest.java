package org.apache.axis2.om;

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
 *
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

import junit.framework.TestCase;
import org.apache.axis2.om.impl.OMOutputImpl;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;


public class OMBlankElementTest extends TestCase {


	public OMBlankElementTest(String name) {
		super(name);
	}

	public void testBlankOMElem() {
		try {
			//We should not get anything as the return value here: the output of the serialization
			String value = buildBlankOMElem();
//			System.out.println(value);
			assertNull("There's a serialized output for a blank XML element that cannot exist", value);
		} catch (Exception e) {
			//An exception is thrown trying to serialize a blank element
			assertTrue(true);
		}
	}



	String buildBlankOMElem() throws XMLStreamException {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace namespace1 = factory.createOMNamespace("","");
		OMElement elem1 = factory.createOMElement("",namespace1);

		StringWriter writer = new StringWriter();
		elem1.build();
		elem1.serializeWithCache(new OMOutputImpl(XMLOutputFactory
				.newInstance().createXMLStreamWriter(writer)));
		writer.flush();
		return writer.toString();
	}
}
