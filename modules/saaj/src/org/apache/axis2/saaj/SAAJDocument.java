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

import org.apache.axiom.om.OMDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public final class SAAJDocument extends ProxyNode<Document,OMDocument> implements Document {
    public SAAJDocument(OMDocument target) {
        super((Document)target, target);
    }

    public String getValue() {
        throw new UnsupportedOperationException();
    }

    public void setValue(String value) {
        throw new UnsupportedOperationException();
    }

    public Node adoptNode(Node arg0) throws DOMException {
        return target.adoptNode(arg0);
    }

    public Attr createAttribute(String arg0) throws DOMException {
        return target.createAttribute(arg0);
    }

    public Attr createAttributeNS(String arg0, String arg1) throws DOMException {
        return target.createAttributeNS(arg0, arg1);
    }

    public CDATASection createCDATASection(String arg0) throws DOMException {
        return (CDATASection)toSAAJNode(target.createCDATASection(arg0));
    }

    public Comment createComment(String arg0) {
        return (Comment)toSAAJNode(target.createComment(arg0));
    }

    public DocumentFragment createDocumentFragment() {
        return target.createDocumentFragment();
    }

    public Element createElement(String arg0) throws DOMException {
        return (Element)toSAAJNode(target.createElementNS(null, arg0));
    }

    public Element createElementNS(String arg0, String arg1) throws DOMException {
        return (Element)toSAAJNode(target.createElementNS(arg0, arg1));
    }

    public EntityReference createEntityReference(String arg0) throws DOMException {
        return target.createEntityReference(arg0);
    }

    public ProcessingInstruction createProcessingInstruction(String arg0, String arg1)
            throws DOMException {
        return target.createProcessingInstruction(arg0, arg1);
    }

    public Text createTextNode(String arg0) {
        return (Text)toSAAJNode(target.createTextNode(arg0));
    }

    public DocumentType getDoctype() {
        return target.getDoctype();
    }

    public Element getDocumentElement() {
        return target.getDocumentElement();
    }

    public String getDocumentURI() {
        return target.getDocumentURI();
    }

    public DOMConfiguration getDomConfig() {
        return target.getDomConfig();
    }

    public Element getElementById(String arg0) {
        return target.getElementById(arg0);
    }

    public NodeList getElementsByTagName(String arg0) {
        return target.getElementsByTagName(arg0);
    }

    public NodeList getElementsByTagNameNS(String arg0, String arg1) {
        return target.getElementsByTagNameNS(arg0, arg1);
    }

    public DOMImplementation getImplementation() {
        return target.getImplementation();
    }

    public String getInputEncoding() {
        return target.getInputEncoding();
    }

    public Node getParentNode() {
        return target.getParentNode();
    }

    public boolean getStrictErrorChecking() {
        return target.getStrictErrorChecking();
    }

    public String getXmlEncoding() {
        return target.getXmlEncoding();
    }

    public boolean getXmlStandalone() {
        return target.getXmlStandalone();
    }

    public String getXmlVersion() {
        return target.getXmlVersion();
    }

    public Node importNode(Node arg0, boolean arg1) throws DOMException {
        return target.importNode(arg0, arg1);
    }

    public void normalizeDocument() {
        target.normalizeDocument();
    }

    public Node renameNode(Node arg0, String arg1, String arg2) throws DOMException {
        return target.renameNode(arg0, arg1, arg2);
    }

    public void setDocumentURI(String arg0) {
        target.setDocumentURI(arg0);
    }

    public void setStrictErrorChecking(boolean arg0) {
        target.setStrictErrorChecking(arg0);
    }

    public void setXmlStandalone(boolean arg0) throws DOMException {
        target.setXmlStandalone(arg0);
    }

    public void setXmlVersion(String arg0) throws DOMException {
        target.setXmlVersion(arg0);
    }
}
