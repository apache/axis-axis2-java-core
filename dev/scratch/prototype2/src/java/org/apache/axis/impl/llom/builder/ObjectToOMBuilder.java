package org.apache.axis.impl.llom.builder;

import org.apache.axis.om.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.Stack;
import java.util.Vector;

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
 * <p/>`
 */
public class ObjectToOMBuilder implements OMXMLParserWrapper, ContentHandler {
    private OutObject outObject;
    private OMElement startElement;
    private OMFactory omFactory;
    private boolean buildStarted = false;

    // ============= For content handler ============
    private OMNode lastNode = null;
    private StringBuffer buffer = new StringBuffer();
    private Stack textBufferStack = new Stack();
    private OMNode currentNode;
//    private OMElement parent = null;
    private Vector nameSpaces = new Vector();
    // ==============================================



    /**
     * @param startElement - this refers to the element the object should come under.
     *                     Most of the time this will be a OMBodyBlock element
     * @param outObject
     */
    public ObjectToOMBuilder(OMElement startElement, OutObject outObject) {
        startElement.setComplete(false);
        this.outObject = outObject;
        this.startElement = startElement;
        lastNode = startElement;
        startElement.setBuilder(this);
        this.outObject.setContentHandler(this);
        omFactory = OMFactory.newInstance();
    }

    public int next() throws OMException {

        // next can not be called more than once at one instance. and next can not be called within
        // another next
        synchronized (this) {
            if (!buildStarted) {
                buildStarted = true;
                outObject.startBuilding();
                this.startElement.setComplete(true);
            }
        }
        return -1;
    }

    /**
     * This has no meaning in this context, as one will basically not be able to stop and build again.
     * This is not useful for SAX event processing.
     *
     * @param el
     * @throws OMException
     */
    public void discard(OMElement el) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Caching will be accomplished by the content handler.
     *
     * @param b
     * @throws OMException
     */
    public void setCache(boolean b) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public Object getParser() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public boolean isCompleted() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public OMElement getDocumentElement() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    

    // ====================  ContentHandler Implementations ========================




    public void endDocument() throws SAXException {
        lastNode.setComplete(true);
    }

    public void startDocument() throws SAXException {
        lastNode = this.startElement;
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        OMText text = omFactory.createText(null, (new StringBuffer().append(ch, start, length).toString()));
        addNewNode(text, lastNode);
        text.setComplete(true);
        lastNode = text;
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        nameSpaces.add(omFactory.createOMNamespace(uri, prefix));
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {


        if (lastNode.isComplete()) {
            OMElement parent = lastNode.getParent();
            parent.setComplete(true);
            lastNode = parent;
        } else {
            lastNode.setComplete(true);
        }

//        String elementText = buffer.toString();
//        textBufferStack.pop();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        if (localName.length() == 0)
            localName = qName;

        // Out current parser (Piccolo) does not error when a
        // namespace is used and not defined.  Check for these here
        //todo In-insert this if needed!!!!!!
//        if (qName.indexOf(':') >= 0 && namespaceURI.length() == 0) {
//            throw new SAXException("Use of undefined namespace prefix: " +
//                    qName.substring(0, qName.indexOf(':')));
//        }

        String prefix = (qName.indexOf(':') >= 0) ? qName.substring(0, qName.indexOf(':')) : "";
        OMNamespace namespace = omFactory.createOMNamespace(namespaceURI, prefix);
        OMElement element = omFactory.createOMElement(localName, namespace, null, this);

        addNewNode(element, lastNode);

        for (int i = 0; i < nameSpaces.size(); i++) {
            OMNamespace ns = (OMNamespace) nameSpaces.elementAt(i);
            element.declareNamespace(ns);
        }
        nameSpaces.clear();

        String attrUri = "";
        String attrPrefix = "";
        OMNamespace ns = null;
        for (int i = 0; i < atts.getLength(); i++) {

            attrUri = atts.getURI(i);
            String attrQName = atts.getQName(i);
            attrPrefix = (attrQName.indexOf(':') >= 0) ? attrQName.substring(0, attrQName.indexOf(':')) : "";

            if (attrUri.hashCode() != 0)
                ns = element.findInScopeNamespace(attrUri, attrPrefix);

            if (ns == null)
            //todo this needs to be fixed!!!!!
            // throw new OMException("All elements must be namespace qualified!");

                element.insertAttribute(omFactory.createOMAttribute(atts.getLocalName(i), ns, atts.getValue(i)));
        }

        element.setComplete(false);
        lastNode = element;

    }

    private void addNewNode(OMNode currentNode, OMNode lastNode) {

        if (lastNode.isComplete()) {
            lastNode.setNextSibling(currentNode);
            currentNode.setParent(lastNode.getParent());
        } else {
            ((OMElement) lastNode).addChild(currentNode);
        }
    }

}
