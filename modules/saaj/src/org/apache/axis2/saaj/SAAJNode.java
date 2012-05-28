/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.saaj;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.dom.DOMMessageFormatter;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.impl.dom.SOAPBodyImpl;
import org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * A representation of a node (element) in a DOM representation of an XML document that provides
 * some tree manipulation methods. This interface provides methods for getting the value of a node,
 * for getting and setting the parent of a node, and for removing a node.
 */
public abstract class SAAJNode<T extends org.w3c.dom.Node> implements Node {
    protected final T target;
    protected SOAPElement parentElement;
    static final String SAAJ_NODE = "saaj.node";

    public SAAJNode(T target) {
        this.target = target;
    }

    public final T getTarget() {
        return target;
    }

    /**
     * Removes this <code>Node</code> object from the tree. Once removed, this node can be garbage
     * collected if there are no application references to it.
     */
    public void detachNode() {
        this.detach();
    }

    public OMNode detach() {
        parentElement = null;
        return null;
    }

    /**
     * Removes this <code>Node</code> object from the tree. Once removed, this node can be garbage
     * collected if there are no application references to it.
     */
    public SOAPElement getParentElement() {
        return this.parentElement;
    }

    public OMContainer getParent() {
        return (OMContainer)this.parentElement;
    }

    /* public OMNode getOMNode() {
        return omNode;
    }*/

    /**
     * Notifies the implementation that this <code>Node</code> object is no longer being used by the
     * application and that the implementation is free to reuse this object for nodes that may be
     * created later.
     * <p/>
     * Calling the method <code>recycleNode</code> implies that the method <code>detachNode</code>
     * has been called previously.
     */
    public void recycleNode() {
        // No corresponding implementation in OM
        // There is no implementation in Axis 1.2 also
    }

    /**
     * Sets the parent of this <code>Node</code> object to the given <code>SOAPElement</code>
     * object.
     *
     * @param parent the <code>SOAPElement</code> object to be set as the parent of this
     *               <code>Node</code> object
     * @throws SOAPException if there is a problem in setting the parent to the given element
     * @see #getParentElement() getParentElement()
     */
    public void setParentElement(SOAPElement parent) throws SOAPException {
        this.parentElement = parent;
    }

    public void setType(int nodeType) throws OMException {
        throw new UnsupportedOperationException("TODO");
    }

    public int getType() {
        return this.getNodeType();
    }

    public TypeInfo getSchemaTypeInfo() {
        // TODO - Fixme.
        throw new UnsupportedOperationException("TODO");
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        // TODO - Fixme.
        throw new UnsupportedOperationException("TODO");
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId)
            throws DOMException {
        // TODO - Fixme.
        throw new UnsupportedOperationException("TODO");
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        // TODO - Fixme.
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Converts or extracts the SAAJ node from the given DOM Node (domNode)
     *
     * @param domNode
     * @return the SAAJ Node corresponding to the domNode
     */
    javax.xml.soap.Node toSAAJNode(org.w3c.dom.Node domNode) {
        return toSAAJNode(domNode, this);
    }
    
    /**
     * Converts or extracts the SAAJ node from the given DOM Node (domNode)
     *
     * @param domNode
     * @return the SAAJ Node corresponding to the domNode
     */
    static javax.xml.soap.Node toSAAJNode(org.w3c.dom.Node domNode, Node parentNode) {
        if (domNode == null) {
            return null;
        }
        Node saajNode = (Node)((NodeImpl)domNode).getUserData(SAAJ_NODE);
        if (saajNode == null) {  // if SAAJ node has not been set in userData, try to construct it
            return toSAAJNode2(domNode, parentNode);
        }
        // update siblings for text nodes
        if (domNode instanceof org.w3c.dom.Text || domNode instanceof org.w3c.dom.Comment) {
            org.w3c.dom.Node prevSiblingDOMNode = domNode.getPreviousSibling();
            org.w3c.dom.Node nextSiblingDOMNode = domNode.getNextSibling();
            
            TextImplEx saajTextNode = (TextImplEx)saajNode;
            
            saajTextNode.setPreviousSibling(prevSiblingDOMNode);
            saajTextNode.setNextSibling(nextSiblingDOMNode);
        }
        return saajNode;
    }

    private static javax.xml.soap.Node toSAAJNode2(org.w3c.dom.Node domNode, Node parentNode) {
        if (domNode == null) {
            return null;
        }
        if (domNode instanceof org.w3c.dom.Text) {
            Text text = (Text)domNode;
            org.w3c.dom.Node prevSiblingDOMNode = text.getPreviousSibling();
            org.w3c.dom.Node nextSiblingDOMNode = text.getNextSibling();
            SOAPElementImpl parent = new SOAPElementImpl((ElementImpl)domNode.getParentNode());
            TextImplEx saajTextNode =
                    new TextImplEx(text.getData(), parent, prevSiblingDOMNode, nextSiblingDOMNode);
            ((NodeImpl)domNode).setUserData(SAAJ_NODE, saajTextNode, null);
            return saajTextNode;
        } else if (domNode instanceof org.w3c.dom.Comment) {
            Comment comment = (Comment)domNode;
            org.w3c.dom.Node prevSiblingDOMNode = comment.getPreviousSibling();
            org.w3c.dom.Node nextSiblingDOMNode = comment.getNextSibling();
            SOAPElementImpl parent = new SOAPElementImpl((ElementImpl)domNode.getParentNode());
            CommentImpl saajTextNode = new CommentImpl(comment.getData(),
                                                     parent, prevSiblingDOMNode,
                                                     nextSiblingDOMNode);
            ((NodeImpl)domNode).setUserData(SAAJ_NODE, saajTextNode, null);
            return saajTextNode;
        } else if (domNode instanceof org.apache.axiom.soap.impl.dom.SOAPBodyImpl) {
            org.apache.axiom.soap.impl.dom.SOAPBodyImpl doomSOAPBody = (SOAPBodyImpl)domNode;
            javax.xml.soap.SOAPBody saajSOAPBody =
                    new org.apache.axis2.saaj.SOAPBodyImpl(doomSOAPBody);
            doomSOAPBody.setUserData(SAAJ_NODE, saajSOAPBody, null);
            return saajSOAPBody;
        } else if (domNode instanceof org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl) {
            org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl doomSOAPEnv
                    = (SOAPEnvelopeImpl)domNode;
            javax.xml.soap.SOAPEnvelope saajEnvelope
                    = new org.apache.axis2.saaj.SOAPEnvelopeImpl(doomSOAPEnv);
            doomSOAPEnv.setUserData(SAAJ_NODE, saajEnvelope, null);
            return saajEnvelope;
        } else if (domNode instanceof org.apache.axiom.soap.impl.dom.SOAPFaultNodeImpl) {
            org.apache.axiom.soap.impl.dom.SOAPFaultNodeImpl doomSOAPFaultNode
                    = (org.apache.axiom.soap.impl.dom.SOAPFaultNodeImpl)domNode;
            javax.xml.soap.SOAPFaultElement saajSOAPFaultEle
                    = new org.apache.axis2.saaj.SOAPFaultElementImpl(doomSOAPFaultNode);
            doomSOAPFaultNode.setUserData(SAAJ_NODE, saajSOAPFaultEle, null);
            return saajSOAPFaultEle;
        } else if (domNode instanceof org.apache.axiom.soap.impl.dom.SOAPFaultDetailImpl) {
            org.apache.axiom.soap.impl.dom.SOAPFaultDetailImpl doomSOAPFaultDetail
                    = (org.apache.axiom.soap.impl.dom.SOAPFaultDetailImpl)domNode;
            javax.xml.soap.Detail saajDetail
                    = new org.apache.axis2.saaj.DetailImpl(doomSOAPFaultDetail);
            doomSOAPFaultDetail.setUserData(SAAJ_NODE, saajDetail, null);
            return saajDetail;
        } else if (domNode instanceof org.apache.axiom.soap.impl.dom.SOAPFaultImpl) {
            org.apache.axiom.soap.impl.dom.SOAPFaultImpl doomSOAPFault
                    = (org.apache.axiom.soap.impl.dom.SOAPFaultImpl)domNode;
            javax.xml.soap.SOAPFault saajSOAPFault
                    = new org.apache.axis2.saaj.SOAPFaultImpl(doomSOAPFault);
            doomSOAPFault.setUserData(SAAJ_NODE, saajSOAPFault, null);
            return saajSOAPFault;
        } else if (domNode instanceof org.apache.axiom.soap.impl.dom.SOAPHeaderBlockImpl) {
            org.apache.axiom.soap.impl.dom.SOAPHeaderBlockImpl doomSOAPHeaderBlock
                    = (org.apache.axiom.soap.impl.dom.SOAPHeaderBlockImpl)domNode;
            javax.xml.soap.SOAPHeaderElement saajSOAPHeaderEle
                    = new org.apache.axis2.saaj.SOAPHeaderElementImpl(doomSOAPHeaderBlock);
            doomSOAPHeaderBlock.setUserData(SAAJ_NODE, saajSOAPHeaderEle, null);
            return saajSOAPHeaderEle;
        } else if (domNode instanceof org.apache.axiom.soap.impl.dom.SOAPHeaderImpl) {
            org.apache.axiom.soap.impl.dom.SOAPHeaderImpl doomSOAPHeader
                    = (org.apache.axiom.soap.impl.dom.SOAPHeaderImpl)domNode;
            javax.xml.soap.SOAPHeader saajSOAPHeader
                    = new org.apache.axis2.saaj.SOAPHeaderImpl(doomSOAPHeader);
            doomSOAPHeader.setUserData(SAAJ_NODE, saajSOAPHeader, null);
            return saajSOAPHeader;
        } else if (domNode instanceof org.apache.axiom.om.impl.dom.DocumentImpl) {
            
            // Must be a SOAPEnvelope
            if (!(parentNode instanceof org.apache.axis2.saaj.SOAPEnvelopeImpl)) {
                return null;
            }
            org.apache.axiom.om.impl.dom.DocumentImpl doomDocument
                = (org.apache.axiom.om.impl.dom.DocumentImpl)domNode;
            org.apache.axis2.saaj.SOAPEnvelopeImpl saajEnv = 
                (org.apache.axis2.saaj.SOAPEnvelopeImpl) parentNode;
            javax.xml.soap.SOAPPart saajSOAPPart = null;
            if (saajEnv.getSOAPPartParent() != null) {
                // return existing SOAPPart
                saajSOAPPart = saajEnv.getSOAPPartParent();
                
            } else {
                // Create Message and SOAPPart
                SOAPMessageImpl saajSOAPMessage = 
                        new SOAPMessageImpl(saajEnv);
                saajSOAPPart = saajSOAPMessage.getSOAPPart();
            }
            
            domNode.setUserData(SAAJ_NODE, saajSOAPPart, null);
            return saajSOAPPart;
        } else { // instanceof org.apache.axis2.om.impl.dom.ElementImpl
            ElementImpl doomElement = (ElementImpl)domNode;
            SOAPElementImpl saajSOAPElement = new SOAPElementImpl(doomElement);
            doomElement.setUserData(SAAJ_NODE, saajSOAPElement, null);
            return saajSOAPElement;
        }
    }
    
    // TODO: the existence of this method probably indicates a problem in TextImplEx
    public org.w3c.dom.Node getParentNode() {
        return null;
    }

    public final boolean hasAttributes() {
        return parentElement.hasAttributes();
    }

    public final boolean isSupported(String feature, String version) {
        return parentElement.isSupported(feature, version);
    }

    public final String getBaseURI() {
        return parentElement.getBaseURI();
    }

    public final String getNodeValue() throws DOMException {
        return target.getNodeValue();
    }

    public final void setNodeValue(String nodeValue) throws DOMException {
        target.setNodeValue(nodeValue);
    }

    public final org.w3c.dom.Node insertBefore(org.w3c.dom.Node newChild, org.w3c.dom.Node refChild) throws DOMException {
        return target.insertBefore(newChild, refChild);
    }

    public final org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild, org.w3c.dom.Node oldChild) throws DOMException {
        return target.replaceChild(newChild, oldChild);
    }

    public final org.w3c.dom.Node cloneNode(boolean deep) {
        return target.cloneNode(deep);
    }

    public final void normalize() {
        target.normalize();
    }

    public final void setPrefix(String prefix) throws DOMException {
        target.setPrefix(prefix);
    }

    public final short compareDocumentPosition(org.w3c.dom.Node other) throws DOMException {
        return target.compareDocumentPosition(other);
    }

    public final void setTextContent(String textContent) throws DOMException {
        target.setTextContent(textContent);
    }

    public final boolean isSameNode(org.w3c.dom.Node other) {
        return target.isSameNode(other);
    }

    public final String lookupPrefix(String namespaceURI) {
        return target.lookupPrefix(namespaceURI);
    }

    public final boolean isDefaultNamespace(String namespaceURI) {
        return target.isDefaultNamespace(namespaceURI);
    }

    public final String lookupNamespaceURI(String prefix) {
        return null;
    }

    public final boolean isEqualNode(org.w3c.dom.Node arg) {
        return target.isEqualNode(arg);
    }

    public final Object getFeature(String feature, String version) {
        return target.getFeature(feature, version);
    }

    public final Object setUserData(String key, Object data, UserDataHandler handler) {
        return target.setUserData(key, data, handler);
    }

    public final Object getUserData(String key) {
        return target.getUserData(key);
    }

    public final org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild) throws DOMException {
        if (oldChild instanceof SAAJNode) {
            oldChild = ((SAAJNode<?>)oldChild).getTarget();
        }
        return target.removeChild(oldChild);
    }

    public final String getNodeName() {
        return target.getNodeName();
    }

    public final short getNodeType() {
        return parentElement.getNodeType();
    }

    public final Document getOwnerDocument() {
        return target.getOwnerDocument();
    }

    public final String getLocalName() {
        return target.getLocalName();
    }

    public final String getNamespaceURI() {
        return target.getNamespaceURI();
    }

    public final String getPrefix() {
        return target.getPrefix();
    }

    public final org.w3c.dom.Node getFirstChild() {
        return toSAAJNode(target.getFirstChild());
    }

    public final boolean hasChildNodes() {
        return target.hasChildNodes();
    }

    public final org.w3c.dom.Node getLastChild() {
        return toSAAJNode(target.getLastChild());
    }

    protected final NodeList toSAAJNodeList(NodeList nodes) {
        NodeListImpl result = new NodeListImpl();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.addNode(toSAAJNode(nodes.item(i)));
        }
        return result;
    }

    public final NodeList getChildNodes() {
        return toSAAJNodeList(target.getChildNodes());
    }

    public final org.w3c.dom.Node appendChild(org.w3c.dom.Node child) throws DOMException {        
        if (getOwnerDocument() != child.getOwnerDocument()) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Wrong document");
        }
        try {
            if (child instanceof Text) {
                return appendText((Text)child);
            } else if (child instanceof ElementImpl) {
                return appendElement((ElementImpl)child);
            }
        } catch (SOAPException e) {
            DOMException ex = 
                new DOMException(DOMException.HIERARCHY_REQUEST_ERR, e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        DOMException.HIERARCHY_REQUEST_ERR, null));
    }

    protected Text appendText(Text child) throws SOAPException {
        String text = child.getData();
        Text textNode = getOwnerDocument().createTextNode(text);
        NodeImpl node = ((NodeImpl)target.appendChild(textNode));
        TextImplEx saajTextNode = new TextImplEx(text, (SOAPElement)this);
        node.setUserData(SAAJ_NODE, saajTextNode, null);
        return saajTextNode;
    }
    
    protected Element appendElement(ElementImpl child) throws SOAPException {
        String namespaceURI = child.getNamespaceURI();
        String prefix = child.getPrefix();

        SOAPElementImpl childEle = new SOAPElementImpl(child);
        
        childEle.target.setUserData(SAAJ_NODE, childEle, null);
        if (namespaceURI != null && namespaceURI.trim().length() > 0) {
            childEle.target.setNamespace(childEle.target.declareNamespace(namespaceURI, prefix));
        }
        target.appendChild(childEle.target);
        ((NodeImpl)childEle.target.getParentNode()).setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement((SOAPElement)this);
        return childEle;
    }

    public final String getTextContent() throws DOMException {
        return target.getTextContent();
    }

    public final NamedNodeMap getAttributes() {
        return target.getAttributes();
    }
}
