package org.apache.axis.om;

import org.apache.axis.om.util.IntegerStack;
import org.apache.axis.om.util.OMConstants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


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
 * @author Ajith Ranabahu
 * Date: Sep 16, 2004
 * Time: 10:00:16 PM
 * This class accepts pull events and generates the OMModel. It encapsultes
 * all the logic that is needed to build the model. And most importantly
 * the builder is unaware of the underlying storage mechanism since it
 * accesses the model through the model interface
 */
public class StreamingOMBuilder {

    private XmlPullParser pullparser;
    private OMModel tableModel = null;
    private IntegerStack stack = new IntegerStack();

    private int lastSibling = OMConstants.DEFAULT_INT_VALUE;
    private int lastSiblingType= OMConstants.DEFAULT_INT_VALUE;

    private boolean cache = true;

    /**
     * Cache status
     * @return
     */
    public boolean isCache() {
        return cache;
    }

    /**
     * Set the cache status
     * @param cache
     */
    public void setCache(boolean cache) {
        this.cache = cache;
    }

    /**
     * @param pullparser to be used. this cannot be changed
     * during the build cycle
     */
    public StreamingOMBuilder(XmlPullParser pullparser) {
        this.pullparser = pullparser;
        this.tableModel = new OMModelImpl(this);
    }

    /**
     *
     * @return  the OMModel inside
     */
    public OMModel getTableModel() {
        return tableModel;
    }

    /**
     *
     * @return XMLpullparser The parser within
     */
    public XmlPullParser getPullparser() {
        return pullparser;
    }

    /**
     * Call the next method once, of pull parser update the Table Model
     * depending on the pull event recd from pull parser.
     *
     * @throws OMException
     */
    public int proceed() throws OMException {
        int eventType = OMConstants.DEFAULT_INT_VALUE;
        try {

            eventType = pullparser.next();
            if (cache) {
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
                }
            }
        } catch (XmlPullParserException e) {
            throw new OMException("parser Exception", e);
        } catch (IOException e) {
            throw new OMException("IO Exception", e);
        } catch (Exception e) {
            throw new OMException("Unknown process Exception", e);
        }

        return eventType;
    }

    /**
     * Processing the start tag has some more things to than just inserting an
     * elemnt. It has to update the parent and set up the attributes for that
     * particular element
     */
    private void processStartElement()  throws XmlPullParserException{

        int parent = OMConstants.DEFAULT_INT_VALUE;
        if(!stack.isEmpty()){
            parent = stack.peek();
        }

        //insert the element
        int elementKey = tableModel.addElement(pullparser.getName(),
                parent);

        //update the namespaces
        int nsStart = pullparser.getNamespaceCount(pullparser.getDepth()-1);
        int nsEnd = pullparser.getNamespaceCount(pullparser.getDepth());
        for (int i = nsStart; i < nsEnd; i++) {
            String prefix = pullparser.getNamespacePrefix(i);
            String ns = pullparser.getNamespaceUri(i);
            tableModel.addNamespace(
                    ns,
                    prefix,
                    elementKey
            );
        }

        //set up this elements namespace
        int namespacekey = this.resolveNamespace(elementKey,
                pullparser.getNamespace(),
                pullparser.getPrefix());
        if (namespacekey!= OMConstants.DEFAULT_INT_VALUE){
            tableModel.updateElement(elementKey,
                    OMConstants.DEFAULT_STRING_VALUE,
                    namespacekey,
                    OMConstants.UPDATE_NAMESPACE);
        }


        //insert the attributes
        int attribCount = pullparser.getAttributeCount();
        int currentAttribKey;
        int previousAttribKey = OMConstants.DEFAULT_INT_VALUE;

        for (int i = 0; i < attribCount; i++) {
            currentAttribKey = tableModel.addAttribute(
                    pullparser.getAttributeName(i),
                    pullparser.getAttributeValue(i),
                    elementKey );

            //add the first attribute
            if (i==0){
                tableModel.updateElement(elementKey,
                        OMConstants.DEFAULT_STRING_VALUE,
                        currentAttribKey,
                        OMConstants.UPDATE_FIRST_ATTRIBUTE);
            }

            //update the attribute namespacs
            namespacekey = this.resolveNamespace(elementKey,
                    pullparser.getAttributeNamespace(i),
                    pullparser.getAttributePrefix(i));
            if (namespacekey!= OMConstants.DEFAULT_INT_VALUE){
                tableModel.updateAttribute(currentAttribKey,
                        OMConstants.DEFAULT_STRING_VALUE,
                        namespacekey,
                        OMConstants.UPDATE_NAMESPACE);
            }

            //update the next sibling of the attribute
            if (previousAttribKey!=OMConstants.DEFAULT_INT_VALUE){
                tableModel.updateAttribute(previousAttribKey,
                        OMConstants.DEFAULT_STRING_VALUE,
                        currentAttribKey,
                        OMConstants.UPDATE_NEXT_SIBLING);
            }
            previousAttribKey = currentAttribKey;
        }

        //insert the event
        tableModel.addEvent(XmlPullParser.START_TAG,elementKey);

        //update the first child
        //NOTE - order is *very* important here
        updateFirstChild(elementKey,OMConstants.ELEMENT);
        //update the sibling
        updateLastSibling(elementKey,OMConstants.ELEMENT);

        stack.push(elementKey);

    }

    /**
     * Processsig the end element is simple. Just mark the peek element as
     * processed and pop it from the stack
     */
    private void processEndElement() {

        //mark the element as processed
        int key = stack.pop();
        tableModel.updateElement(key,
                null,
                OMConstants.DEFAULT_INT_VALUE,
                OMConstants.UPDATE_DONE);

        //insert the event
        tableModel.addEvent(XmlPullParser.END_TAG,key);

        //update the siblings
        lastSibling = key;
        lastSiblingType = OMConstants.ELEMENT;
    }

    /**
     * Process the start docuemnt. this may not
     * be visible at anytime
     */
    private void processStartDocument() {
        //do nothing!!!!!!!!!
    }

    /**
     * Process the end of the document
     */
    private void processEndDocument() {
        //mark the document as done
        tableModel.setComplete(true);
        tableModel.addEvent(XmlPullParser.END_DOCUMENT,OMConstants.DEFAULT_INT_VALUE);
    }

    /**
     * Process a text node
     */
    private void processText() {

        int key = tableModel.addText(
                pullparser.getText(),
                stack.peek() //parent
        );

        //update the first child
        //NOTE - order is *very* important here
        updateFirstChild(key,OMConstants.TEXT);
        updateLastSibling(key,OMConstants.TEXT);
        tableModel.addEvent(XmlPullParser.TEXT,key);

    }

    /**
     * process a CDATA section
     */
    private void processCDATA() {
        int key = tableModel.addCDATA(
                pullparser.getText(),
                stack.peek() //parent
        );

        //update the first child
        //NOTE - order is *very* important here
        updateFirstChild(key,OMConstants.CDATA);
        updateLastSibling(key,OMConstants.CDATA);
        tableModel.addEvent(XmlPullParser.CDSECT,key);
    }

    /**
     * Process a comment
     */
    private void processComment() {
        int key = tableModel.addComment(
                pullparser.getText(),
                stack.peek() //parent
        );

        //update the first child
        //NOTE - order is *very* important here
        updateFirstChild(key,OMConstants.COMMENT);
        updateLastSibling(key,OMConstants.COMMENT);
        tableModel.addEvent(XmlPullParser.COMMENT,key);
    }

    /**
     *
     * @param key
     * @param type
     */
    private void updateFirstChild(int key,int type){

        if (stack.isEmpty())
            return; //stack is empty. Just return


        //at this point the siblings are not yet updated. So if the last sibling (being an element) is
        //still the parent then that means we have the first child here
        if (lastSiblingType==OMConstants.ELEMENT && lastSibling == stack.peek()){
            tableModel.updateElement(stack.peek(),
                    type+"",
                    key,
                    OMConstants.UPDATE_FIRST_CHILD);
        }

    }

    /**
     *
     * @param currentKey
     * @param type
     */
    private void updateLastSibling(int currentKey,int type){

        if (lastSibling != OMConstants.DEFAULT_INT_VALUE ){
            if (!(lastSiblingType==OMConstants.ELEMENT && lastSibling==stack.peek())){
                //ths means the last sibling pointer is actually set to the parent element
                if (lastSiblingType==OMConstants.ELEMENT){
                    tableModel.updateElement(
                            lastSibling,
                            type+"", //type
                            currentKey,
                            OMConstants.UPDATE_NEXT_SIBLING);
                }else if (lastSiblingType==OMConstants.TEXT ||
                        lastSiblingType==OMConstants.CDATA ||
                        lastSiblingType==OMConstants.COMMENT){
                    tableModel.updateText(
                            lastSibling,
                            type+"", //type
                            currentKey,
                            OMConstants.UPDATE_NEXT_SIBLING);
                }
            }
        }
        lastSibling = currentKey;
        lastSiblingType = type;
    }

    /**
     *
     * @param elementKey
     * @param uri
     * @param prefix
     * @return
     */
    public int resolveNamespace(int elementKey,String uri,String prefix){
        return tableModel.resolveNamespace(elementKey,uri,prefix);
    }

}