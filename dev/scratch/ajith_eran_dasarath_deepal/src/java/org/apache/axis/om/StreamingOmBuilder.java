package org.apache.axis.om;

import java.io.IOException;
import java.util.Stack;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
 *         Time: 10:00:16 PM
 *         <p/>
 *         This class accepts pull events and generates the OMTableModel
 */
public class StreamingOmBuilder {

    private XmlPullParser pullparser;
    private OMTableModel tableModel=null;
    private Stack elementStack = new Stack();

    /**
     * @param pullparser
     */
    public StreamingOmBuilder(XmlPullParser pullparser) {
        this.pullparser = pullparser;
        //////////////////////////////////
        //todo CHECK THIS
         this.tableModel = new OMTableModel(this); //the parser never seems to get the
                                                //start document event!!!!!!!!!!!!!
        //////////////////////////////////

    }

    public XmlPullParser getPullparser() {
        return pullparser;
    }

    /**
     * Since this is the entry point to the undelying XML representation
     * this method contains the only reference to its own proceed method.
     * it parses the XML until the document is created.
     * In other words it will parse until the START_DOCUMENT event is generated
     * @return
     */
    public Document getDocument() {
        while (tableModel==null){
            try {
                proceed();
            } catch (OMBuilderException e) {
                e.printStackTrace();
                break;
                //just break the loop for now.
                //todo think of a better way to handle this
            }
        }
        return tableModel;
    }

    /**
     * Call the next method once, of pull parser
     * update the Table Model depending on the pull event recd from pull parser.
     *
     * @throws OMBuilderException
     */
    public void proceed() throws OMBuilderException {

        try {
            pullparser.next();
            int eventType = pullparser.getEventType();
            //do the necessary to each type of event
            if (eventType==XmlPullParser.START_DOCUMENT){
                processStartDocument();
            }else if (eventType==XmlPullParser.END_DOCUMENT){
                processEndDocument();
            }else if (eventType==XmlPullParser.START_TAG){
                processStartElement();
            }else if (eventType==XmlPullParser.END_TAG){
                processEndElement();
            }else if (eventType==XmlPullParser.CDSECT){
                processCDATA();
            }else if (eventType==XmlPullParser.COMMENT){
                processComment();
            }else if (eventType==XmlPullParser.TEXT){
                processText();
            }else{
                return ;//any other events are not interesting :)
            }

        } catch (XmlPullParserException e) {
            throw new OMBuilderException("parser Exception",e);
        } catch (IOException e) {
            throw new OMBuilderException("IO Exception",e);
        } catch (Exception e) {
            throw new OMBuilderException("Unknown process Exception",e);
        }


    }

    /**
     * Processing the start tag has some more things to than just inserting an
     * elemnt. It has to update the parent and set up the attributes for that particular
     * element
     */
    private void processStartElement(){

        //Check the stack for an element. The one at the peek
        //is this ones parent!
        Object parent  =null;
        if (!elementStack.isEmpty())
            parent = elementStack.peek();

        //first insert the elemnt to the model
        Object currentElement = tableModel.addElement(pullparser.getNamespace(),
                pullparser.getName(),
                "",  //where am I supposed to get this
                parent,
                this); //pass this instamce as the builder. It will be needed by the

        //fill in the set of attributes
        int attribCount =  pullparser.getAttributeCount();
        Object previousAttribute = null;
        Object currentAttribute = null;
        for (int i = 0; i < attribCount; i++) {
            currentAttribute  = tableModel.addAttribute(
                    pullparser.getAttributeName(i),
                    pullparser.getAttributePrefix(i),
                    pullparser.getAttributeNamespace(i),
                    pullparser.getAttributeValue(i),
                    currentElement); //parent element

            //set the next sibling of the previous attribute
            if (previousAttribute!=null){
                tableModel.updateAttributeSibling(previousAttribute,currentAttribute);
            }
            //swap the attribute references
            previousAttribute = currentAttribute ;
        }

        //update siblings
        //todo this needs to be written!! How am I supposed to do this? By a Queue?

        //push to the stack for later use
        elementStack.push(currentElement);


    }

    /**
     * Processsig the end element is simple. Just mark the peek element as
     * processed and pop it from the stack
     */
    private void processEndElement(){
        tableModel.updateElementDone(elementStack.pop());
    }

    private void processStartDocument(){
        //since the table model is THE DOCUMENT  make it here
        this.tableModel = new OMTableModel(this);
    }

    private void processEndDocument(){
        this.tableModel.setCompleted(true);
    }

    private void processText(){
        //Check the stack for an element. The one at the peek
        //is this ones parent!
        Object parent = elementStack.peek();
        if (parent==null){
            //having text without a parent is not correct XML syntax.
            //ideally this should be taken care of by the parser
            throw new UnsupportedOperationException("Invlid XML");
        }

        tableModel.addText(pullparser.getText(),parent);


    }

    private void processCDATA(){
        //Check the stack for an element. The one at the peek
        //is this ones parent!
        Object parent = elementStack.peek();
        if (parent==null){
            //having text without a parent is not correct XML syntax.
            //ideally this should be taken care of by the parser
            throw new UnsupportedOperationException("Invlid XML");
        }

        tableModel.addCData(pullparser.getText(),parent);

    }

    private void processComment(){
        //Check the stack for an element. The one at the peek
        //is this ones parent!
        Object parent = elementStack.peek();
        if (parent==null){
            //having text without a parent is not correct XML syntax.
            //ideally this should be taken care of by the parser
            throw new UnsupportedOperationException("Invlid XML");
        }

        tableModel.addComment(pullparser.getText(),parent);

    }
}
