package org.apache.axis.om.storage;

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
public class AttributeRow extends NodeRow implements Attr {

    private String URI;
    private String Prefix;
    private String localName;
    private String value;
//    private String valueURI;

    private Object nextSibling;


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


    public Node getNextSibling() {
        return (Node) this.nextSibling;
    }

    public void setNextSibling(Object nextSibling) {
        this.nextSibling = nextSibling;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    /*  Interface methods  */
    public String getNodeName() {
        return null;
    }

    public String getNodeValue()
            throws DOMException {
        return null;
    }

    public void setNodeValue(String nodeValue)
            throws DOMException {
    }

    public short getNodeType() {
        return 0;
    }

    public Node getParentNode() {
        return null;
    }

    public NodeList getChildNodes() {
        return null;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public NamedNodeMap getAttributes() {
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

    public String getName() {
        return null;
    }

    public boolean getSpecified() {
        return false;
    }

    public Element getOwnerElement() {
        return null;
    }

}
