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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLPart;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.factory.XMLPartFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * MessageImpl
 * A Message is an XML part + Attachments.
 * Most of the implementation delegates to the XMLPart implementation.
 */
/**
 * @author scheu
 *
 */
public class MessageImpl implements Message {

	Protocol protocol = Protocol.unknown; // the protocol, defaults to unknown
	XMLPart xmlPart = null; // the representation of the xmlpart
	List<Attachment> attachments = new ArrayList<Attachment>(); // non-xml parts
    boolean mtomEnabled;
    private MimeHeaders mimeHeaders = new MimeHeaders(); 
    
    private boolean postPivot = false;
    
	// Constants
	private static final String SOAP11_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String SOAP12_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";
	private static final String SOAP11_CONTENT_TYPE ="text/xml";
	private static final String SOAP12_CONTENT_TYPE = "application/soap+xml";
	
	/**
	 * MessageImpl should be constructed via the MessageFactory.
	 * This constructor constructs an empty message with the specified protocol
	 * @param protocol
	 */
	MessageImpl(Protocol protocol) throws WebServiceException, XMLStreamException {
		createXMLPart(protocol);
	}
	
	/**
	 * Message is constructed by the MessageFactory.
	 * This constructor creates a message from the specified root.
	 * @param root
	 */
	MessageImpl(OMElement root) throws WebServiceException, XMLStreamException  {
		createXMLPart(root);
	}
	
	/**
	 * Message is constructed by the MessageFactory.
	 * This constructor creates a message from the specified root.
	 * @param root
	 */
	MessageImpl(SOAPEnvelope root) throws WebServiceException, XMLStreamException  {
	    createXMLPart(root);
	}
    
    /**
     * Create a new XMLPart and Protocol from the root
     * @param root SOAPEnvelope
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    private void createXMLPart(SOAPEnvelope root) throws WebServiceException, XMLStreamException {
        XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
        xmlPart = factory.createFrom(root);
        protocol = xmlPart.getProtocol();
        xmlPart.setParent(this); 
    }
    
    /**
     * Create a new XMLPart and Protocol from the root
     * @param root OMElement
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    private void createXMLPart(OMElement root) throws WebServiceException, XMLStreamException {
        XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
        xmlPart = factory.createFrom(root);
        protocol = xmlPart.getProtocol();
        xmlPart.setParent(this);
    }
    
    /**
     * Create a new empty XMLPart from the Protocol
     * @param protocol
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    private void createXMLPart(Protocol protocol) throws WebServiceException, XMLStreamException {
        this.protocol = protocol;
        if (protocol.equals(Protocol.unknown)) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ProtocolIsNotKnown"));
        } else if (protocol.equals(Protocol.rest)) {
            // TODO Need REST support
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("RESTIsNotSupported"));
        }
        XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
        xmlPart = factory.create(protocol);
        xmlPart.setParent(this);
    }

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.Message#getAsSOAPMessage()
	 */
	public SOAPMessage getAsSOAPMessage() throws WebServiceException {

		// TODO: 
		// This is a non performant way to create SOAPMessage. I will serialize
		// the xmlpart content and then create an InputStream of byte.
		// Finally create SOAPMessage using this InputStream.
		// The real solution may involve using non-spec, implementation
		// constructors to create a Message from an Envelope
		try {
			// Get OMElement from XMLPart.
			OMElement element = xmlPart.getAsOMElement();
			
			// Get the namespace so that we can determine SOAP11 or SOAP12
			OMNamespace ns = element.getNamespace();
			
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			element.serialize(outStream);

			// Create InputStream
			ByteArrayInputStream inStream = new ByteArrayInputStream(outStream
					.toByteArray());
			
			// Create MessageFactory that supports the version of SOAP in the om element
			MessageFactory mf = getSAAJConverter().createMessageFactory(ns.getNamespaceURI());

			// Create soapMessage object from Message Factory using the input
			// stream created from OM.
      
			// Get the MimeHeaders
			MimeHeaders defaultHeaders = this.getMimeHeaders();

			// Toggle based on SOAP 1.1 or SOAP 1.2
			String contentType = null;
			if (ns.getNamespaceURI().equals(SOAP11_ENV_NS)) {
				contentType = SOAP11_CONTENT_TYPE;
			} else {
				contentType = SOAP12_CONTENT_TYPE;
			}
            
            // Override the content-type
			defaultHeaders.setHeader("Content-type", contentType +"; charset=UTF-8");
			SOAPMessage soapMessage = mf.createMessage(defaultHeaders, inStream);
            
            // At this point the XMLPart is still an OMElement.  We need to change it to the new SOAPEnvelope.
			createXMLPart(soapMessage.getSOAPPart().getEnvelope());
            
            // Now add the attachments to the SOAPMessage
            Iterator it = getAttachments().iterator();
            while (it.hasNext()) {
                AttachmentPart ap = MessageUtils.createAttachmentPart((Attachment)it.next(), soapMessage);
                soapMessage.addAttachmentPart(ap);
            }
            
            return soapMessage;
		} catch (Exception e) {
			throw ExceptionFactory.makeWebServiceException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis2.jaxws.message.XMLPart#getAsBlock(java.lang.Object,
	 *      org.apache.axis2.jaxws.message.factory.BlockFactory)
	 */
	public Block getAsBlock(Object context, BlockFactory blockFactory) throws WebServiceException, XMLStreamException {
		return xmlPart.getAsBlock(context, blockFactory);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.XMLPart#getAttachments()
	 */
	public List<Attachment> getAttachments() {
		return attachments;
	}
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getAttachment(java.lang.String)
     */
    public Attachment getAttachment(String cid) {
       if (attachments != null) {
           Iterator<Attachment> itr = attachments.iterator();
           while (itr.hasNext()) {
               Attachment a = itr.next();
               if (a.getContentID().equals(cid)) {
                   return a;
               }
           }
       }
        
       return null;
    }

    
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.Message#removeAttachment(java.lang.String)
	 */
    public Attachment removeAttachment(String cid) {
        if (attachments != null) {
            Iterator<Attachment> itr = attachments.iterator();
            while (itr.hasNext()) {
                Attachment a = itr.next();
                if (a.getContentID().equals(cid)) {
                   itr.remove();
                   return a;
                }
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.XMLPart#getProtocol()
	 */
	public Protocol getProtocol() {
		return protocol;
	}

	public OMElement getAsOMElement() throws WebServiceException {
		return xmlPart.getAsOMElement();
	}

	public javax.xml.soap.SOAPEnvelope getAsSOAPEnvelope() throws WebServiceException {
		return xmlPart.getAsSOAPEnvelope();
	}

	public Block getBodyBlock(int index, Object context, BlockFactory blockFactory) throws WebServiceException {
		return xmlPart.getBodyBlock(index, context, blockFactory);
	}

	public Block getHeaderBlock(String namespace, String localPart, Object context, BlockFactory blockFactory) throws WebServiceException {
		return xmlPart.getHeaderBlock(namespace, localPart, context, blockFactory);
	}

	public int getNumBodyBlocks() throws WebServiceException {
		return xmlPart.getNumBodyBlocks();
	}

	public int getNumHeaderBlocks() throws WebServiceException {
		return xmlPart.getNumHeaderBlocks();
	}

	public XMLStreamReader getXMLStreamReader(boolean consume) throws WebServiceException {
		return xmlPart.getXMLStreamReader(consume);
	}

	public boolean isConsumed() {
		return xmlPart.isConsumed();
	}

	public void outputTo(XMLStreamWriter writer, boolean consume) throws XMLStreamException, WebServiceException {
		xmlPart.outputTo(writer, consume);
	}

	public void removeBodyBlock(int index) throws WebServiceException {
		xmlPart.removeBodyBlock(index);
	}

	public void removeHeaderBlock(String namespace, String localPart) throws WebServiceException {
		xmlPart.removeHeaderBlock(namespace, localPart);
	}

	public void setBodyBlock(int index, Block block) throws WebServiceException {
        xmlPart.setBodyBlock(index, block);
	}

	public void setHeaderBlock(String namespace, String localPart, Block block) throws WebServiceException {
		xmlPart.setHeaderBlock(namespace, localPart, block);
	}

	public String traceString(String indent) {
		return xmlPart.traceString(indent);
	}
	
	/**
	 * Load the SAAJConverter
	 * @return SAAJConverter
	 */
	SAAJConverter converter = null;
	private SAAJConverter getSAAJConverter() {
		if (converter == null) {
			SAAJConverterFactory factory = (
						SAAJConverterFactory)FactoryRegistry.getFactory(SAAJConverterFactory.class);
			converter = factory.getSAAJConverter();
		}
		return converter;
	}
    
    public void addAttachment(Attachment data) {
        attachments.add(data);
    }
    
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#createAttachment(javax.activation.DataHandler, java.lang.String)
     */
    public Attachment createAttachment(DataHandler dh, String id) {
        return new AttachmentImpl(dh, id);
    }

    public Message getParent() {
        return null;
    }

    public void setParent(Message msg) { 
        // A Message does not have a parent
        throw new UnsupportedOperationException();
    }

    public boolean isMTOMEnabled() {
        return mtomEnabled;
    }

    public void setMTOMEnabled(boolean b) {
        mtomEnabled = b;
    }

	public XMLFault getXMLFault() throws WebServiceException {
		return xmlPart.getXMLFault();
	}

	public void setXMLFault(XMLFault xmlFault) throws WebServiceException {
		xmlPart.setXMLFault(xmlFault);
	}

	public boolean isFault() throws WebServiceException {
		return xmlPart.isFault();
	}

    public String getXMLPartContentType() {
        return xmlPart.getXMLPartContentType();
    }

    public Style getStyle() {
        return xmlPart.getStyle();
    }

    public void setStyle(Style style) throws WebServiceException {
        xmlPart.setStyle(style);
    }

    public QName getOperationElement() throws WebServiceException {
        return xmlPart.getOperationElement();
    }

    public void setOperationElement(QName operationQName) throws WebServiceException {
        xmlPart.setOperationElement(operationQName);
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Attachment#getMimeHeaders()
     */
    public MimeHeaders getMimeHeaders() {
       return mimeHeaders;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Attachment#setMimeHeaders(javax.xml.soap.MimeHeaders)
     */
    public void setMimeHeaders(MimeHeaders mhs) {
        mimeHeaders = mhs;
        if (mimeHeaders == null) {
            mimeHeaders = new MimeHeaders();
        }
    }

    public Block getBodyBlock(Object context, BlockFactory blockFactory) throws WebServiceException {
        return xmlPart.getBodyBlock(context, blockFactory);
    }

    public void setBodyBlock(Block block) throws WebServiceException {
        xmlPart.setBodyBlock(block);
    }

    public void setPostPivot() {
        this.postPivot = true;
    }
    
    public boolean isPostPivot() {
        return postPivot;
    }

    public int getIndirection() {
        return xmlPart.getIndirection();
    }

    public void setIndirection(int indirection) {
        xmlPart.setIndirection(indirection);
    }

}
