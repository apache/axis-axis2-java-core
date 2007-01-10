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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.attachments.AttachmentUtils;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * XMLPartImpl
 * 
 * This class extends the implementation of the XMLPartBase so that it 
 * can define the transformations between OM, SAAJ SOAPEnvelope and XMLSpine.
 * @see org.apache.axis2.jaxws.impl.XMLPartBase
 * 
 */
public class XMLPartImpl extends  XMLPartBase {

	SAAJConverter converter = null;
	
	/**
	 * XMLPart should be constructed via the XMLPartFactory.
	 * This constructor constructs an empty XMLPart with the specified protocol
	 * @param protocol
	 * @throws MessageException
	 */
	XMLPartImpl(Protocol protocol) throws MessageException {
		super(protocol);
	}
	
	/**
	 * XMLPart should be constructed via the XMLPartFactory.
	 * This constructor creates an XMLPart from the specified root.
	 * @param root
	 * @throws MessageException
	 */
	XMLPartImpl(OMElement root) throws MessageException {
		super(root);
	}
	
	/**
	 * XMLPart should be constructed via the XMLPartFactory.
	 * This constructor creates an XMLPart from the specified root.
	 * @param root
	 * @throws MessageException
	 */
	XMLPartImpl(SOAPEnvelope root) throws MessageException {
		super(root);
	}
	
	@Override
	protected OMElement _convertSE2OM(SOAPEnvelope se) throws MessageException {
		return getSAAJConverter().toOM(se);
	}

	@Override
	protected OMElement _convertSpine2OM(XMLSpine spine) throws MessageException {
		// Get an XMLStreamReader that consumes the spine object
		XMLStreamReader reader = spine.getXMLStreamReader(true);
		// Get a SOAP OM Builder.  Passing null causes the version to be automatically triggered
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, null);  
		// Create and return the OM Envelope
		org.apache.axiom.soap.SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
        
        // If we have MTOM attachments, we need to replace the <xop:include>
        // elements with OMText binary nodes.
        Message msg = getParent();
        if (msg.isMTOMEnabled()) {
            // First find all of the <xop:include> elements
            ArrayList<OMElement> xops = AttachmentUtils.findXopElements(omEnvelope);
            
            if (xops != null) {
                QName href = new QName("","href");
                Iterator<OMElement> itr = xops.iterator();
                while (itr.hasNext()) {
                    OMElement xop = itr.next();
                    String cid = xop.getAttributeValue(href);
                    
                    // Then find their corresponding Attachment object
                    Attachment a = msg.getAttachment(cid);
                    
                    // Convert the <xop:include> OMElement into an OMText
                    // binary node and replace it in the tree.                    
                    OMText binaryNode = AttachmentUtils.makeBinaryOMNode(xop, a);
                    xop.insertSiblingAfter(binaryNode);
                    xop.detach();
                }
            }
        }
        
		return omEnvelope;
	}

	@Override
	protected SOAPEnvelope _convertOM2SE(OMElement om) throws MessageException {
		return getSAAJConverter().toSAAJ((org.apache.axiom.soap.SOAPEnvelope) om);
	}

	@Override
	protected SOAPEnvelope _convertSpine2SE(XMLSpine spine) throws MessageException {
		return _convertOM2SE(_convertSpine2OM(spine));
	}

	@Override
	protected XMLSpine _convertOM2Spine(OMElement om) throws MessageException {
		return new XMLSpineImpl((org.apache.axiom.soap.SOAPEnvelope) om);
	}

	@Override
	protected XMLSpine _convertSE2Spine(SOAPEnvelope se) throws MessageException {
		return _convertOM2Spine(_convertSE2OM(se));
	}

	@Override
	protected XMLSpine _createSpine(Protocol protocol) throws MessageException {
		// Use the default implementation provided in XMLPartBase
		return super._createSpine(protocol);
	}
	
	/**
	 * Load the SAAJConverter
	 * @return SAAJConverter
	 */
	protected SAAJConverter getSAAJConverter() {
		if (converter == null) {
			SAAJConverterFactory factory = (
						SAAJConverterFactory)FactoryRegistry.getFactory(SAAJConverterFactory.class);
			converter = factory.getSAAJConverter();
		}
		return converter;
	}
	
}
