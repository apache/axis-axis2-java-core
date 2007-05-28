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
import java.util.Set;

import javax.activation.DataHandler;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.client.Options;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLPart;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLPartFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MessageImpl
 * A Message is an XML part + Attachments.
 * Most of the implementation delegates to the XMLPart implementation.
 * 
 * NOTE: For XML/HTTP (REST), a SOAP 1.1. Envelope is built and the rest payload is placed
 * in the body.  This purposely mimics the Axis2 implementation.
 */
public class MessageImpl implements Message {
    private static final Log log = LogFactory.getLog(MessageImpl.class);
    
    Protocol protocol = Protocol.unknown; // the protocol, defaults to unknown
    XMLPart xmlPart = null; // the representation of the xmlpart
    
    boolean mtomEnabled;
    private MimeHeaders mimeHeaders = new MimeHeaders(); 
    
    // The Message is connected to a MessageContext.
    // Prior to that connection, attachments are stored locally
    // After the connection, attachments are obtained from the MessageContext
    Attachments attachments = new Attachments();  // Local Attachments
    private MessageContext messageContext;
    
    // Set after we have past the pivot point when the message is consumed
    private boolean postPivot = false;
    
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
     * @param protocol or null
     */
    MessageImpl(OMElement root, Protocol protocol) throws WebServiceException, XMLStreamException  {
        createXMLPart(root, protocol);
    }
    
    /**
     * Message is constructed by the MessageFactory.
     * This constructor creates a message from the specified root.
     * @param root
     * @param protocol or null
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
        this.protocol = xmlPart.getProtocol();
        xmlPart.setParent(this); 
    }
    
    /**
     * Create a new XMLPart and Protocol from the root
     * @param root OMElement
     * @throws WebServiceException
     * @throws XMLStreamException
     */
    private void createXMLPart(OMElement root, Protocol protocol) throws WebServiceException, XMLStreamException {
        XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
        xmlPart = factory.createFrom(root, protocol);
        this.protocol = xmlPart.getProtocol();
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
            if (ns.getNamespaceURI().equals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                contentType = SOAPConstants.SOAP_1_1_CONTENT_TYPE;
            } else {
                contentType = SOAPConstants.SOAP_1_2_CONTENT_TYPE;
            }
            
            // Override the content-type
            defaultHeaders.setHeader("Content-type", contentType +"; charset=UTF-8");
            SOAPMessage soapMessage = mf.createMessage(defaultHeaders, inStream);
            
            // At this point the XMLPart is still an OMElement.  We need to change it to the new SOAPEnvelope.
            createXMLPart(soapMessage.getSOAPPart().getEnvelope());
            
            // If axiom read the message from the input stream, 
            // then one of the attachments is a SOAPPart.  Ignore this attachment
            String soapPartContentID = getSOAPPartContentID();  // This may be null
            
            // Add the attachments
            for (String cid:getAttachmentIDs()) {
                DataHandler dh = attachments.getDataHandler(cid);
                boolean isSOAPPart = cid.equals(soapPartContentID);
                if (!isSOAPPart) {
                    AttachmentPart ap = MessageUtils.createAttachmentPart(cid, dh, soapMessage);
                    soapMessage.addAttachmentPart(ap);
                }
            }
            
            return soapMessage;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
    }
    
    private String getSOAPPartContentID() {
        String contentID = null;
        if (messageContext == null) {
            return null;  // Attachments set up programmatically...so there is no SOAPPart
        }
        try {
            contentID = attachments.getSOAPPartContentID();
        } catch (RuntimeException e) {
            // OM will kindly throw an OMException or NPE if the attachments is set up programmatically. 
            return null;
        }
        return contentID;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getValue(java.lang.Object, org.apache.axis2.jaxws.message.factory.BlockFactory)
     */
    public Object getValue(Object context, BlockFactory blockFactory) throws WebServiceException {
        try {
            Object value = null;
            if (protocol == Protocol.rest) {
                // The implementation of rest stores the rest xml inside a dummy soap 1.1 envelope.
                // So use the get body block logic.
                Block block = xmlPart.getBodyBlock(context, blockFactory);
                if (block != null) {
                    value = block.getBusinessObject(true);
                }
                
            } else {
                // Must be SOAP
                if (blockFactory instanceof SOAPEnvelopeBlockFactory) {
                    value = getAsSOAPMessage();
                } else {
                    // TODO: This doesn't seem right to me. We should not have an intermediate StringBlock.  
                    // This is not performant. Scheu 
                    OMElement messageOM = getAsOMElement();
                    String stringValue = messageOM.toString();  
                    String soapNS = (protocol == Protocol.soap11) ? SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE : SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE; 
                    QName soapEnvQname = new QName(soapNS, "Envelope");
                    
                    
                    XMLStringBlockFactory stringFactory = (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
                    Block stringBlock = stringFactory.createFrom(stringValue, null, soapEnvQname);   
                    Block block = blockFactory.createFrom(stringBlock, context);
                    value = block.getBusinessObject(true);
                }
            }
            return value;
        } catch (Throwable e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getAttachmentIDs()
     */
    public Set<String> getAttachmentIDs() {
        return attachments.getContentIDSet();
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#getDataHandler(java.lang.String)
     */
    public DataHandler getDataHandler(String cid) {
        String bcid = getBlobCID(cid);
        return attachments.getDataHandler(bcid);
    }
    
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Message#removeDataHandler(java.lang.String)
     */
    public DataHandler removeDataHandler(String cid) {
        String bcid = getBlobCID(cid);
        DataHandler dh = attachments.getDataHandler(bcid);
        attachments.removeDataHandler(bcid);
        return dh;
    }
    
    private String getBlobCID(String cid) {
        String blobCID = cid;
        if (cid.startsWith("cid:")) {
            blobCID = cid.substring(4);  // Skip over cid:
        }
        return blobCID;
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
    
    public void addDataHandler(DataHandler dh, String id) {
        if (id.startsWith("<")  && id.endsWith(">")) {
            id = id.substring(1, id.length()-1);
        }
        attachments.addDataHandler(id, dh);
    }
    
    public Message getParent() {
        return null;
    }
    
    public void setParent(Message msg) { 
        // A Message does not have a parent
        throw new UnsupportedOperationException();
    }
    
    /**
     * @return true if the binding for this message indicates mtom
     */
    public boolean isMTOMEnabled() {
        return mtomEnabled;
    }
    
    /**
     * @param true if the binding for this message indicates mtom
     */
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
    
    public MessageContext getMessageContext() {
        return messageContext;
    }
    
    public void setMessageContext(MessageContext messageContext) {
        if (this.messageContext != messageContext) {
            // Copy attachments to the new map
            Attachments newMap = messageContext.getAxisMessageContext().getAttachmentMap();
            Attachments oldMap = attachments;
            for (String cid:oldMap.getAllContentIDs()) {
                DataHandler dh = oldMap.getDataHandler(cid);
                if (dh != null) {
                    newMap.addDataHandler(cid, dh);
                }
            }
            // If not MTOM and there are attachments, set SWA style
            if (!isMTOMEnabled()) {
                String[] cids = newMap.getAllContentIDs();
                if (cids.length > 0) {
                    Options opts = messageContext.getAxisMessageContext().getOptions();
                    opts.setProperty(Configuration.ENABLE_SWA, "true");
                }
            }
            if (log.isDebugEnabled()) {
                for (String cid:newMap.getAllContentIDs()) {
                    log.debug("Message has an attachment with content id= " + cid);
                }
            }
            attachments = newMap;
        }
        this.messageContext = messageContext;
    }
    
}
