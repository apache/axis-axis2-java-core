package org.apache.axis.impl.llom;

import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.impl.llom.exception.OMStreamingException;
import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamedNode;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 */

/**
 *         Note  - This class also implements the streaming constants interface
 *         to get access to the StAX constants
 */
public class OMStAXWrapper implements XMLStreamReader, XMLStreamConstants {

    private Log log = LogFactory.getLog(getClass());
    private OMNavigator navigator;
    private OMXMLParserWrapper builder;
    private XMLStreamReader parser;
    private OMNode rootNode;
    private boolean isFirst = true;

    // navigable means the output should be taken from the navigator
    // as soon as the navigator returns a null navigable will be reset
    //to false and the subsequent events will be taken from the builder
    //or the parser directly
    private static final short NAVIGABLE = 0;
    private static final short SWITCH_AT_NEXT = 1;
    private static final short COMPLETED = 2;
    private static final short SWITCHED = 3;

    private short state;
    private int currentEvent = 0;



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

    //needs this to refer to the last known node
    private OMNode lastNode = null;

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
    OMStAXWrapper(OMXMLParserWrapper builder, OMElement startNode) {
        this(builder,startNode,false);
    }

    OMStAXWrapper(OMXMLParserWrapper builder, OMElement startNode,boolean cacheOff) {
        //create a navigator
        this.navigator = new OMNavigator(startNode);
        this.builder = builder;
        this.rootNode = startNode;
        //initaite the next and current nodes
        //Note -  navigator is written in such a way that it first
        //returns the starting node at the first call to it
        currentNode = navigator.next();
        updateNextNode();
        switchingAllowed = cacheOff;
    }
    /**
     * @see javax.xml.stream.XMLStreamReader#getPrefix()
     */
    public String getPrefix() {
        String returnStr = null;
        if (parser!=null){
            returnStr = parser.getPrefix();
        }else{
            if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT) {
                OMNamespace ns = ((OMElement) lastNode).getNamespace();
                returnStr = ns == null ? null : ns.getPrefix();
            }
        }
        return returnStr;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI()
     */
    public String getNamespaceURI() {
        String returnStr = null;
        if (parser!=null){
            returnStr = parser.getNamespaceURI();
        }else{
            if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT || currentEvent == NAMESPACE) {
                OMNamespace ns = ((OMElement) lastNode).getNamespace();
                returnStr = ns == null ? null : ns.getName();
            }
        }
        return returnStr;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#hasName()
     */
    public boolean hasName() {
        if (parser!= null){
            return parser.hasName();
        }else{
            return (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT);
        }
    }

    /**
     *@see javax.xml.stream.XMLStreamReader#getLocalName()
     */
    public String getLocalName() {
        String returnStr = null;
        if (parser!=null){
            returnStr = parser.getLocalName();
        }else{
            if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT || currentEvent == ENTITY_REFERENCE) {
                returnStr = ((OMElement) lastNode).getLocalName();
            }
        }

        return returnStr;
    }

    /**
     *@see javax.xml.stream.XMLStreamReader#getName()
     */
    public QName getName() {
        QName returnName = null;
        if (parser!=null){
            returnName = parser.getName();
        } else{
            if (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT) {
                returnName = getQName((OMNamedNode) lastNode);
            }
        }

        return returnName;
    }


    /**
     * @see javax.xml.stream.XMLStreamReader#hasText()
     */
    public boolean hasText() {
        return (currentEvent == CHARACTERS ||
                currentEvent == DTD ||
                currentEvent == ENTITY_REFERENCE ||
                currentEvent == COMMENT ||
                currentEvent == SPACE);
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getTextLength()
     */
    public int getTextLength() {
        int returnLength = 0;
        if (parser!=null){
            returnLength = parser.getTextLength();
        } else{
            OMText textNode = (OMText) lastNode;
            returnLength = textNode.getValue().length();
        }

        return returnLength;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getTextStart()
     */
    public int getTextStart() {
        int returnLength = 0;
        if (parser !=null) {
            returnLength = parser.getTextStart();
        }
        //Note - this has no relevant method in the OM

        return returnLength;
    }

    /**
     *
     *  @see javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)
     */
    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        int returnLength = 0;
        if (hasText()) {
            if (parser!=null) {
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
     * @see javax.xml.stream.XMLStreamReader#getTextCharacters()
     */
    public char[] getTextCharacters() {
        char[] returnArray = null;

        if (parser!=null) {
            returnArray = parser.getTextCharacters();
        } else {
            if (hasText()) {
                OMText textNode = (OMText) lastNode;
                String str = textNode.getValue();
                returnArray = str.toCharArray();
            }
        }

        return returnArray;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getText()
     */
    public String getText() {
        String returnString = null;
        if (parser!=null){
            returnString = parser.getText();
        }  else {
            if (hasText()) {

                OMText textNode = (OMText) lastNode;
                returnString = textNode.getValue();

            }
        }
        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getEventType()
     */
    //todo this should be improved
    public int getEventType() {
        return currentEvent;
    }

    /**
     * @see  javax.xml.stream.XMLStreamReader#getNamespaceURI
     *
     */
    public String getNamespaceURI(int i) {
        String returnString = null;
        if (parser!=null){
            returnString = parser.getNamespaceURI(i);
        }else{
            if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {
                OMNamespace ns = (OMNamespace)getItemFromIterator(((OMElement)lastNode).getAllDeclaredNamespaces(),i);
                returnString = ns==null?null:ns.getName();
            }
        }

        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getNamespacePrefix
     */
    public String getNamespacePrefix(int i) {
        String returnString = null;
        if (parser !=null){
            returnString = parser.getNamespacePrefix(i);
        } else {
            if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {
                OMNamespace ns = (OMNamespace)getItemFromIterator(((OMElement)lastNode).getAllDeclaredNamespaces(),i);
                returnString = ns==null?null:ns.getPrefix();
            }
        }
        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getNamespaceCount()
     */
    public int getNamespaceCount() {
        int returnCount = 0;
        if (parser!=null){
            returnCount = parser.getNamespaceCount();
        } else{
            if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {
                returnCount = getCount(((OMElement)lastNode).getAllDeclaredNamespaces());
            }
        }


        return returnCount;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#isAttributeSpecified
     */
    public boolean isAttributeSpecified(int i) {
        boolean returnValue = false;
        if (parser!=null){
            returnValue = parser.isAttributeSpecified(i);
        }else {
            if (isStartElement() || currentEvent == ATTRIBUTE) {
                //theres nothing to be returned here
            } else {
                throw new IllegalStateException("attribute type accessed in illegal event!");
            }
        }
        return returnValue;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributeValue
     */
    public String getAttributeValue(int i) {
        String returnString = null;
        if (parser!=null){
            returnString = parser.getAttributeValue(i);
        }else{
            if (isStartElement() || currentEvent == ATTRIBUTE) {

                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null) {
                    returnString = attrib.getValue();
                }
            }else{
                throw new IllegalStateException("attribute type accessed in illegal event!");
            }
        }

        return returnString;
    }

    /**
     *
     * @see javax.xml.stream.XMLStreamReader#getAttributeType
     */
    public String getAttributeType(int i) {
        String returnString = null;
        if (parser!=null){
            returnString = parser.getAttributeType(i);
        } else {
            if (isStartElement() || currentEvent == ATTRIBUTE) {
                //todo implement this
        } else {
            throw new IllegalStateException("attribute type accessed in illegal event!");
        }
        }

        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributePrefix
     */
    public String getAttributePrefix(int i) {
        String returnString = null;
        if (parser!=null){
           returnString = parser.getAttributePrefix(i);
        }else{
           if (isStartElement() || currentEvent == ATTRIBUTE) {

                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getPrefix();
                    }
                }

        } else {
            throw new IllegalStateException("attribute prefix accessed in illegal event!");
        }
        }

        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributeLocalName
     */
    public String getAttributeLocalName(int i) {
        String returnString = null;
        if (parser!=null){
           returnString = parser.getAttributeLocalName(i);
        }else{
           if (isStartElement() || currentEvent == ATTRIBUTE) {

                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null)
                    returnString = attrib.getLocalName();


        } else {
            throw new IllegalStateException("attribute localName accessed in illegal event!");
        }
        }

        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributeNamespace
     */
    public String getAttributeNamespace(int i) {
        String returnString = null;
        if (parser!=null){
             returnString = parser.getAttributeNamespace(i);
        }else{
            if (isStartElement() || currentEvent == ATTRIBUTE) {

                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getName();
                    }
                }

        } else {
            throw new IllegalStateException("attribute nameSpace accessed in illegal event!");
        }
        }

        return returnString;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributeName
     */
    public QName getAttributeName(int i) {
        QName returnQName = null;
        if (parser!=null){
           returnQName = parser.getAttributeName(i);
        }else{
           if (isStartElement() || currentEvent == ATTRIBUTE) {

                returnQName = getAttribute((OMElement) lastNode, i).getQName();

        } else {
            throw new IllegalStateException("attribute count accessed in illegal event!");
        }
        }

        return returnQName;
    }

    /**
     * @return
     * @see javax.xml.stream.XMLStreamReader#getAttributeCount
     */
    public int getAttributeCount() {
        int returnCount = 0;
        if (parser!=null){
            returnCount = parser.getAttributeCount();
        }else{
           if (isStartElement() || currentEvent == ATTRIBUTE) {

                OMElement elt = (OMElement) lastNode;
                returnCount = getCount(elt.getAttributes());


        } else {
            throw new IllegalStateException("attribute count accessed in illegal event!");
        }
        }


        return returnCount;
    }


    //todo
    public String getAttributeValue(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    public boolean isWhiteSpace() {
        boolean b;
        if (parser!=null){
             b = parser.isWhiteSpace();
        }else{
              b = (currentEvent == SPACE);
        }

        return b;
    }

    public boolean isCharacters() {
        boolean b;
        if (parser!=null){
             b = parser.isCharacters();
        }else{
             b = (currentEvent == CHARACTERS);
        }
        return b;
    }

    public boolean isEndElement() {
        boolean b;
        if (parser!=null){
            b = parser.isEndElement();
        }else{
            b = (currentEvent == END_ELEMENT);
        }

        return b;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#require(int, String, String)
     * @param i
     * @param s
     * @param s1
     * @throws XMLStreamException
     */
    public void require(int i, String s, String s1) throws XMLStreamException {
        throw new XMLStreamException();
    }

    public boolean isStartElement() {
        boolean b;
        if (parser!=null){
             b = parser.isStartElement();
        }else{
             b = (currentEvent == START_ELEMENT);
        }

        return b;
    }

    public String getNamespaceURI(String s) {
        String returnString = null;
        if (parser!=null){
            returnString = parser.getNamespaceURI(s);
        }else{
            if (isStartElement() || isEndElement() || currentEvent == NAMESPACE) {

                //Nothing to do here! How to get the namespacace references

        }
        }

        return returnString;
    }

    public void close() throws XMLStreamException {
        //this doesnot mean anything with respect to the OM
        if (parser!=null) {
            parser.close();

        }

    }

    public boolean hasNext() throws XMLStreamException {
        return !(state==COMPLETED);
    }

    /**
     * Not implemented yet
     *
     * @return
     * @throws org.apache.axis.impl.llom.exception.OMStreamingException
     *
     */
    public int nextTag() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getElementText()
     */
    public String getElementText() throws XMLStreamException {

        String returnText = "";
        if (parser!=null){
            try {
                returnText = parser.getElementText();
            } catch (XMLStreamException e) {
                throw new OMStreamingException(e);
            }
        }else{
           if (currentNode.getType() == OMNode.ELEMENT_NODE) {
                //todo complete this
                return null;
            }
        }
       
        return returnText;
    }

    public int next() throws XMLStreamException {
        switch(state){
            case COMPLETED:
                throw new OMStreamingException("Parser completed!");
            case SWITCH_AT_NEXT:
                state = SWITCHED;
                //load the parser
                try {
                    parser = (XMLStreamReader) builder.getParser();
                } catch (ClassCastException e) {
                    throw new UnsupportedOperationException("incompatible parser found!");
                }
                log.info("Switching to the Real Stax parser to generated the future events");
                currentEvent = parser.next();
                updateCompleteStatus();
                break;
            case NAVIGABLE:
                currentEvent = generateEvents(currentNode);
                updateCompleteStatus();
                updateLastNode();
                break;
            case SWITCHED:
                currentEvent = parser.next();
                updateCompleteStatus();
                break;
            default:
                throw new OMStreamingException("unsuppported state!");
        }


        return currentEvent;
    }

    public Object getProperty(String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }


    /**
     * This is a very important method. this keeps the
     * navigator one step ahead and pushes the navigator
     * one event ahead. If the nextNode is null then navigable is set to false;
     * At the same time the parser and builder are set up for the upcoming event
     * generation
     */
    private void updateLastNode() throws XMLStreamException {
        lastNode = currentNode;
        currentNode = nextNode;

        try {
            updateNextNode();
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }

    private void updateNextNode() {

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
                state = SWITCH_AT_NEXT;
            }
        }
    }

    private void updateCompleteStatus() {
        if (state==NAVIGABLE){
            if (rootNode == currentNode)
                if (isFirst)
                    isFirst = false;
                else
                    state = COMPLETED;
        } else{
            state = (currentEvent == END_DOCUMENT)?COMPLETED:state;
        }

    }


    public NamespaceContext getNamespaceContext() {
        throw new UnsupportedOperationException();
    }

    public String getEncoding() {
        throw new UnsupportedOperationException();
    }

    public Location getLocation() {
        throw new UnsupportedOperationException();
    }

    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    public boolean isStandalone() {
        throw new UnsupportedOperationException();
    }

    public boolean standaloneSet() {
        throw new UnsupportedOperationException();
    }

    public String getCharacterEncodingScheme() {
        throw new UnsupportedOperationException();
    }

    public String getPITarget() {
        throw new UnsupportedOperationException();
    }

    public String getPIData() {
        throw new UnsupportedOperationException();
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
                OMElement element = (OMElement) node;
                log.info("Generating events from element {" + element.getNamespaceName()+ "}" + element.getLocalName() + " Generated OM tree");
                returnEvent = generateElementEvents(element);
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
     *
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
     *
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
     *
     * @param namedNode
     * @return
     */
    private QName getQName(OMNamedNode namedNode) {
        QName returnName;
        OMNamespace ns = namedNode.getNamespace();
        String localPart = namedNode.getLocalName();
        if (ns != null) {
            String prefix = ns.getPrefix();
            String uri = ns.getName();
            if (prefix==null || prefix.equals(""))
                returnName = new QName(uri, localPart);
            else
                returnName = new QName(uri, localPart, prefix);
        } else {
            returnName = new QName(localPart);
        }
        return returnName;
    }

    /**
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
