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
package org.apache.axis2.om.impl.dom.jaxp;

import org.apache.axis2.om.impl.dom.DOMImplementationImpl;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.factory.OMDOMFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class DocumentBuilderImpl extends DocumentBuilder {

	public DocumentBuilderImpl() {
		super();
	}

	/**
	 * Returns whether the parser is configured to understand namespaces or not
	 * The StAX parser used by this DOM impl is namespace aware
	 * therefore this will always return true
	 * @see javax.xml.parsers.DocumentBuilder#isNamespaceAware()
	 */
	public boolean isNamespaceAware() {
		return true;
	}

	/**
	 * The StAX builder used is the org.apache.axis2.om.impl.llom.StAXOMBuilder
	 * is a validating builder
	 * @see javax.xml.parsers.DocumentBuilder#isValidating()
	 */
	public boolean isValidating() {
		return true;
	}

	public DOMImplementation getDOMImplementation() {
		return new DOMImplementationImpl();
	}

	/**
	 * Returns a new document impl 
	 * @see javax.xml.parsers.DocumentBuilder#newDocument()
	 */
	public Document newDocument() {
		DocumentImpl documentImpl = new DocumentImpl();
		documentImpl.setComplete(true);
		return documentImpl;
	}

	public void setEntityResolver(EntityResolver arg0) {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	public void setErrorHandler(ErrorHandler arg0) {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	public Document parse(InputSource inputSource) throws SAXException, IOException {
		try {
			OMDOMFactory factory = new OMDOMFactory();
			//Not really sure whether this will work :-?
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputSource.getCharacterStream());
			StAXOMBuilder builder = new StAXOMBuilder(factory,reader);
			return (DocumentImpl)builder.getDocument();
		}catch (XMLStreamException e) {
			throw new SAXException(e);
		} 
	}
	
	/**
	 * @see javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream)
	 */
	public Document parse(InputStream is) throws SAXException, IOException {
		try {
			OMDOMFactory factory = new OMDOMFactory();
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
			StAXOMBuilder builder = new StAXOMBuilder(factory,reader);
			return (DocumentImpl)builder.getDocument();
		}catch (XMLStreamException e) {
			throw new SAXException(e);
		} 
	}

	/**
	 * @see javax.xml.parsers.DocumentBuilder#parse(java.io.File)
	 */
	public Document parse(File file) throws SAXException, IOException {
		try {
			OMDOMFactory factory = new OMDOMFactory();
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(file));
			StAXOMBuilder builder = new StAXOMBuilder(factory,reader);
			return (DocumentImpl)builder.getDocument();
		}catch (XMLStreamException e) {
			throw new SAXException(e);
		} 
	}

	/**
	 * @see javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream, java.lang.String)
	 */
	public Document parse(InputStream is, String systemId) throws SAXException, IOException {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * @see javax.xml.parsers.DocumentBuilder#parse(java.lang.String)
	 */
	public Document parse(String uri) throws SAXException, IOException {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	
	
}
