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
package org.apache.axis2.jaxws.message.databinding.impl;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.databinding.JAXBBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;

/**
 * JAXBBlockImpl
 * 
 * A Block containing a JAXB business object
 */
public class JAXBBlockImpl extends BlockImpl implements JAXBBlock {

	protected static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	protected static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	/**
	 * Called by JAXBBlockFactory
	 * @param busObject
	 * @param busContext
	 * @param qName
	 * @param factory
	 */
	JAXBBlockImpl(Object busObject, Object busContext, QName qName, BlockFactory factory) {
		super(busObject, 
				busContext, 
				(qName==null) ? getQName(busObject, (JAXBContext) busContext): qName , 
				factory);
	}
	
	

	/**
	 * Called by JAXBBlockFactory
	 * @param omelement
	 * @param busContext
	 * @param qName
	 * @param factory
	 */
	JAXBBlockImpl(OMElement omElement, Object busContext, QName qName, BlockFactory factory) {
		super(omElement, busContext, qName, factory);
	}

	@Override
	protected Object _getBOFromReader(XMLStreamReader reader, Object busContext) throws XMLStreamException, MessageException {
		try {
			// Very easy, use the Context to get the Unmarshaller.
			// Use the marshaller to get the jaxb object
			JAXBContext jc = (JAXBContext) busContext;
			Unmarshaller u = jc.createUnmarshaller();
			Object jaxb = u.unmarshal(reader);
			setQName(getQName(jaxb, jc));
			return jaxb;
		} catch(JAXBException je) {
			throw ExceptionFactory.makeMessageException(je);
		}
	}

	@Override
	protected XMLStreamReader _getReaderFromBO(Object busObj, Object busContext) throws XMLStreamException, MessageException {
		// TODO Review and determine if there is a better solution
		
		// This is hard because JAXB does not expose a reader from the business object.
		// The solution is to write out the object and use a reader to read it back in.
		// First create an XMLStreamWriter backed by a writer
		StringWriter sw = new StringWriter();
		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(sw);
		
		// Write the business object to the writer
		_outputFromBO(busObj, busContext, writer);
		
		// Flush the writer and get the String
		writer.flush();
		sw.flush();
		String str = sw.toString();
		
		// Return a reader backed by the string
		StringReader sr = new StringReader(str);
		return inputFactory.createXMLStreamReader(sr);
	}

	@Override
	protected void _outputFromBO(Object busObject, Object busContext, XMLStreamWriter writer) throws XMLStreamException, MessageException {
		try {
			// Very easy, use the Context to get the Marshaller.
			// Use the marshaller to write the object.  
			JAXBContext jc = (JAXBContext) busContext;
			Marshaller m = jc.createMarshaller();
			m.marshal(busObject, writer);
		} catch(JAXBException je) {
			// TODO NLS
			throw ExceptionFactory.makeMessageException(je);
		}
	}

	/**
	 * Get the QName from the jaxb object
	 * @param jaxb
	 * @param jbc
	 * @throws MessageException
	 */
	private static QName getQName(Object jaxb, JAXBContext jbc){
		JAXBIntrospector jbi = jbc.createJAXBIntrospector();
		return jbi.getElementName(jaxb);
	}
}
