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
package org.apache.axis2.jaxws.message.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;

/**
 * XMLPartOptimizedImpl
 * 
 * This class extends the implementation of the XMLPartBase so that it 
 * can define the transformations between OM, SAAJ SOAPEnvelope and XMLSpine.
 * 
 * This class uses OMObjectWrapperElement constructs to speed up the transformations.
 * 
 * @see org.apache.axis2.jaxws.impl.XMLPartBase
 * 
 */
public class XMLPartOptimizedImpl extends  XMLPartImpl {

	// TODO Add custom transformations that take advantage of OMObjectWrapperElement
	
	/**
	 * XMLPart should be constructed via the XMLPartFactory.
	 * This constructor constructs an empty XMLPart with the specified protocol
	 * @param protocol
	 * @throws MessageException
	 */
	XMLPartOptimizedImpl(Protocol protocol) throws MessageException {
		super(protocol);
	}
	
	/**
	 * XMLPart should be constructed via the XMLPartFactory.
	 * This constructor creates an XMLPart from the specified root.
	 * @param root
	 * @throws MessageException
	 */
	XMLPartOptimizedImpl(OMElement root) throws MessageException {
		super(root);
	}
	
}
