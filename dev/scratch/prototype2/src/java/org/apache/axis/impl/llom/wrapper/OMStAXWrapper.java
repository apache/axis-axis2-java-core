package org.apache.axis.impl.llom.wrapper;

import org.apache.axis.om.*;
import org.apache.axis.impl.llom.exception.OMStreamingException;
import org.apache.axis.impl.llom.OMNavigator;
import org.apache.axis.impl.llom.OMNavigator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Iterator;
import java.util.Stack;

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
 * @author Axis team
 * Date: Nov 18, 2004
 * Time: 2:16:09 PM
 *
 * Note  - This class also implements the streaming constants interface
 * to get access to the StAX constants
 */
public class OMStAXWrapper implements StreamingWrapper, XMLStreamConstants {


    private OMNavigator navigator;
    private OMXMLParserWrapper builder;
    private XMLStreamReader parser;
    private OMNode rootNode;
    private boolean isFirst = true;
    //the boolean flag that keeps the state of the document!
    private boolean complete = false;


    private int currentEvent = 0;
    // navigable means the output should be taken from the navigator
    // as soon as the navigator returns a null navigable will be reset
    //to false and the subsequent events will be taken from the builder
    //or the parser directly
    private boolean navigable = true;

    // SwitchingAllowed is set to false by default
    // this means that unless the user explicitly states
    // that he wants things not to be cached, everything will
    // be cached
    boolean switchingAllowed = false;

    private Stack elementStack = new Stack();

    //keeps the next event. The parser actually keeps one step ahead to
    //detect the end of navigation. (at the end of the stream the navigator
    //returns a null
    private OMNode nextNode = null;

    //holder for the current node. Needs this to generate events from the current node
    private OMNode currentNode = null;

    public void setAllowSwitching(boolean b) {
        this.switchingAllowed = b;
    }

    public boolean isAllowSwitching() {
        return switchingAllowed;
    }

    /**
     * When constructing the OMStaxWrapper, the creator must produce the
     * builder (an instance of the OMXMLparserWrapper of the input) and the
     * Element Node to start parsing. The wrapper wil parse(proceed) until
     * the end of the given element. hence care should be taken to pass the
     * root element if the entire document is needed
     */
    public OMStAXWrapper(OMXMLParserWrapper builder, OMElement startNode) {
        //create a navigator
        this.navigator = new OMNavigator(startNode);
        this.builder = builder;
        this.rootNode = startNode;
        //initaite the next and current nodes
        //Note -  navigator is written in such a way that it first
        //returns the starting node at the first call to it
        currentNode = navigator.next();
        nextNode = navigator.next();

    }

    public String getPrefix() {
        String returnStr = null;
        if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT) {
            if (navigable) {
                OMNamespace ns = ((OMElement) currentNode).getNamespace();
                returnStr = ns == null ? null : ns.getPrefix();
            } else {
                returnStr = parser.getPrefix();
            }
        }
        return returnStr;
    }

    /**
     *
     * @return
     */
    public String getNamespaceURI() {
        String returnStr = null;
        if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT || currentEvent == NAMESPACE) {
            if (navigable) {
                OMNamespace ns = ((OMElement) currentNode).getNamespace();
                returnStr = ns == null ? null : ns.getValue();
            } else {
                returnStr = parser.getNamespaceURI();
            }
        }

        return returnStr;
    }

    /**
     *
     * @return
     */
    public boolean hasName() {
        if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public String getLocalName() {
        String returnStr = null;
        if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT || currentEvent == ENTITY_REFERENCE) {
            if (navigable) {
                returnStr = ((OMElement) currentNode).getLocalName();
            } else {
                returnStr = parser.getLocalName();
            }
        }
        return returnStr;
    }

    public QName getName() {

        QName returnName = null;
        if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT) {
            if (navigable) {
                returnName = getQName((OMNamedNode) currentNode);
            } else {
                returnName = parser.getName();
            }
        }
        return returnName;
    }


    public boolean hasText() {
        return (currentEvent == CHARACTERS ||
                currentEvent == DTD ||
                currentEvent == ENTITY_REFERENCE ||
                currentEvent == COMMENT ||
                currentEvent == SPACE);
    }

    public int getTextLength() {
        int returnLength = 0;
        if (hasText()) {
            if (navigable) {
                OMText textNode = (OMText) currentNode;
                returnLength = textNode.getValue().length();
            } else {
                returnLength = parser.getTextLength();
            }

        }
        return returnLength;
    }

    public int getTextStart() {
        int returnLength = 0;
        if (hasText()) {
            if (!navigable) {
                returnLength = parser.getTextStart();
            }
            //Note - this has no relevant method in the OM
        }
        return returnLength;
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws OMStreamingException {
        int returnLength = 0;
        if (hasText()) {
            if (!navigable) {
                try {
                    returnLength = parser.getTextCharacters(i, chars, i1, i2);
                } catch (XMLStreamException e) {
                    throw new OMStreamingException(e);
                }
            }
            //Note - this has no relevant method in the OM
        }
        return returnLength;
    }

    /**
     *
     * @return
     */
    public char[] getTextCharacters() {
        char[] returnArray = null;
        if (hasText()) {
            if (navigable) {
                OMText textNode = (OMText) currentNode;
                String str = textNode.getValue();
                returnArray = str.toCharArray();
            } else {
                returnArray = parser.getTextCharacters();
            }

        }
        return returnArray;
    }

    /**
     *
     * @return
     */
    public String getText() {
        String returnString = null;
        if (hasText()) {
            if (navigable) {
                OMText textNode = (OMText) currentNode;
                returnString = textNode.getValue();
            } else {
                returnString = parser.getText();
            }
        }
        return returnString;
    }

    //todo this should be improved
    public int getEventType() {
        return currentEvent;
    }

    public String getNamespaceURI(int i) {
        String returnString = null;
        if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {
            if (navigable) {
                //Nothing to do here! How to get the namespacace references
            } else {
                returnString = parser.getNamespaceURI(i);
            }
        }
        return returnString;
    }

    public String getNamespacePrefix(int i) {
        String returnString = null;
        if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {
            if (navigable) {
                //Nothing to do here! How to get the namespacace references
            } else {
                returnString = parser.getNamespacePrefix(i);
            }
        }
        return returnString;
    }

    public int getNamespaceCount() {
        int returnCount = 0;
        if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {
            if (navigable) {
                //Nothing to do here! How to get the namespacace references
            } else {
                returnCount = parser.getNamespaceCount();
            }
        }
        return returnCount;
    }

    public boolean isAttributeSpecified(int i) {
        boolean returnValue = false;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                //theres nothing to be returned here
            } else {
                returnValue = parser.isAttributeSpecified(i);
            }
        } else {
            throw new IllegalStateException("attribute type accessed in illegal event!");
        }
        return returnValue;
    }

    public String getAttributeValue(int i) {
        String returnString = null;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                OMAttribute attrib = getAttribute((OMElement) currentNode, i);
                if (attrib != null) {
                    returnString = attrib.getValue();
                }
            } else {
                returnString = parser.getAttributeValue(i);
            }
        } else {
            throw new IllegalStateException("attribute type accessed in illegal event!");
        }
        return returnString;
    }

    public String getAttributeType(int i) {
        String returnString = null;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                //todo implement this
            } else {
                returnString = parser.getAttributeType(i);
            }
        } else {
            throw new IllegalStateException("attribute type accessed in illegal event!");
        }
        return returnString;
    }

    public String getAttributePrefix(int i) {
        String returnString = null;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                OMAttribute attrib = getAttribute((OMElement) currentNode, i);
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getPrefix();
                    }
                }
            } else {
                returnString = parser.getAttributePrefix(i);
            }
        } else {
            throw new IllegalStateException("attribute prefix accessed in illegal event!");
        }
        return returnString;
    }

    public String getAttributeLocalName(int i) {
        String returnString = null;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                OMAttribute attrib = getAttribute((OMElement) currentNode, i);
                if (attrib != null)
                    returnString = attrib.getLocalName();

            } else {
                returnString = parser.getAttributeLocalName(i);
            }
        } else {
            throw new IllegalStateException("attribute localName accessed in illegal event!");
        }
        return returnString;
    }

    public String getAttributeNamespace(int i) {
        String returnString = null;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                OMAttribute attrib = getAttribute((OMElement) currentNode, i);
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getValue();
                    }
                }
            } else {
                returnString = parser.getAttributeNamespace(i);
            }
        } else {
            throw new IllegalStateException("attribute nameSpace accessed in illegal event!");
        }
        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributeName
     * @param i
     * @return
     */
    public QName getAttributeName(int i) {
        QName returnQName = null;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                returnQName = getQName(getAttribute((OMElement) currentNode, i));
            } else {
                returnQName = parser.getAttributeName(i);
            }
        } else {
            throw new IllegalStateException("attribute count accessed in illegal event!");
        }
        return returnQName;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributeCount
     * @return
     */
    public int getAttributeCount() {
        int returnCount = 0;
        if (isStartElement() || currentEvent == ATTRIBUTE) {
            if (navigable) {
                OMElement elt = (OMElement) currentNode;
                returnCount = getCount(elt.getAttributes());
            } else {
                returnCount = parser.getAttributeCount();
            }

        } else {
            throw new IllegalStateException("attribute count accessed in illegal event!");
        }

        return returnCount;
    }


    public String getAttributeValue(String s, String s1) {
        return null;
    }

    public boolean isWhiteSpace() {
        boolean b;
        if (navigable) {
            b = (currentEvent == SPACE);
        } else {
            b = parser.isWhiteSpace();
        }

        return b;
    }

    public boolean isCharacters() {
        boolean b;
        if (navigable) {
            b = (currentEvent == CHARACTERS);
        } else {
            b = parser.isCharacters();
        }
        return b;
    }

    public boolean isEndElement() {
        boolean b;
        if (navigable) {
            b = (currentEvent == END_ELEMENT);
        } else {
            b = parser.isEndElement();
        }
        return b;
    }

    public boolean isStartElement() {
        boolean b;
        if (navigable) {
            b = (currentEvent == START_ELEMENT);
        } else {
            b = parser.isEndElement();
        }
        return b;
    }

    public String getNamespaceURI(String s) {
        String returnString = null;
        if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {
            if (navigable) {
                //Nothing to do here! How to get the namespacace references
            } else {
                returnString = parser.getNamespaceURI(s);
            }
        }
        return returnString;
    }

    public void close() throws OMStreamingException {
        //this doesnot mean anything with respect to the OM
        if (!navigable){
            try {
                parser.close();
            } catch (XMLStreamException e) {
                throw new OMStreamingException(e);
            }
        }

    }

    public boolean hasNext() throws OMStreamingException {
        return !complete;
    }

    /**
     * Not implemented yet
     * @return
     * @throws org.apache.axis.impl.llom.exception.OMStreamingException
     */
    public int nextTag() throws OMStreamingException {
        return 0;
    }

    public String getElementText() throws OMStreamingException {

        String returnText = "";

        if (navigable) {
            if (currentNode.getType() == OMNode.ELEMENT_NODE) {
                //todo complete this
                return null;
            }
        } else {
            try {
                returnText = parser.getElementText();
            } catch (XMLStreamException e) {
                throw new OMStreamingException(e);
            }
        }
        return returnText;
    }

    public int next() throws OMStreamingException {

        if (complete) {
            throw new OMStreamingException("Parser completed!");
        }

        if (navigable) {
//            System.out.println("--------------");
//            System.out.println("currentNode = " + currentNode);
//            System.out.println("currentNode = " + currentNode.getValue());

            currentEvent = generateEvents(currentNode);
            updateCompleteStatus();
            updateNextNode();
        } else {
            currentEvent = builder.next();
            updateCompleteStatus();
        }
        return currentEvent;
    }

    public Object getProperty(String s) throws IllegalArgumentException {
        return null;
    }


    /**
     * This is a very important method. this keeps the
     * navigator one step ahead and pushes the navigator
     * one event ahead. If the nextNode is null then navigable is set to false;
     * At the same time the parser and builder are set up for the upcoming event
     * generation
     */
    private void updateNextNode() throws OMStreamingException {

        currentNode = nextNode;

        if (navigator.isNavigable()) {
            nextNode = navigator.next();
        } else {
            if (!switchingAllowed) {
                if (navigator.isCompleted()) {
                    nextNode = null;
                } else {
                    builder.next();
                    navigator.step();
                    nextNode = navigator.next();
                }
            } else {
                //reset caching (the default is ON so it was not needed in the
                //earlier case!
                builder.setCache(false);
                //load the parser
                try {
                    parser = (XMLStreamReader) builder.getParser();
                } catch (ClassCastException e) {
                    throw new OMStreamingException("incompatible parser found!", e);
                }
                //set navigable to false.Now the subsequent requests will be directed to
                //the parser
                navigable = false;


            }
        }
    }

    private void updateCompleteStatus() {
        //todo this needs to be done correctly
        if (!navigable) {
            complete = (currentEvent == END_DOCUMENT);
        } else {
            if (rootNode.equals(currentNode))
                if (isFirst)
                    isFirst = false;
                else
                    complete = true;
        }
    }

    /*

    ################################################################
    Generator methods for the OMNodes returned by the navigator
    ################################################################

    */

    private int generateEvents(OMNode node) {
        int returnEvent = 0;

        int nodeType = node.getType();
        switch (nodeType) {
            case OMNode.ELEMENT_NODE:
                returnEvent = generateElementEvents((OMElement) node);
                break;
            case OMNode.TEXT_NODE:
                returnEvent = generateTextEvents();
                break;
            case OMNode.COMMENT_NODE:
                returnEvent = generateCommentEvents();
                break;
            case OMNode.CDATA_SECTION_NODE:
                returnEvent = generateCdataEvents();
                break;
            default:
                break; //just ignore any other nodes
        }

        return returnEvent;
    }


    private int generateElementEvents(OMElement elt) {
        int returnValue = START_ELEMENT;
        if (!elementStack.isEmpty() && elementStack.peek().equals(elt)) {
            returnValue = END_ELEMENT;
            elementStack.pop();
        } else {
            elementStack.push(elt);
        }
        return returnValue;
    }


    private int generateTextEvents() {
        return CHARACTERS;
    }

    private int generateCommentEvents() {
        return COMMENT;
    }

    private int generateCdataEvents() {
        return CDATA;
    }

    /*
    ####################################################################
    Other helper methods
    ####################################################################
    */

    /**
     * helper method
     * @param it
     * @return
     */
    private int getCount(Iterator it) {
        int count = 0;
        if (it != null) {
            while (it.hasNext()) {
                it.next();
                count++;
            }
        }
        return count;
    }

    /**
     * Helper method
     * @param it
     * @param index
     * @return
     */
    private Object getItemFromIterator(Iterator it, int index) {
        int count = 0;
        Object returnObject = null;
        boolean found = false;
        if (it != null) {
            while (it.hasNext()) {
                returnObject = it.next();
                if (index == count++) {
                    found = true;
                    break;
                }
            }
        }

        if (found) {
            return returnObject;
        } else {
            return null;
        }
    }

    /**
     * Helper method
     * @param namedNode
     * @return
     */
    private QName getQName(OMNamedNode namedNode) {
        QName returnName;
        OMNamespace ns = namedNode.getNamespace();
        String localPart = namedNode.getLocalName();
        if (ns != null) {
            String prefix = ns.getPrefix();
            String uri = ns.getValue();
            if (parser.equals(""))
                returnName = new QName(uri, localPart);
            else
                returnName = new QName(uri, localPart, prefix);
        } else {
            returnName = new QName(localPart);
        }
        return returnName;
    }

    /**
     *
     * @param elt
     * @param index
     * @return
     */
    private OMAttribute getAttribute(OMElement elt, int index) {
        OMAttribute returnAttrib = null;
        if (elt != null) {
            returnAttrib = (OMAttribute) getItemFromIterator(elt.getAttributes(), index);
        }
        return returnAttrib;
    }
}
