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

import org.apache.axis2.util.XMLUtils;
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

/**
 * @author Ashutosh Shahi	ashutosh.shahi@gmail.com
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class SOAPDocumentImpl implements Document {

    // Depending on the user's parser preference
    protected Document delegate = null;
    protected SOAPPartImpl soapPart = null;

    /**
     * Construct the Document
     * 
     * @param sp the soap part
     */
    public SOAPDocumentImpl(SOAPPartImpl sp) {
        try {
            delegate = XMLUtils.newDocument();
        } catch (ParserConfigurationException e) {
            // Do nothing
        }
        soapPart = sp;
    }

    public DOMImplementation getImplementation() {
        return delegate.getImplementation();
    }

    /**
     * Creates an empty <code>DocumentFragment</code> object. @todo not
     * implemented yet
     *
     * @return A new <code>DocumentFragment</code>.
     */
    public DocumentFragment createDocumentFragment() {
        return delegate.createDocumentFragment();
    }

    /**
     * @return
     * @todo : link with SOAP
     */
    public DocumentType getDoctype() {
        return delegate.getDoctype();
    }

    /**
     * should not be called, the method will be handled in SOAPPart
     *
     * @return
     */
    public Element getDocumentElement() {
        return soapPart.getDocumentElement();
    }

    /**
     * @todo: How Axis will maintain the Attribute representation ?
     */
    public Attr createAttribute(String name) throws DOMException {
        return delegate.createAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createCDATASection(java.lang.String)
     */
    public CDATASection createCDATASection(String arg0) throws DOMException {
        // Not implementing this one, as it may not be supported in om
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createComment(java.lang.String)
     */
    public Comment createComment(String arg0) {
        //Not implementing this one, as it may not be supported in om
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createElement(java.lang.String)
     */
    public Element createElement(String arg0) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Element getElementById(String elementId) {
        return delegate.getElementById(elementId);
    }


    /**
     * @param name
     * @return @throws
     *         DOMException
     */
    public EntityReference createEntityReference(String name)
            throws DOMException {
        throw new java.lang.UnsupportedOperationException(
                "createEntityReference");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#importNode(org.w3c.dom.Node, boolean)
     */
    public Node importNode(Node arg0, boolean arg1) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getElementsByTagName(java.lang.String)
     */
    public NodeList getElementsByTagName(String localName) {
        try {
            NodeListImpl list = new NodeListImpl();
            if (soapPart != null) {
                SOAPEnvelopeImpl soapEnv = (SOAPEnvelopeImpl) soapPart.getEnvelope();
                SOAPHeaderImpl header = (SOAPHeaderImpl) soapEnv.getHeader();
                if (header != null) {
                    list.addNodeList(header.getElementsByTagName(localName));
                }
                SOAPBodyImpl body = (SOAPBodyImpl) soapEnv.getBody();
                if (body != null) {
                    list.addNodeList(body.getElementsByTagName(localName));
                }
            }
            return list;
        } catch (SOAPException se) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "");
        }
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createTextNode(java.lang.String)
     */
    public Text createTextNode(String data) {
        TextImpl me = new TextImpl(delegate.createTextNode(data));
        me.setOwnerDocument(soapPart);
        return me;
    }

    /**
     * Attribute is not particularly dealt with in SAAJ.
     */
    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        return delegate.createAttributeNS(namespaceURI, qualifiedName);
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createElementNS(java.lang.String, java.lang.String)
     */
    public Element createElementNS(String arg0, String arg1)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getElementsByTagNameNS(java.lang.String, java.lang.String)
     */
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        try {
            NodeListImpl list = new NodeListImpl();
            if (soapPart != null) {
                SOAPEnvelopeImpl soapEnv = (SOAPEnvelopeImpl) soapPart.getEnvelope();
                SOAPHeaderImpl header = (SOAPHeaderImpl) soapEnv.getHeader();
                if (header != null) {
                    list.addNodeList(
                            header.getElementsByTagNameNS(namespaceURI,
                                    localName));
                }
                SOAPBodyImpl body = (SOAPBodyImpl) soapEnv.getBody();
                if (body != null) {
                    list.addNodeList(
                            body.getElementsByTagNameNS(namespaceURI,
                                    localName));
                }
            }
            return list;
        } catch (SOAPException se) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "");
        }
    }


    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data)
            throws DOMException {
        throw new java.lang.UnsupportedOperationException(
                "createProcessingInstruction");
    }


    /**
     * override it in sub-classes
     *
     * @return
     */
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }


    public void normalize() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }


    public boolean hasAttributes() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }

    public boolean hasChildNodes() {
        try {
            if (soapPart != null) {
                if (soapPart.getEnvelope() != null) {
                    return true;
                }
            }
            return false;
        } catch (SOAPException se) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "");
        }
    }

    public String getLocalName() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }

    public String getNamespaceURI() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }


    public String getNodeName() {
        return null;
    }

    public String getNodeValue() throws DOMException {
        throw new DOMException(DOMException.NO_DATA_ALLOWED_ERR,
                "Cannot use TextNode.get in " + this);
    }

    public String getPrefix() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        throw new DOMException(DOMException.NO_DATA_ALLOWED_ERR,
                "Cannot use TextNode.set in " + this);
    }


    public void setPrefix(String prefix) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }

    /**
     * we have to have a link to them...
     */
    public Document getOwnerDocument() {
        return null;
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public Node getFirstChild() {
        try {
            if (soapPart != null)
                return (org.apache.axis2.saaj.SOAPEnvelopeImpl) soapPart
                        .getEnvelope();
            else
                return null;
        } catch (SOAPException se) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "");
        }
    }

    public Node getLastChild() {
        try {
            if (soapPart != null)
                return (org.apache.axis2.saaj.SOAPEnvelopeImpl) soapPart
                        .getEnvelope();
            else
                return null;
        } catch (SOAPException se) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "");
        }
    }

    public Node getNextSibling() {

        return null;
    }

    public Node getParentNode() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node cloneNode(boolean deep) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }


    public NodeList getChildNodes() {
        try {
            if (soapPart != null) {
                NodeListImpl children = new NodeListImpl();
                children.addNode(soapPart.getEnvelope());
                return children;
            } else {
                return NodeListImpl.EMPTY_NODELIST;
            }
        } catch (SOAPException se) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "");
        }

    }

    // fill appropriate features
    private String[] features = {"foo", "bar"};
    private String version = "version 2.0";

    public boolean isSupported(String feature, String version) {
        if (!version.equalsIgnoreCase(version))
            return false;
        else
            return true;
    }

    public Node appendChild(Node newChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }

    public Node removeChild(Node oldChild) throws DOMException {
        try {
            Node envNode;
            if (soapPart != null) {
                envNode = soapPart.getEnvelope();
                if (envNode.equals(oldChild)) {
                    return envNode;
                }
            }
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
        } catch (SOAPException se) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "");
        }
    }

    public Node insertBefore(Node newChild, Node refChild)
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }

    public Node replaceChild(Node newChild, Node oldChild)
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
    }


}
