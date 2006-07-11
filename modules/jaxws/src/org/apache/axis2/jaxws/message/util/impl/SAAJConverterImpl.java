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
package org.apache.axis2.jaxws.message.util.impl;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.message.util.SOAPElementReader;

/**
 * SAAJConverterImpl
 * Provides an conversion methods between OM<->SAAJ
 */
public class SAAJConverterImpl implements SAAJConverter {

	/**
	 * Constructed via SAAJConverterFactory
	 */
	SAAJConverterImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.util.SAAJConverter#toSAAJ(org.apache.axiom.soap.SOAPEnvelope)
	 */
	public SOAPEnvelope toSAAJ(org.apache.axiom.soap.SOAPEnvelope omEnvelope)
			throws MessageException {
		SOAPEnvelope soapEnvelope = null;
		try {
			// Build the default envelope
			MessageFactory mf = MessageFactory.newInstance();
			SOAPMessage sm = mf.createMessage();
			SOAPPart sp = sm.getSOAPPart();
			soapEnvelope = sp.getEnvelope();
			
			// The getSOAPEnvelope() call creates a default SOAPEnvelope with a SOAPHeader and SOAPBody.
			// The SOAPHeader and SOAPBody are removed (they will be added back in if they are present in the 
			// OMEnvelope).
			SOAPBody soapBody = soapEnvelope.getBody();
			if (soapBody != null) {
				soapBody.detachNode();
				//soapEnvelope.removeChild(soapBody);
			}
			SOAPHeader soapHeader = soapEnvelope.getHeader();
			if (soapHeader != null) {
				soapHeader.detachNode();
				//soapEnvelope.removeChild(soapHeader);
			}
			
			// Adjust tag data on the SOAPEnvelope.  (i.e. set the prefix, set the attributes)
			//adjustTagData(soapEnvelope, omEnvelope);
			
			// We don't know if there is a real OM tree or just a backing XMLStreamReader.
			// The best way to walk the data is to get the XMLStreamReader and use this 
			// to build the SOAPElements
			XMLStreamReader reader = omEnvelope.getXMLStreamReaderWithoutCaching();
			
			buildSOAPTree(soapEnvelope, soapEnvelope, null, reader, false);
		} catch (MessageException e) {
			throw e;
		} catch (SOAPException e) {
			throw new MessageException(e);
		}
		return soapEnvelope;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.util.SAAJConverter#toOM(javax.xml.soap.SOAPEnvelope)
	 */
	public org.apache.axiom.soap.SOAPEnvelope toOM(SOAPEnvelope saajEnvelope)
			throws MessageException {
		// Get a XMLStreamReader backed by a SOAPElement tree
		XMLStreamReader reader = new SOAPElementReader(saajEnvelope);
		// Get a SOAP OM Builder.  Passing null causes the version to be automatically triggered
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, null);  
		// Create and return the OM Envelope
		org.apache.axiom.soap.SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
		return omEnvelope;
	}
	
	

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.util.SAAJConverter#toOM(javax.xml.soap.SOAPElement)
	 */
	public OMElement toOM(SOAPElement soapElement) throws MessageException {
		// Get a XMLStreamReader backed by a SOAPElement tree
		XMLStreamReader reader = new SOAPElementReader(soapElement);
		// Get a OM Builder.  Passing null causes the version to be automatically triggered
		StAXOMBuilder builder = new StAXOMBuilder(reader);  
		// Create and return the OM Envelope
		OMElement om = builder.getDocumentElement();
		return om;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.util.SAAJConverter#toSAAJ(org.apache.axiom.om.OMElement, javax.xml.soap.SOAPElement)
	 */
	public SOAPElement toSAAJ(OMElement omElement, SOAPElement parent) throws MessageException {
		XMLStreamReader reader = omElement.getXMLStreamReaderWithoutCaching();
		SOAPElement env = parent;
		while (env != null && !(env instanceof SOAPEnvelope)) {
			env = env.getParentElement();
		}
		if (env == null) {
			// TODO NLS
			throw new MessageException("SOAPEnvelope is needed!");
		}
		return buildSOAPTree((SOAPEnvelope) env, null, parent, reader, false);
	}

	/**
	 * Build SOAPTree
	 * Either the root or the parent is null.
	 * If the root is null, a new element is created under the parent using information from the reader
	 * If the parent is null, the existing root is updated with the information from the reader
	 * @param envelope SOAPEnvelope (used only to create Name objects)
	 * @param root SOAPElement (the element that represents the data in the reader)
	 * @param parent (the parent of the element represented by the reader)
	 * @param reader XMLStreamReader. the first START_ELEMENT matches the root
	 * @param quitAtBody - true if quit reading after the body START_ELEMENT
	 */
	protected SOAPElement buildSOAPTree(SOAPEnvelope envelope, 
					SOAPElement root, 
					SOAPElement parent, 
					XMLStreamReader reader, 
					boolean quitAtBody) 
		throws MessageException {
		try {
			while(reader.hasNext()) {
				int eventID = reader.next();	
				switch (eventID) {
				case XMLStreamReader.START_ELEMENT: {
					
					// The first START_ELEMENT defines the prefix and attributes of the root
					if (parent == null) {
						updateTagData(envelope, root, reader);
						parent = root;
					} else {
						parent = createElementFromTag(envelope, parent, reader);
						if (root == null) {
							root = parent;
						}
					}
					if (quitAtBody && parent instanceof SOAPBody) {
						return root;
					}
					break;
				}
				case XMLStreamReader.ATTRIBUTE: {
					String eventName ="ATTRIBUTE";
					this._unexpectedEvent(eventName);
				}
				case XMLStreamReader.NAMESPACE: {
					String eventName ="NAMESPACE";
					this._unexpectedEvent(eventName);
				}
				case XMLStreamReader.END_ELEMENT: {
					if (parent instanceof SOAPEnvelope) {
						parent = null;
					} else {
						parent = parent.getParentElement();
					}
					break;
				}
				case XMLStreamReader.CHARACTERS: {
					parent.addTextNode(reader.getText());
					break;
				}
				case XMLStreamReader.CDATA: {
					parent.addTextNode(reader.getText());
					break;
				}
				case XMLStreamReader.COMMENT: {
					// SOAP really doesn't have an adequate representation for comments.
					// The defacto standard is to add the whole element as a text node.
					parent.addTextNode("<!--" + reader.getText() + "-->");
					break;
				}
				case XMLStreamReader.SPACE: {
					parent.addTextNode(reader.getText());
					break;
				}
				case XMLStreamReader.START_DOCUMENT: {
					// Ignore
					break;
				}
				case XMLStreamReader.END_DOCUMENT: {
					// Ignore
					break;
				}
				case XMLStreamReader.PROCESSING_INSTRUCTION: {
					// Ignore 
					break;
				}
				case XMLStreamReader.ENTITY_REFERENCE: {
					// Ignore. this is unexpected in a web service message
					break;
				}
				case XMLStreamReader.DTD: {
					// Ignore. this is unexpected in a web service message
					break;
				}
				default:
					this._unexpectedEvent("EventID " +String.valueOf(eventID));
				}
			}	
		} catch (MessageException e) {
			throw e;
		} catch (XMLStreamException e) {
			throw new MessageException(e);
		} catch (SOAPException e) {
			throw new MessageException(e);
		}
		return root;
	}
	
	/**
	 * Create SOAPElement from the current tag data
	 * @param envelope SOAPEnvelope 
	 * @param parent SOAPElement for the new SOAPElement
	 * @param reader XMLStreamReader whose cursor is at the START_ELEMENT
	 * @return
	 */
	protected SOAPElement createElementFromTag(SOAPEnvelope envelope, 
					SOAPElement parent, 
					XMLStreamReader reader) 
		throws SOAPException {
		// Unfortunately, the SAAJ object is a product of both the 
		// QName of the element and the parent object.  For example, 
		// All element children of a SOAPBody must be object's that are SOAPBodyElements.
		// createElement creates the proper child element.
		QName qName = reader.getName();
		String prefix = reader.getPrefix();
		Name name = envelope.createName(qName.getLocalPart(), prefix, qName.getNamespaceURI());
		SOAPElement child = createElement(parent, name);
		
		// Update the tag data on the child
		updateTagData(envelope, child, reader);
		return child;
	}
	
	/**
	 * Create child SOAPElement 
	 * @param parent SOAPElement
	 * @param name Name
	 * @return
	 */
	protected SOAPElement createElement(SOAPElement parent, Name name) 
		throws SOAPException {
		SOAPElement child;
		if (parent instanceof SOAPEnvelope) {
			if (name.getURI().equals(parent.getNamespaceURI())) {
				if (name.getLocalName().equals("Body")) {
					child = ((SOAPEnvelope)parent).addBody();
				} else {
					child = ((SOAPEnvelope)parent).addHeader();
				}
			} else {
				child = parent.addChildElement(name);
			}
		} else if (parent instanceof SOAPBody) {
			if (name.getURI().equals(parent.getNamespaceURI()) &&
			    name.getLocalName().equals("Fault")) {
				child = ((SOAPBody)parent).addFault();
			} else {
				child = ((SOAPBody)parent).addBodyElement(name);
			}
		} else if (parent instanceof SOAPHeader) {
			child = ((SOAPHeader)parent).addHeaderElement(name);
		} else if (parent instanceof SOAPFault) {
			// This call assumes that the addChildElement implementation
			// is smart enough to add "Detail" or "SOAPFaultElement" objects.
			child = parent.addChildElement(name);
		} else if (parent instanceof Detail) {
			child = ((Detail) parent).addDetailEntry(name); 
		} else {
			child = parent.addChildElement(name);
		}
	
		return child;
	}
	
	/**
	 * update the tag data of the SOAPElement
	 * @param envelope SOAPEnvelope
	 * @param element SOAPElement
	 * @param reader XMLStreamReader whose cursor is at START_ELEMENT
	 */
	protected void updateTagData(SOAPEnvelope envelope, 
			SOAPElement element, 
			XMLStreamReader reader) throws SOAPException {
		String prefix = reader.getPrefix();
		prefix = (prefix == null) ? "" : prefix;
		
		// Make sure the prefix is correct
		if (prefix.length() > 0 && !element.getPrefix().equals(prefix)) {
			element.setPrefix(prefix);
		}
		
		//Remove all of the namespace declarations on the element
		Iterator it = element.getNamespacePrefixes();
		while (it.hasNext()) {
			String aPrefix = (String)it.next();
			element.removeNamespaceDeclaration(aPrefix);
		}
		
		// Add the namespace declarations from the reader
		int size = reader.getNamespaceCount();
		for (int i=0; i<size; i++) {
			element.addNamespaceDeclaration(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
		}
		
		// Add attributes 
		addAttributes(envelope, element, reader);
		
		return;
	}
	
	/** add attributes
	 * @param envelope SOAPEnvelope
	 * @param element SOAPElement which is the target of the new attributes
	 * @param reader XMLStreamReader whose cursor is at START_ELEMENT
	 * @throws SOAPException
	 */
	protected void addAttributes(SOAPEnvelope envelope, 
			SOAPElement element, 
			XMLStreamReader reader) throws SOAPException {
		
		// Add the attributes from the reader
		int size = reader.getAttributeCount();
		for (int i=0; i<size; i++) {
			QName qName = reader.getAttributeName(i);
			String prefix = reader.getAttributePrefix(i);
			String value = reader.getAttributeValue(i);
			Name name = envelope.createName(qName.getLocalPart(), prefix, qName.getNamespaceURI());
			element.addAttribute(name, value);
		}
	}
	
	private void _unexpectedEvent(String event) throws MessageException {
		// Review We need NLS for this message, but this code will probably 
		// be added to JAX-WS.  So for now we there is no NLS.
		// TODO NLS
		throw new MessageException("Unexpected XMLStreamReader event:" + event);
	}
}
