package org.apache.axis.om.storage;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMTableModel;
import org.apache.axis.om.StreamingOMBuilder;
import org.apache.axis.om.traversal.ElementNodeList;
import org.w3c.dom.*;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Ajith Ranabahu
 *         Date: Sep 16, 2004
 *         Time: 6:18:52 PM
 */
public class ElementRow extends NodeRow implements Element {

    private String URI = null;
    private String elementPrefix;
    private String localName;
    private Object firstChild;


    private StreamingOMBuilder builder;

    public ElementRow(StreamingOMBuilder builder, Document parentDocument) {
        this.builder = builder;
        this.parentDocument = parentDocument;
    }


    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getElementPrefix() {
        return elementPrefix;
    }

    /**
     * Since setPrefix method is not exposed through DOM API, its exposed through this method
     * @param prefix
     */
    public void setElementPrefix(String prefix) {
        elementPrefix = prefix;
    }

    public String getPrefix() {
        return getElementPrefix();
    }

    /**
     * This method is currently unsuported due to the foloowing reason.
     * If one comes and changes the prefix of the element, the URI of the element must also be changed.
     * But this is not simple, as if someone does this at a later time, the namespace stack may not be available.
     * Then the getNamespaceURI() may return some erroneous value
     *
     * This method is not exposed through the DOM API
     * Eran Chinthaka on 22-09-2004
     * @param prefix
     * @exception UnsupportedOperationException
     */
    public void setPrefix(String prefix) {
        throw new UnsupportedOperationException("this method may lead to wrong outcome for the namespace uri of the element");
        //elementPrefix = prefix;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }





    /*   Interface methods  */
    public String getNodeName() {
        return OMConstants.NODE_ELEMENT;
    }

    public String getNodeValue()
            throws DOMException {
        return Element.ELEMENT_NODE + "";
    }

    public void setNodeValue(String nodeValue)
            throws DOMException {
        //not supported
        throw new UnsupportedOperationException();
    }

    public short getNodeType() {
        return ELEMENT_NODE;
    }

    public Node getParentNode() {
        return (Node) this.parent;
    }

    public NodeList getChildNodes() {
        NodeList nodeList = new ElementNodeList(this);
        return nodeList;
    }

    /**
     * This will return null if there is no child
     *
     * @return
     */ public Node getFirstChild() {

        while( (firstChild == null) && (!done)){
            ((OMTableModel) parentDocument).proceedTheParser();
        }

        return (Node) firstChild;
    }

    public void setFirstChild(Object firstChild) {
        this.firstChild = firstChild;
    }

    public Node getLastChild() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public NamedNodeMap getAttributes() {

        //todo this needs to be filled

        return null;
    }

    public Document getOwnerDocument() {
        return null;
    }

    public Node insertBefore(Node newChild,
                             Node refChild)
            throws DOMException {
        return null;
    }

    public Node replaceChild(Node newChild,
                             Node oldChild)
            throws DOMException {
        return null;
    }

    public Node removeChild(Node oldChild)
            throws DOMException {
        return null;
    }

    public Node appendChild(Node newChild)
            throws DOMException {
        return null;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public Node cloneNode(boolean deep) {
        return null;
    }

    public void normalize() {
    }

    public boolean isSupported(String feature,
                               String version) {
        return false;
    }

    public String getNamespaceURI() {
        return URI;
    }

    public boolean hasAttributes() {
        return false;
    }

    public String getTagName() {
        return null;
    }

    public String getAttribute(String name) {
        return null;
    }

    public void setAttribute(String name,
                             String value)
            throws DOMException {
    }

    public void removeAttribute(String name)
            throws DOMException {
    }

    public Attr getAttributeNode(String name) {
        return null;
    }

    public Attr setAttributeNode(Attr newAttr)
            throws DOMException {
        return null;
    }

    public Attr removeAttributeNode(Attr oldAttr)
            throws DOMException {
        return null;
    }

    public NodeList getElementsByTagName(String name) {
        return null;
    }

    public String getAttributeNS(String namespaceURI,
                                 String localName) {
        return null;
    }

    public void setAttributeNS(String namespaceURI,
                               String qualifiedName,
                               String value)
            throws DOMException {
    }

    public void removeAttributeNS(String namespaceURI,
                                  String localName)
            throws DOMException {
    }

    public Attr getAttributeNodeNS(String namespaceURI,
                                   String localName) {
        return null;
    }

    public Attr setAttributeNodeNS(Attr newAttr)
            throws DOMException {
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        return null;
    }

    public boolean hasAttribute(String name) {
        return false;
    }

    public boolean hasAttributeNS(String namespaceURI,
                                  String localName) {
        return false;
    }

}
