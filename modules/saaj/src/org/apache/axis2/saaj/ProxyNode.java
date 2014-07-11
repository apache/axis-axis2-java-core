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

import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMInformationItem;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * A representation of a node (element) in a DOM representation of an XML document that provides
 * some tree manipulation methods. This interface provides methods for getting the value of a node,
 * for getting and setting the parent of a node, and for removing a node.
 */
public abstract class ProxyNode<T extends org.w3c.dom.Node, S extends OMInformationItem> implements Node {
    protected final T target;
    protected final S omTarget;
    static final String SAAJ_NODE = "saaj.node";

    public ProxyNode(T target, S omTarget) {
        this.target = target;
        this.omTarget = omTarget;
        target.setUserData(SAAJ_NODE, this, null);
    }

    public final T getTarget() {
        return target;
    }

    public final S getOMTarget() {
        return omTarget;
    }

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
    Node toSAAJNode(Node domNode) {
        return toSAAJNode(domNode, this);
    }
    
    /**
     * Converts or extracts the SAAJ node from the given DOM Node (domNode)
     *
     * @param domNode
     * @return the SAAJ Node corresponding to the domNode
     */
    static Node toSAAJNode(Node domNode, Node parentNode) {
        if (domNode == null) {
            return null;
        }
        Node saajNode = (Node)domNode.getUserData(SAAJ_NODE);
        if (saajNode == null) {  // if SAAJ node has not been set in userData, try to construct it
            return toSAAJNode2(domNode, parentNode);
        }
        return saajNode;
    }

    private static Node toSAAJNode2(Node domNode, Node parentNode) {
        if (domNode == null) {
            return null;
        }
        if (domNode instanceof org.w3c.dom.Text) {
            return new TextImplEx((OMText)domNode);
        } else if (domNode instanceof org.w3c.dom.Comment) {
            return new CommentImpl((OMComment)domNode);
        } else if (domNode instanceof SOAPBody) {
            javax.xml.soap.SOAPBody saajSOAPBody =
                    new org.apache.axis2.saaj.SOAPBodyImpl((SOAPBody)domNode);
            domNode.setUserData(SAAJ_NODE, saajSOAPBody, null);
            return saajSOAPBody;
        } else if (domNode instanceof SOAPEnvelope) {
            javax.xml.soap.SOAPEnvelope saajEnvelope
                    = new org.apache.axis2.saaj.SOAPEnvelopeImpl((SOAPEnvelope)domNode);
            domNode.setUserData(SAAJ_NODE, saajEnvelope, null);
            return saajEnvelope;
        } else if (domNode instanceof SOAPFaultNode) {
            javax.xml.soap.SOAPFaultElement saajSOAPFaultEle
                    = new org.apache.axis2.saaj.SOAPFaultElementImpl<SOAPFaultNode>((SOAPFaultNode)domNode);
            domNode.setUserData(SAAJ_NODE, saajSOAPFaultEle, null);
            return saajSOAPFaultEle;
        } else if (domNode instanceof SOAPFaultDetail) {
            javax.xml.soap.Detail saajDetail
                    = new org.apache.axis2.saaj.DetailImpl((SOAPFaultDetail)domNode);
            domNode.setUserData(SAAJ_NODE, saajDetail, null);
            return saajDetail;
        } else if (domNode instanceof SOAPFault) {
            javax.xml.soap.SOAPFault saajSOAPFault
                    = new org.apache.axis2.saaj.SOAPFaultImpl((SOAPFault)domNode);
            domNode.setUserData(SAAJ_NODE, saajSOAPFault, null);
            return saajSOAPFault;
        } else if (domNode instanceof SOAPHeaderBlock) {
            javax.xml.soap.SOAPHeaderElement saajSOAPHeaderEle
                    = new org.apache.axis2.saaj.SOAPHeaderElementImpl((SOAPHeaderBlock)domNode);
            domNode.setUserData(SAAJ_NODE, saajSOAPHeaderEle, null);
            return saajSOAPHeaderEle;
        } else if (domNode instanceof SOAPHeader) {
            javax.xml.soap.SOAPHeader saajSOAPHeader
                    = new org.apache.axis2.saaj.SOAPHeaderImpl((SOAPHeader)domNode);
            domNode.setUserData(SAAJ_NODE, saajSOAPHeader, null);
            return saajSOAPHeader;
        } else if (domNode instanceof Document) {
            return new SAAJDocument((OMDocument)domNode);
        } else { // instanceof org.apache.axis2.om.impl.dom.ElementImpl
            SOAPElementImpl<OMElement> saajSOAPElement = new SOAPElementImpl<OMElement>((OMElement)domNode);
            domNode.setUserData(SAAJ_NODE, saajSOAPElement, null);
            return saajSOAPElement;
        }
    }
    
    public org.w3c.dom.Node getParentNode() {
        return toSAAJNode(target.getParentNode());
    }

    public final boolean hasAttributes() {
        return target.hasAttributes();
    }

    public final boolean isSupported(String feature, String version) {
        return target.isSupported(feature, version);
    }

    public final String getBaseURI() {
        return target.getBaseURI();
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
        if (oldChild instanceof ProxyNode) {
            oldChild = ((ProxyNode<?,?>)oldChild).getTarget();
        }
        return target.removeChild(oldChild);
    }

    public final String getNodeName() {
        return target.getNodeName();
    }

    public final short getNodeType() {
        return target.getNodeType();
    }

    public final Document getOwnerDocument() {
        return (Document)toSAAJNode(target.getOwnerDocument());
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
            if (child instanceof Element) {
                return appendElement((Element)child);
            } else {
                target.appendChild(((ProxyNode<?,?>)child).target);
                return child;
            }
        } catch (SOAPException e) {
            DOMException ex = 
                new DOMException(DOMException.HIERARCHY_REQUEST_ERR, e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }
    
    protected Element appendElement(Element child) throws SOAPException {
        String namespaceURI = child.getNamespaceURI();
        String prefix = child.getPrefix();

        SOAPElementImpl<OMElement> childEle = (SOAPElementImpl<OMElement>)child;
        
        childEle.target.setUserData(SAAJ_NODE, childEle, null);
        if (namespaceURI != null && namespaceURI.trim().length() > 0) {
            childEle.omTarget.setNamespace(childEle.omTarget.declareNamespace(namespaceURI, prefix));
        }
        target.appendChild(childEle.target);
        childEle.target.getParentNode().setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement((SOAPElement)this);
        return childEle;
    }

    public final String getTextContent() throws DOMException {
        return target.getTextContent();
    }

    public final NamedNodeMap getAttributes() {
        return target.getAttributes();
    }

    public final org.w3c.dom.Node getNextSibling() {
        return toSAAJNode(target.getNextSibling());
    }

    public final org.w3c.dom.Node getPreviousSibling() {
        return toSAAJNode(target.getPreviousSibling());
    }
}
