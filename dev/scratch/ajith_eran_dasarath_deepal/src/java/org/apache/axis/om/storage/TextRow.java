package org.apache.axis.om.storage;

import org.apache.axis.om.OMConstants;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
 *         Time: 6:29:38 PM
 */
public class TextRow extends NodeRow implements Text,Comment,CDATASection {

    private String value;

    /* This class will be used to hold both the CDATA and PCDATA. Both these items have same attributes. So opted to use
     * the same class with a type attribute to say the type of the class
     *
     */
    private int type = OMConstants.TYPE_TEXT; //default is type text

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

   /*  interface methods */

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

    public Node getNextSibling() {
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

    public String getPrefix() {
        return null;
    }

    public void setPrefix(String prefix)
            throws DOMException {
    }

    public String getLocalName() {
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public Text splitText(int offset)
            throws DOMException {
        return null;
    }

    public String getData()
            throws DOMException {
        return null;
    }

    public void setData(String data)
            throws DOMException {
    }

    public int getLength() {
        return 0;
    }

    public String substringData(int offset,
                                int count)
            throws DOMException {
        return null;
    }

    public void appendData(String arg)
            throws DOMException {
    }

    public void insertData(int offset,
                           String arg)
            throws DOMException {
    }

    public void deleteData(int offset,
                           int count)
            throws DOMException {
    }

    public void replaceData(int offset,
                            int count,
                            String arg)
            throws DOMException {
    }
}
