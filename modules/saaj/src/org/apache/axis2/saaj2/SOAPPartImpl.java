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
package org.apache.axis2.saaj2;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Source;

import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.saaj.SOAPEnvelopeImpl;
import org.apache.axis2.soap.impl.dom.factory.DOMSOAPFactory;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.SessionUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class SOAPPartImpl extends SOAPPart {
	
	Document document = new DocumentImpl();
	SOAPMessageImpl msgObject;
	MimeHeadersEx mimeHeaders = new MimeHeadersEx();
	private Object envelope;
	

	public SOAPPartImpl(SOAPMessageImpl parent, Object initialContents)  throws SOAPException {
        setMimeHeader(HTTPConstants.HEADER_CONTENT_ID,
                SessionUtils.generateSessionId());
        setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/xml");
        
        StAXSOAPModelBuilder stAXSOAPModelBuilder;

        msgObject = parent;
        try {
            if (initialContents instanceof SOAPEnvelope) {
                ((SOAPEnvelopeImpl) initialContents).setOwnerDocument(this);
                envelope = initialContents;
            }else if (initialContents instanceof InputStream) {
                //XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader((InputStream)initialContents);
                InputStreamReader inr = new InputStreamReader(
                        (InputStream) initialContents);
                
                stAXSOAPModelBuilder =
                    new StAXSOAPModelBuilder(XMLInputFactory.newInstance()
                            .createXMLStreamReader(inr), new DOMSOAPFactory(), null);
                org.apache.axis2.soap.SOAPEnvelope omEnv = stAXSOAPModelBuilder.getSOAPEnvelope();
                ///TODO ave to complete SOAP Env
            }
        } catch (Exception e) {
            throw new SOAPException(e);
        }
	}
	
	
	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#getEnvelope()
	 */
	public SOAPEnvelope getEnvelope() throws SOAPException {
		return (SOAPEnvelope)this.envelope;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#removeMimeHeader(java.lang.String)
	 */
	public void removeMimeHeader(String header) {
		this.mimeHeaders.removeHeader(header);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#removeAllMimeHeaders()
	 */
	public void removeAllMimeHeaders() {
		this.mimeHeaders.removeAllHeaders();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#getMimeHeader(java.lang.String)
	 */
	public String[] getMimeHeader(String name) {
		return this.mimeHeaders.getHeader(name);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#setMimeHeader(java.lang.String, java.lang.String)
	 */
	public void setMimeHeader(String name, String value) {
		this.mimeHeaders.setHeader(name, value);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#addMimeHeader(java.lang.String, java.lang.String)
	 */
	public void addMimeHeader(String header, String value) {
		this.mimeHeaders.addHeader(header, value);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#getAllMimeHeaders()
	 */
	public Iterator getAllMimeHeaders() {
		return this.mimeHeaders.getAllHeaders();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#getMatchingMimeHeaders(java.lang.String[])
	 */
	public Iterator getMatchingMimeHeaders(String[] names) {
		return this.mimeHeaders.getMatchingHeaders(names);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#getNonMatchingMimeHeaders(java.lang.String[])
	 */
	public Iterator getNonMatchingMimeHeaders(String[] names) {
		return this.mimeHeaders.getNonMatchingHeaders(names);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#setContent(javax.xml.transform.Source)
	 */
	public void setContent(Source source) throws SOAPException {
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPPart#getContent()
	 */
	public Source getContent() throws SOAPException {
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#getDoctype()
	 */
	public DocumentType getDoctype() {
		return this.document.getDoctype();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#getImplementation()
	 */
	public DOMImplementation getImplementation() {
		return this.document.getImplementation();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#getDocumentElement()
	 */
	public Element getDocumentElement() {
		return this.document.getDocumentElement();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createElement(java.lang.String)
	 */
	public Element createElement(String arg0) throws DOMException {
		return this.document.createElement(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createDocumentFragment()
	 */
	public DocumentFragment createDocumentFragment() {
		return this.document.createDocumentFragment();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createTextNode(java.lang.String)
	 */
	public Text createTextNode(String arg0) {
		return this.document.createTextNode(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createComment(java.lang.String)
	 */
	public Comment createComment(String arg0) {
		return this.document.createComment(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createCDATASection(java.lang.String)
	 */
	public CDATASection createCDATASection(String arg0) throws DOMException {
		return this.document.createCDATASection(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createProcessingInstruction(java.lang.String, java.lang.String)
	 */
	public ProcessingInstruction createProcessingInstruction(String arg0, String arg1) throws DOMException {
		return this.document.createProcessingInstruction(arg0,arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createAttribute(java.lang.String)
	 */
	public Attr createAttribute(String arg0) throws DOMException {
		return this.document.createAttribute(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createEntityReference(java.lang.String)
	 */
	public EntityReference createEntityReference(String arg0) throws DOMException {
		return this.document.createEntityReference(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#getElementsByTagName(java.lang.String)
	 */
	public NodeList getElementsByTagName(String arg0) {
		return this.document.getElementsByTagName(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#importNode(org.w3c.dom.Node, boolean)
	 */
	public Node importNode(Node arg0, boolean arg1) throws DOMException {
		return this.document.importNode(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createElementNS(java.lang.String, java.lang.String)
	 */
	public Element createElementNS(String arg0, String arg1) throws DOMException {
		return this.document.createElementNS(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#createAttributeNS(java.lang.String, java.lang.String)
	 */
	public Attr createAttributeNS(String arg0, String arg1) throws DOMException {
		return this.document.createAttributeNS(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#getElementsByTagNameNS(java.lang.String, java.lang.String)
	 */
	public NodeList getElementsByTagNameNS(String arg0, String arg1) {
		return this.document.getElementsByTagNameNS(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Document#getElementById(java.lang.String)
	 */
	public Element getElementById(String arg0) {
		return this.document.getElementById(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	public String getNodeName() {
		return this.document.getNodeName();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeValue()
	 */
	public String getNodeValue() throws DOMException {
		return this.document.getNodeValue();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
	 */
	public void setNodeValue(String arg0) throws DOMException {
		this.document.setNodeValue(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	public short getNodeType() {
		return this.document.getNodeType();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getParentNode()
	 */
	public Node getParentNode() {
		return this.getParentNode();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getChildNodes()
	 */
	public NodeList getChildNodes() {
		return this.document.getChildNodes();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getFirstChild()
	 */
	public Node getFirstChild() {
		return this.document.getFirstChild();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getLastChild()
	 */
	public Node getLastChild() {
		return this.document.getLastChild();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getPreviousSibling()
	 */
	public Node getPreviousSibling() {
		return this.document.getPreviousSibling();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNextSibling()
	 */
	public Node getNextSibling() {
		return this.document.getNextSibling();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getAttributes()
	 */
	public NamedNodeMap getAttributes() {
		return this.document.getAttributes();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getOwnerDocument()
	 */
	public Document getOwnerDocument() {
		return this.document.getOwnerDocument();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	public Node insertBefore(Node arg0, Node arg1) throws DOMException {
		return this.document.insertBefore(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	public Node replaceChild(Node arg0, Node arg1) throws DOMException {
		return this.document.replaceChild(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
	 */
	public Node removeChild(Node arg0) throws DOMException {
		return this.document.removeChild(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
	 */
	public Node appendChild(Node arg0) throws DOMException {
		return this.document.appendChild(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#hasChildNodes()
	 */
	public boolean hasChildNodes() {
		return this.document.hasChildNodes();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#cloneNode(boolean)
	 */
	public Node cloneNode(boolean arg0) {
		return this.document.cloneNode(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#normalize()
	 */
	public void normalize() {
		this.document.normalize();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
	 */
	public boolean isSupported(String arg0, String arg1) {
		return this.document.isSupported(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNamespaceURI()
	 */
	public String getNamespaceURI() {
		return this.document.getNamespaceURI();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getPrefix()
	 */
	public String getPrefix() {
		return this.document.getPrefix();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#setPrefix(java.lang.String)
	 */
	public void setPrefix(String arg0) throws DOMException {
		this.document.setPrefix(arg0);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getLocalName()
	 */
	public String getLocalName() {
		return this.document.getLocalName();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#hasAttributes()
	 */
	public boolean hasAttributes() {
		return this.document.hasAttributes();
	}
	
	protected void setMessage(SOAPMessageImpl message) {
		this.msgObject = message;
	}

}
