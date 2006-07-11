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
package org.apache.axis2.jaxws.message.factory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;

/**
 * MessageFactory
 * 
 * Creates a Message object.  The two common patterns are:
 *   - Create an empty message for a specific protocol
 *   - Create a message sourced from OM (XMLStreamReader)
 *   
 * The FactoryRegistry should be used to get access to the Factory
 * @see org.apache.axis2.jaxws.registry.FactoryRegistry
 */
public interface MessageFactory {
	/**
	 * create Message from XMLStreamReader
	 * @param reader XMLStreamReader
	 * @throws XMLStreamException
	 */
	public Message createFrom(XMLStreamReader reader) throws XMLStreamException, MessageException;
	
	/**
	 * createBlock from OMElement
	 * @param omElement OMElement
	 * @throws XMLStreamException
	 */
	public Message createFrom(OMElement omElement) throws XMLStreamException, MessageException;
	
	/**
	 * create empty Message of the specified protocol
	 * @param protocol
	 * @throws XMLStreamException
	 */
	public Message create(Protocol protocol) throws XMLStreamException, MessageException;
	
}
