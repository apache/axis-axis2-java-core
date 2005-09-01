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
package org.apache.axis2.saaj;

import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.SessionUtils;
import org.w3c.dom.*;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class SOAPPartImpl extends SOAPPart {

    private SOAPMessageImpl msgObject;
    private MimeHeaders mimeHeaders = new MimeHeaders();
    private Object envelope;
    /**
     * default message encoding charset
     */
    private String currentEncoding = "UTF-8";

    public SOAPPartImpl(SOAPMessageImpl parent,
                        Object initialContents,
                        boolean isBodyStream) throws SOAPException {

        setMimeHeader(HTTPConstants.HEADER_CONTENT_ID,
                SessionUtils.generateSessionId());
        setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/xml");
        StAXSOAPModelBuilder stAXSOAPModelBuilder;

        msgObject = parent;
        try {
            if (initialContents instanceof SOAPEnvelope) {
                ((SOAPEnvelopeImpl) initialContents).setOwnerDocument(this);
                envelope = initialContents;
            } else if (initialContents instanceof InputStream) {
                //XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader((InputStream)initialContents);
                InputStreamReader inr = new InputStreamReader(
                        (InputStream) initialContents);
                stAXSOAPModelBuilder =
                        new StAXSOAPModelBuilder(
                                XMLInputFactory.newInstance()
                        .createXMLStreamReader(inr), null);
                org.apache.axis2.soap.SOAPEnvelope omEnv = stAXSOAPModelBuilder.getSOAPEnvelope();
                envelope = new SOAPEnvelopeImpl(omEnv);
                ((SOAPEnvelopeImpl) envelope).setOwnerDocument(this);
            }

        } catch (Exception e) {
        	//e.printStackTrace();
            throw new SOAPException(e);
        }
    }

    public SOAPMessageImpl getMessage() {
        return msgObject;
    }

    /**
     * Set the Message for this Part.
     * Do not call this Directly. Called by Message.
     *
     * @param msg the <code>Message</code> for this part
     */
    public void setMessage(SOAPMessageImpl msg) {
        this.msgObject = msg;
    }

    /**
     * @see javax.xml.soap.SOAPPart#getEnvelope()
     */
    public SOAPEnvelope getEnvelope() throws SOAPException {
        //if(envelope != null)
        return (SOAPEnvelope) envelope;

    }

    /**
     * Removes all MIME headers that match the given name.
     *
     * @param header a <CODE>String</CODE> giving
     *               the name of the MIME header(s) to be removed
     */
    public void removeMimeHeader(String header) {
        mimeHeaders.removeHeader(header);
    }

    /**
     * Removes all the <CODE>MimeHeader</CODE> objects for this
     * <CODE>SOAPEnvelope</CODE> object.
     */
    public void removeAllMimeHeaders() {
        mimeHeaders.removeAllHeaders();
    }

    /**
     * Gets all the values of the <CODE>MimeHeader</CODE> object
     * in this <CODE>SOAPPart</CODE> object that is identified by
     * the given <CODE>String</CODE>.
     *
     * @param name the name of the header; example:
     *             "Content-Type"
     * @return a <CODE>String</CODE> array giving all the values for
     *         the specified header
     * @see #setMimeHeader(java.lang.String, java.lang.String) setMimeHeader(java.lang.String, java.lang.String)
     */
    public String[] getMimeHeader(String name) {
        return mimeHeaders.getHeader(name);
    }

    /**
     * Changes the first header entry that matches the given
     * header name so that its value is the given value, adding a
     * new header with the given name and value if no existing
     * header is a match. If there is a match, this method clears
     * all existing values for the first header that matches and
     * sets the given value instead. If more than one header has
     * the given name, this method removes all of the matching
     * headers after the first one.
     * <p/>
     * <P>Note that RFC822 headers can contain only US-ASCII
     * characters.</P>
     *
     * @param name  a <CODE>String</CODE> giving the
     *              header name for which to search
     * @param value a <CODE>String</CODE> giving the
     *              value to be set. This value will be substituted for the
     *              current value(s) of the first header that is a match if
     *              there is one. If there is no match, this value will be
     *              the value for a new <CODE>MimeHeader</CODE> object.
     * @ throws java.lang.IllegalArgumentException if
     * there was a problem with the specified mime header name
     * or value
     * @see #getMimeHeader(java.lang.String) getMimeHeader(java.lang.String)
     */
    public void setMimeHeader(String name, String value) {
        mimeHeaders.setHeader(name, value);
    }

    /**
     * Add the specified MIME header, as per JAXM.
     *
     * @param header the header to add
     * @param value  the value of that header
     */
    public void addMimeHeader(String header, String value) {
        mimeHeaders.addHeader(header, value);
    }

    /**
     * Retrieves all the headers for this <CODE>SOAPPart</CODE>
     * object as an iterator over the <CODE>MimeHeader</CODE>
     * objects.
     *
     * @return an <CODE>Iterator</CODE> object with all of the Mime
     *         headers for this <CODE>SOAPPart</CODE> object
     */
    public Iterator getAllMimeHeaders() {
        return mimeHeaders.getAllHeaders();
    }

    /**
     * Get all headers that match.
     *
     * @param match an array of <code>String</code>s giving mime header names
     * @return an <code>Iterator</code> over all values matching these headers
     */
    public java.util.Iterator getMatchingMimeHeaders(final String[] match) {
        return mimeHeaders.getMatchingHeaders(match);
    }

    /**
     * Get all headers that do not match.
     *
     * @param match an array of <code>String</code>s giving mime header names
     * @return an <code>Iterator</code> over all values not matching these
     *         headers
     */
    public java.util.Iterator getNonMatchingMimeHeaders(final String[] match) {
        return mimeHeaders.getNonMatchingHeaders(match);
    }

    /* (non-Javadoc)
     * @see javax.xml.soap.SOAPPart#setContent(javax.xml.transform.Source)
     */
    public void setContent(Source source) throws SOAPException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.xml.soap.SOAPPart#getContent()
     */
    public Source getContent() throws SOAPException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Implementation of org.w3c.Document
     * Most of methods will be implemented using the delgate
     * instance of SOAPDocumentImpl
     * This is for two reasons:
     * - possible change of message classes, by extenstion of xerces implementation
     * - we cannot extends SOAPPart (multiple inheritance),
     * since it is defined as Abstract class
     * ***********************************************************
     */

    private Document document = new SOAPDocumentImpl(this);

    /**
     * @since SAAJ 1.2
     */
    public Document getSOAPDocument() {
        if (document == null) {
            document = new SOAPDocumentImpl(this);
        }
        return document;
    }

    /**
     * @return
     */
    public DocumentType getDoctype() {
        return document.getDoctype();
    }

    /**
     * @return
     */
    public DOMImplementation getImplementation() {
        return document.getImplementation();
    }

    /**
     * SOAPEnvelope is the Document Elements of this XML document
     */
    protected Document mDocument;

    public Element getDocumentElement() {
        try {
            return getEnvelope();
        } catch (SOAPException se) {
            return null;
        }
    }

    /**
     * @param tagName
     * @return
     * @throws DOMException
     */
    public Element createElement(String tagName) throws DOMException {
        return document.createElement(tagName);
    }

    public DocumentFragment createDocumentFragment() {
        return document.createDocumentFragment();
    }

    public Text createTextNode(String data) {
        return document.createTextNode(data);
    }

    public Comment createComment(String data) {
        return document.createComment(data);
    }

    public CDATASection createCDATASection(String data) throws DOMException {
        return document.createCDATASection(data);
    }

    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data)
            throws DOMException {
        return document.createProcessingInstruction(target, data);
    }

    public Attr createAttribute(String name) throws DOMException {
        return document.createAttribute(name);
    }

    public EntityReference createEntityReference(String name) throws DOMException {
        return document.createEntityReference(name);
    }

    public NodeList getElementsByTagName(String tagname) {
        return document.getElementsByTagName(tagname);
    }

    public Node importNode(Node importedNode, boolean deep)
            throws DOMException {
        return document.importNode(importedNode, deep);
    }

    public Element createElementNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        return document.createElementNS(namespaceURI, qualifiedName);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        return document.createAttributeNS(namespaceURI, qualifiedName);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        return document.getElementsByTagNameNS(namespaceURI, localName);
    }

    public Element getElementById(String elementId) {
        return document.getElementById(elementId);
    }

    /////////////////////////////////////////////////////////////

    public String getEncoding() {
        return currentEncoding;
    }

    public void setEncoding(String s) {
        currentEncoding = s;
    }

    public boolean getStandalone() {
        throw new UnsupportedOperationException("Not yet implemented.71");
    }


    public void setStandalone(boolean flag) {
        throw new UnsupportedOperationException("Not yet implemented.72");
    }

    public boolean getStrictErrorChecking() {
        throw new UnsupportedOperationException("Not yet implemented.73");
    }


    public void setStrictErrorChecking(boolean flag) {
        throw new UnsupportedOperationException("Not yet implemented. 74");
    }


    public String getVersion() {
        throw new UnsupportedOperationException("Not yet implemented. 75");
    }


    public void setVersion(String s) {
        throw new UnsupportedOperationException("Not yet implemented.76");
    }


    public Node adoptNode(Node node)
            throws DOMException {
        throw new UnsupportedOperationException("Not yet implemented.77");
    }

    /**
     * Node Implementation
     */

    public String getNodeName() {
        return document.getNodeName();
    }

    public String getNodeValue() throws DOMException {
        return document.getNodeValue();
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        document.setNodeValue(nodeValue);
    }

    public short getNodeType() {
        return document.getNodeType();
    }

    public Node getParentNode() {
        return document.getParentNode();
    }

    public NodeList getChildNodes() {
        return document.getChildNodes();
    }

    public Node getFirstChild() {
        return document.getFirstChild();
    }

    public Node getLastChild() {
        return document.getLastChild();
    }

    public Node getPreviousSibling() {
        return document.getPreviousSibling();
    }

    public Node getNextSibling() {
        return document.getNextSibling();
    }

    public NamedNodeMap getAttributes() {
        return document.getAttributes();
    }

    public Document getOwnerDocument() {
        return document.getOwnerDocument();
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return document.insertBefore(newChild, refChild);
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return document.replaceChild(newChild, oldChild);
    }

    public Node removeChild(Node oldChild) throws DOMException {
        return document.removeChild(oldChild);
    }

    public Node appendChild(Node newChild) throws DOMException {
        return document.appendChild(newChild);
    }

    public boolean hasChildNodes() {
        return document.hasChildNodes();
    }

    public Node cloneNode(boolean deep) {
        return document.cloneNode(deep);
    }

    public void normalize() {
        document.normalize();
    }

    public boolean isSupported(String feature, String version) {
        return document.isSupported(feature, version);
    }

    public String getNamespaceURI() {
        return document.getNamespaceURI();
    }

    public String getPrefix() {
        return document.getPrefix();
    }

    public void setPrefix(String prefix) throws DOMException {
        document.setPrefix(prefix);
    }

    public String getLocalName() {
        return document.getLocalName();
    }

    public boolean hasAttributes() {
        return document.hasAttributes();
    }
    
}
