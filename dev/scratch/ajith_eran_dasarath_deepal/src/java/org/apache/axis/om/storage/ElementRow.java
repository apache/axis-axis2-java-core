package org.apache.axis.om.storage;

import org.apache.axis.om.OMBuilderException;
import org.apache.axis.om.OMTableModel;
import org.apache.axis.om.StreamingOmBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    private String URI;
    private String Prefix;
    private String localName;

    private Object nextSibling;
    private StreamingOmBuilder builder;
    private Document parentDocument;

    public ElementRow(StreamingOmBuilder builder,Document parentDocument) {
        this.builder = builder;
        this.parentDocument = parentDocument ;
    }

    private boolean done;

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getPrefix() {
        return Prefix;
    }

    public void setPrefix(String prefix) {
        Prefix = prefix;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }





    public void setNextSibling(Object nextSibling) {
        this.nextSibling = nextSibling;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }


    /*   Interface methods  */
    public String getNodeName() {
        return localName;
    }

    public String getNodeValue()
            throws DOMException {
        return null;
        //todo implement this
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
        return (Node)this.parent;
    }

    public NodeList getChildNodes() {
        //implement this
        return null;
    }

    public Node getFirstChild() {
        OMTableModel omTableModel = (OMTableModel)this.parentDocument;
        Object[] children = omTableModel.getElementsByParent(this);

        while(children != null && !this.done){  //todo is this logic right?????

            try {
                builder.proceed();
            } catch (OMBuilderException e) {
                break; //just get out of here if the proceeding failed
            }
            //see whether there are children already for this element
            children = omTableModel.getElementsByParent(this);
        }

        if ((children==null) ||children.length==0){
            return null;
        }else{
            return (Node)children[0];
        }

    }

    public Node getLastChild() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node getNextSibling() {
        return null;
        //return ((OMTableModel)this.parentDocument).;
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
        return null;
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
