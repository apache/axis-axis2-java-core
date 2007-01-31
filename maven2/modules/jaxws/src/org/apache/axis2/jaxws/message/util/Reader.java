/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;

/**
 * Reader
 * In many situations, you want the ability to reset an XMLStreamReader.
 * (Or at least ask if the XMLStreamReader is resettable).
 * 
 * The Reader abstract class:
 *    - accepts an XMLStreamReader
 *    - provides reset() and isResettable() methods
 * Adds support resettable support to XMLStreamReader
 * 
 * Derived classes must pass the initial reader to the constructor and indicate if it
 * is resettable.
 * Derived classes must also provide an implementation of the newReader() method if 
 * resettable.
 */
public abstract class Reader implements XMLStreamReader {
	protected XMLStreamReader reader;
	private final boolean resettable;
	/**
	 * @param reader
	 * @param resettable
	 */
	Reader(XMLStreamReader reader, boolean resettable) {
		this.reader = reader;
		this.resettable = resettable;
	}
	
	/**
	 * Get a newReader from the Object
	 * @return XMLStreamReader
	 */
	protected abstract XMLStreamReader newReader();
	
	/**
	 * isResettable
	 * @return true or false
	 */
	public boolean isResettable() {
		return resettable;
	}
	
	public void reset() throws WebServiceException {
		if (!resettable) {
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("resetReaderErr"));
		}
		reader = newReader();
	}

	public void close() throws XMLStreamException {
		reader.close();
	}

	public int getAttributeCount() {
		return reader.getAttributeCount();
	}

	public String getAttributeLocalName(int arg0) {
		return reader.getAttributeLocalName(arg0);
	}

	public QName getAttributeName(int arg0) {
		return reader.getAttributeName(arg0);
	}

	public String getAttributeNamespace(int arg0) {
		return reader.getAttributeNamespace(arg0);
	}

	public String getAttributePrefix(int arg0) {
		return reader.getAttributePrefix(arg0);
	}

	public String getAttributeType(int arg0) {
		return reader.getAttributeType(arg0);
	}

	public String getAttributeValue(int arg0) {
		return reader.getAttributeValue(arg0);
	}

	public String getAttributeValue(String arg0, String arg1) {
		return reader.getAttributeValue(arg0, arg1);
	}

	public String getCharacterEncodingScheme() {
		return reader.getCharacterEncodingScheme();
	}

	public String getElementText() throws XMLStreamException {
		return reader.getElementText();
	}

	public String getEncoding() {
		return reader.getEncoding();
	}

	public int getEventType() {
		return reader.getEventType();
	}

	public String getLocalName() {
		return reader.getLocalName();
	}

	public Location getLocation() {
		return reader.getLocation();
	}

	public QName getName() {
		return reader.getName();
	}

	public NamespaceContext getNamespaceContext() {
		return reader.getNamespaceContext();
	}

	public int getNamespaceCount() {
		return reader.getNamespaceCount();
	}

	public String getNamespacePrefix(int arg0) {
		return reader.getNamespacePrefix(arg0);
	}

	public String getNamespaceURI() {
		return reader.getNamespaceURI();
	}

	public String getNamespaceURI(int arg0) {
		return reader.getNamespaceURI(arg0);
	}

	public String getNamespaceURI(String arg0) {
		return reader.getNamespaceURI(arg0);
	}

	public String getPIData() {
		return reader.getPIData();
	}

	public String getPITarget() {
		return reader.getPITarget();
	}

	public String getPrefix() {
		return reader.getPrefix();
	}

	public Object getProperty(String arg0) throws IllegalArgumentException {
		return reader.getProperty(arg0);
	}

	public String getText() {
		return reader.getText();
	}

	public char[] getTextCharacters() {
		return reader.getTextCharacters();
	}

	public int getTextCharacters(int arg0, char[] arg1, int arg2, int arg3) throws XMLStreamException {
		return reader.getTextCharacters(arg0, arg1, arg2, arg3);
	}

	public int getTextLength() {
		return reader.getTextLength();
	}

	public int getTextStart() {
		return reader.getTextStart();
	}

	public String getVersion() {
		return reader.getVersion();
	}

	public boolean hasName() {
		return reader.hasName();
	}

	public boolean hasNext() throws XMLStreamException {
		return reader.hasNext();
	}

	public boolean hasText() {
		return reader.hasText();
	}

	public boolean isAttributeSpecified(int arg0) {
		return reader.isAttributeSpecified(arg0);
	}

	public boolean isCharacters() {
		return reader.isCharacters();
	}

	public boolean isEndElement() {
		return reader.isEndElement();
	}

	public boolean isStandalone() {
		return reader.isStandalone();
	}

	public boolean isStartElement() {
		return reader.isStartElement();
	}

	public boolean isWhiteSpace() {
		return reader.isWhiteSpace();
	}

	public int next() throws XMLStreamException {
		return reader.next();
	}

	public int nextTag() throws XMLStreamException {
		return reader.nextTag();
	}

	public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
		reader.require(arg0, arg1, arg2);
	}

	public boolean standaloneSet() {
		return reader.standaloneSet();
	}
	
}
