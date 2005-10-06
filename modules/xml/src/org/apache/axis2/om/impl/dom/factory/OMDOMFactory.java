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
package org.apache.axis2.om.impl.dom.factory;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMComment;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMDocType;
import org.apache.axis2.om.OMDocument;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMProcessingInstruction;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;

import javax.xml.namespace.QName;

public class OMDOMFactory implements OMFactory {

	public OMDocument createOMDocument() {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMElement createOMElement(String localName, OMNamespace ns) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMElement createOMElement(String localName, OMNamespace ns, OMContainer parent) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMElement createOMElement(String localName, OMNamespace ns, OMContainer parent, OMXMLParserWrapper builder) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMElement createOMElement(String localName, String namespaceURI, String namespacePrefix) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMElement createOMElement(QName qname, OMContainer parent) throws OMException {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMNamespace createOMNamespace(String uri, String prefix) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMText createText(OMElement parent, String text) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMText createText(String s) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMText createText(String s, int type) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMText createText(String s, String mimeType, boolean optimize) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMText createText(Object dataHandler, boolean optimize) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMText createText(OMElement parent, String s, String mimeType, boolean optimize) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMAttribute createOMAttribute(String localName, OMNamespace ns, String value) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMDocType createOMDocType(OMContainer parent, String content) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMProcessingInstruction createOMProcessingInstruction(OMContainer parent, String piTarget, String piData) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMComment createOMComment(OMContainer parent, String content) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}
	
}
