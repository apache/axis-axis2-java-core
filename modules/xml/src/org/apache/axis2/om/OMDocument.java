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

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.om.impl.OMOutputImpl;


public interface OMDocument extends OMContainer {

	/**
	 * Field XML_10 XML Version 1.0
	 */
	public final static String XML_10 = "1.0";
	
	/**
	 * Field XML_11 XML Version 1.1
	 */
	public final static String XML_11 = "1.1";
	
	/**
	 * Returns the document element
	 * @return
	 */
	public OMElement getDocumentElement();
	
	/**
	 * Sets the document element of the XML document
	 * @param rootElement
	 */
	public void setDocumentElement(OMElement rootElement);
	
	/**
	 * Returns the XML version
	 * @return
	 */
	public String getXMLVersion();
	
	/**
	 * Sets the XML version
	 * @see OMDocumentImpl#XML_10 XML 1.0
	 * @see OMDocumentImpl#XML_11 XML 1.1
	 * @param version
	 */
	public void setXMLVersion(String version);
	
	/**
	 * Returns the caracter set encoding scheme
	 * @return
	 */
	public String getCharsetEncoding();
	
	/**
	 * Sets the character set encoding scheme to be used
	 * @param charsetEncoding
	 */
	public void setCharsetEncoding(String charsetEncoding);

	/**
	 * Serialize the OMDocument
	 * @param omOutput
	 * @throws XMLStreamException
	 */
	public void serialize(OMOutputImpl omOutput) throws XMLStreamException;
	
	/**
	 * Serilize the OMDocument with the option of excluding/including the 
	 * XML declaration
	 * @param omOutput
	 * @param includeXMLDeclaration Whether to include the XML declaration or not 
	 * @throws XMLStreamException
	 */
	public void serialize(OMOutputImpl omOutput, boolean includeXMLDeclaration) throws XMLStreamException;
	
	/**
	 * Serializa the document with cache on
	 * @param omOutput
	 * @throws XMLStreamException
	 */
	public void serializeWithCache(OMOutputImpl omOutput) throws XMLStreamException;
	
	/**
	 * Seralize the document with cache on with the option on excluding the XML declaration
	 * @param omOutput
	 * @param includeXMLDeclaration
	 * @throws XMLStreamException
	 */
	public void serializeWithCache(OMOutputImpl omOutput, boolean includeXMLDeclaration) throws XMLStreamException;
}
