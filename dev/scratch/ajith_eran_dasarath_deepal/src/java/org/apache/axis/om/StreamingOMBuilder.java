package org.apache.axis.om;

import org.apache.axis.om.storage.ElementRow;
import org.apache.axis.om.storage.NodeRow;
import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Stack;

/**
 * Copyright 2001-2004 The Apache Software Foundation. <p/>Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at <p/>
 * http://www.apache.org/licenses/LICENSE-2.0 <p/>Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * 
 * @author Ajith Ranabahu Date: Sep 16, 2004 Time: 10:00:16 PM <p/>This class
 *         accepts pull events and generates the OMTableModel
 */
public class StreamingOMBuilder {

    private XmlPullParser pullparser;

    private OMTableModel tableModel = null;

    private Stack elementStack = new Stack();
    
    private Object previousSibling;


    /**
     * @param pullparser
     */
    public StreamingOMBuilder(XmlPullParser pullparser) {
        this.pullparser = pullparser;

        //////////////////////////////////
        //todo CHECK THIS
        this.tableModel = new OMTableModel(this); //the parser never seems to
        // get the
        //start document event!!!!!!!!!!!!!
        //////////////////////////////////

    }

    public XmlPullParser getPullparser() {
        return pullparser;
    }

    /**
     * Since this is the entry point to the undelying XML representation this
     * method contains the only reference to its own proceed method. it parses
     * the XML until the document is created. In other words it will parse until
     * the START_DOCUMENT event is generated
     *
     * @return
     */
    public Document getDocument() {
        while (tableModel == null) {
            try {
                proceed();
            } catch (OMException e) {
                e.printStackTrace();
                break;
                //just break the loop for now.
                //todo think of a better way to handle this
            }
        }
        return tableModel;
    }

    /**
     * Call the next method once, of pull parser update the Table Model
     * depending on the pull event recd from pull parser.
     *
     * @throws OMException
     */
    public void proceed() throws OMException {

        try {

            int eventType = pullparser.next();
            ;
            //do the necessary to each type of event
            if (eventType == XmlPullParser.START_DOCUMENT) {
                processStartDocument();
            } else if (eventType == XmlPullParser.END_DOCUMENT) {
                processEndDocument();
            } else if (eventType == XmlPullParser.START_TAG) {
                processStartElement();
            } else if (eventType == XmlPullParser.END_TAG) {
                processEndElement();
            } else if (eventType == XmlPullParser.CDSECT) {
                processCDATA();
            } else if (eventType == XmlPullParser.COMMENT) {
                processComment();
            } else if (eventType == XmlPullParser.TEXT) {
                processText();
            } else {
                return;//any other events are not interesting :)
            }

        } catch (XmlPullParserException e) {
            throw new OMException("parser Exception", e);
        } catch (IOException e) {
            throw new OMException("IO Exception", e);
        } catch (Exception e) {
            throw new OMException("Unknown process Exception", e);
        }

    }

    /**
     * Processing the start tag has some more things to than just inserting an
     * elemnt. It has to update the parent and set up the attributes for that
     * particular element
     */
    private void processStartElement() {

        //Check the stack for an element. The one at the peek
        //is this ones parent and this is alwys an element!
        ElementRow parent = null;
        if (!elementStack.isEmpty())
            parent = (ElementRow) elementStack.peek();

        //first insert the elemnt to the model

        Object currentElement = tableModel.addElement(pullparser.getNamespace(), pullparser.getName(), pullparser.getPrefix(), parent, this); //pass this instamce as the builder. It will be
        // in the current xpp implementation we use here (i.e. lxpp) getNamespace will provide the

        // needed by the

        // now check whether the parent has already the firstChild set or not. If not this element is the first child.
        checkFirstChild(currentElement, (ElementRow)parent);

        //fill in the set of attributes
        int attribCount = pullparser.getAttributeCount();

        if (attribCount > 0) {
            Object previousAttribute = null;
            Object currentAttribute = null;
            for (int i = 0; i < attribCount; i++) {
                currentAttribute = tableModel.addAttribute(
                        pullparser.getAttributeName(i),
                        pullparser.getAttributePrefix(i),
                        pullparser.getAttributeNamespace(i),
                        pullparser.getAttributeValue(i),
                        currentElement); //parent element

                //set the next sibling of the previous attribute
                if (previousAttribute != null) {
                    tableModel.updateAttributeSibling(previousAttribute,
                            currentAttribute);
                }
                //swap the attribute references
                previousAttribute = currentAttribute;
            }
        }

        processSiblings(parent, currentElement);

        // push the current element to the stack
        //push to the stack for later use
        elementStack.push(currentElement);

    }

    private void processSiblings(ElementRow parent, Object currentInfoItem) {
        //update siblings
        if (previousSibling != null && !parent.equals(previousSibling)) //this is needed to avoid erroneous references
            ((NodeRow)previousSibling).setNextSibling (currentInfoItem);

        //update prev sibling
        previousSibling = currentInfoItem;
    }

    /**
     * This method will check whether there is a firstChild in the parent. if not
     * this will set the cuurentElement as the first child
     * @param currentElement
     * @param parent
     */ private void checkFirstChild(Object currentElement, ElementRow parent) {
        if( parent != null && parent.getFirstChild() == null){
            parent.setFirstChild(currentElement);
        }
    }

    /**
     * Processsig the end element is simple. Just mark the peek element as
     * processed and pop it from the stack
     */
    private void processEndElement() {
        Object key = elementStack.pop();
        previousSibling = key;
        tableModel.updateElementDone(key);
    }

    private void processStartDocument() {
        //since the table model is THE DOCUMENT make it here
        this.tableModel = new OMTableModel(this);
    }

    private void processEndDocument() {
        this.tableModel.setCompleted(true);
    }

    private void processText() {
        //Check the stack for an element. The one at the peek
        //is this ones parent!
        ElementRow parent = (ElementRow) elementStack.peek();
        if (parent == null) {
            //having text without a parent is not correct XML syntax.
            //ideally this should be taken care of by the parser
            throw new UnsupportedOperationException("Invlid XML");
        }

        Object textRow = tableModel.addText(pullparser.getText(), parent);

        checkFirstChild(textRow, (ElementRow)parent);

        processSiblings(parent, textRow);

    }

    private void processCDATA() {
        //Check the stack for an element. The one at the peek
        //is this ones parent!
        ElementRow parent = (ElementRow) elementStack.peek();
        if (parent == null) {
            //having text without a parent is not correct XML syntax.
            //ideally this should be taken care of by the parser
            throw new UnsupportedOperationException("Invalid XML");
        }

        Object cdata = tableModel.addCData(pullparser.getText(), parent);
        checkFirstChild(cdata, (ElementRow) parent);
        processSiblings(parent, cdata);


    }

    private void processComment() {
        //Check the stack for an element. The one at the peek
        //is this ones parent!
        ElementRow parent = (ElementRow) elementStack.peek();
        if (parent == null) {
            //having text without a parent is not correct XML syntax.
            //ideally this should be taken care of by the parser
            throw new UnsupportedOperationException("Invlid XML");
        }

        Object commentRow = tableModel.addComment(pullparser.getText(), parent);

        checkFirstChild(commentRow, (ElementRow) parent);

        processSiblings(parent, commentRow);



    }
}