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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.Text;

import junit.framework.TestCase;

public class TextImplTest extends TestCase {

	public TextImplTest() {
		super();
	}

	public TextImplTest(String name) {
		super(name);
	}
	
	public void testSetText() {
		OMDOMFactory factory = new OMDOMFactory();
		String localName = "TestLocalName";
		String namespace = "http://ws.apache.org/axis2/ns";
		String prefix = "axis2";
		String tempText = "The quick brown fox jumps over the lazy dog";
		
		OMElement elem = factory.createOMElement(localName,namespace,prefix);
		OMText textNode = factory.createText(elem,tempText);
		
		assertEquals("Text value mismatch", tempText, textNode.getText());
	}
	
	public void testAppendText() {
		OMDOMFactory factory = new OMDOMFactory();
		String localName = "TestLocalName";
		String namespace = "http://ws.apache.org/axis2/ns";
		String prefix = "axis2";
		String tempText = "The quick brown fox jumps over the lazy dog";
		String textToAppend = " followed by another fox";
		
		OMElement elem = factory.createOMElement(localName,namespace,prefix);
		OMText textNode = factory.createText(elem,tempText);
		
		((Text)textNode).appendData(textToAppend);
		
		assertEquals("Text value mismatch", tempText+textToAppend, textNode.getText());
	}
	
	

}
