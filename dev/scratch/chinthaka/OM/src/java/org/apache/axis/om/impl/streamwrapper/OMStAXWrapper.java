package org.apache.axis.om.impl.streamwrapper;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.OMNavigator;
import org.apache.xml.utils.QName;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
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
public class OMStAXWrapper implements StreamingWrapper,XMLStreamConstants{


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
    private boolean  navigable = true;

    // SwitchingAllowed is set to false by default
    // this means that unless the user explicitly states
    // that he wants things not to be cached, everything will
    // be cached
    boolean switchingAllowed=false;

    private Stack elementStack = new Stack();

    //keeps the next event. The parser actually keeps one step ahead to
    //detect the end of navigation. (at the end of the stream the navigator
    //returns a null
    private OMNode nextNode=null;

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
    public OMStAXWrapper(OMXMLParserWrapper builder,OMElement startNode) {
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
        String returnStr = "";
        if (navigable){

        }else{

        }
        return returnStr;
    }

    public String getNamespaceURI() {
        return null;
    }

    public boolean hasName() {
        return false;
    }

    public String getLocalName() {
        return null;
    }

    public QName getName() {
        return null;
    }

    public boolean hasText() {
        return false;
    }

    public int getTextLength() {
        return 0;
    }

    public int getTextStart() {
        return 0;
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws OMStreamingException {
        return 0;
    }

    public char[] getTextCharacters() {
        return new char[0];
    }

    public String getText() {
        return null;
    }

    public int getEventType() {
        return 0;
    }

    public String getNamespaceURI(int i) {
        return null;
    }

    public String getNamespacePrefix(int i) {
        return null;
    }

    public int getNamespaceCount() {
        return 0;
    }

    public boolean isAttributeSpecified(int i) {
        return false;
    }

    public String getAttributeValue(int i) {
        return null;
    }

    public String getAttributeType(int i) {
        return null;
    }

    public String getAttributePrefix(int i) {
        return null;
    }

    public String getAttributeLocalName(int i) {
        return null;
    }

    public String getAttributeNamespace(int i) {
        return null;
    }

    public QName getAttributeName(int i) {
        return null;
    }

    /**
     * @see javax.xml.stream.XMLStreamReader#getAttributeCount
     * @return
     */
    public int getAttributeCount() {
        if (isStartElement() || currentEvent==ATTRIBUTE){
            if (navigable){
                //get attribute count from the current node
            }else{
                //get attribute count from the parser
            }
            return 0;
        }else{
            throw new IllegalStateException("attribute count accessed in illegal event!");
        }

    }

    public String getAttributeValue(String s, String s1) {
        return null;
    }

    public boolean isWhiteSpace() {
        return false;
    }

    public boolean isCharacters() {
        return (currentEvent==CHARACTERS);
    }

    public boolean isEndElement() {
        return (currentEvent==END_ELEMENT);
    }

    public boolean isStartElement() {
        return (currentEvent==START_ELEMENT);
    }

    public String getNamespaceURI(String s) {
        return null;
    }

    public void close() throws OMStreamingException {
    }

    public boolean hasNext() throws OMStreamingException {
        return !complete;
    }

    public int nextTag() throws OMStreamingException {
        return 0;
    }

    public String getElementText() throws OMStreamingException {
        return null;
    }

    public int next() throws OMStreamingException {

        if (complete){
            throw new OMStreamingException("Parser completed!");
        }

        if (navigable){
//            System.out.println("--------------");
//            System.out.println("currentNode = " + currentNode);
//            System.out.println("currentNode = " + currentNode.getValue());

            currentEvent = generateEvents(currentNode);
            updateCompleteStatus();
            updateNextNode();
        }else{
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
    private void updateNextNode() throws OMStreamingException{

        currentNode = nextNode;

        if (navigator.isNavigable()){
            nextNode = navigator.next();
        }else{
            if (!switchingAllowed){
                if (navigator.isCompleted()){
                    nextNode=null;
                }else{
                    builder.next();
                    navigator.step();
                    nextNode = navigator.next();
                }
            }else{
                //reset caching (the default is ON so it was not needed in the
                //earlier case!
                builder.setCache(false);
                //load the parser
                try {
                    parser = (XMLStreamReader)builder.getParser();
                } catch (ClassCastException  e) {
                    throw new OMStreamingException("incompatible parser found!",e);
                }
                //set navigable to false.Now the subsequent requests will be directed to
                //the parser
                navigable =false;


            }
        }
    }

    private void updateCompleteStatus(){
        //todo this needs to be done correctly
        if (!navigable){
            complete = builder.isCompleted();
        }else{
            if (rootNode.equals(currentNode))
                if (isFirst)
                    isFirst=false;
                else
                    complete=true;
        }
    }
    /*
    ################################################################
    Generator methods for the OMNodes returned by the navigator
    ################################################################
    */

    private int generateEvents(OMNode node){
        int returnEvent=0;

        int nodeType = node.getType();
        switch (nodeType){
            case OMNode.ELEMENT_NODE:
                returnEvent = generateElementEvents((OMElement)node);
                break;
            case OMNode.TEXT_NODE:
                returnEvent = generateTextEvents();
                break;
            case OMNode.COMMENT_NODE:
                returnEvent =generateCommentEvents();
                break;
            case OMNode.CDATA_SECTION_NODE:
                returnEvent =generateCdataEvents();
                break;
            default:break; //just ignore any other nodes
        }

        return returnEvent;
    }


    private int generateElementEvents(OMElement elt){

        int returnValue = START_ELEMENT;
        if (!elementStack.isEmpty()&& elementStack.peek().equals(elt)){
            returnValue = END_ELEMENT;
            elementStack.pop();
        }else{
            elementStack.push(elt);
        }
        return returnValue;
    }


    private int generateTextEvents(){
        return CHARACTERS;
    }

    private int generateCommentEvents(){
        return COMMENT;
    }

    private int generateCdataEvents(){
        return CDATA;
    }



}
