package org.apache.axis.om;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.*;
import org.apache.axis.om.pool.OMAttributePool;
import org.apache.axis.om.pool.OMElementPool;
import org.apache.axis.om.pool.OMNameSpacePool;
import org.apache.axis.om.pool.OMTextPool;
import org.apache.axis.om.storage.table.*;
import org.apache.axis.om.util.OMConstants;
import org.xmlpull.v1.XmlPullParser;

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
 * Date: Sep 27, 2004
 * Time: 6:29:36 PM
 *
 */
public class OMModelImpl implements OMModel {

    private ElementTable elementTable = new ElementTable();
    private AttributeTable attribTable = new AttributeTable();
    private TextTable textTable = new TextTable();
    private EventTable eventTable = new EventTable();
    private NameSpaceTable nameSpaceTable = new NameSpaceTable();
    private boolean completed=false;
    private StreamingOMBuilder builder = null;


    public OMModelImpl() {
    }

    /**
     * Needs a builder for the model
     * @param builder
     */
    public OMModelImpl(StreamingOMBuilder builder) {
        this.builder = builder;
    }

    /**
     * The builder can either be passed in construction or set as an attribute
     * However a new builder cannot be set once a builder is set
     * @param builder
     */
    public void setBuilder(StreamingOMBuilder builder) {
        if (this.builder ==null)
            this.builder = builder;
    }


    /**
     * @see org.apache.axis.om.OMModel#addElement
     * @param localName
     * @param parentElement
     * @return
     */
    public int addElement(String localName, int parentElement) {
        return elementTable.addElement(localName,parentElement);
    }

    /**
     * @see org.apache.axis.om.OMModel#addAttribute
     * @param localName
     * @param value
     * @param parentElement
     * @return
     */
    public int addAttribute(String localName, String value, int parentElement) {
        return attribTable.addAttribute(localName,value,parentElement);
    }

    /**
     * @see org.apache.axis.om.OMModel#addText
     * @param value
     * @param parent
     * @return
     */
    public int addText(String value, int parent) {
        return textTable.addText(value,OMConstants.TEXT,parent);
    }

    /**
     * @see org.apache.axis.om.OMModel#addCDATA
     * @param value
     * @param parent
     * @return
     */
    public int addCDATA(String value, int parent) {
        return textTable.addText(value,OMConstants.CDATA,parent);
    }

    /**
     * @see org.apache.axis.om.OMModel#addComment
     * @param value
     * @param parent
     * @return
     */
    public int addComment(String value, int parent) {
        return textTable.addText(value,OMConstants.COMMENT,parent);
    }

    /**
     * @see org.apache.axis.om.OMModel#addNamespace
     * @param URI
     * @param prefix
     * @param declaredElementKey
     * @return
     */
    public int addNamespace(String URI, String prefix, int declaredElementKey) {
        return nameSpaceTable.addNamespace(URI,prefix,declaredElementKey);
    }

    /**
     * @see org.apache.axis.om.OMModel#updateElement
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     *
     * supports the following  updations
     * done,namespace,next sibling,first child,first attribute
     */
    public void updateElement(int key, String stringValue, int intValue, int type) {

        if (type==OMConstants.UPDATE_DONE){
            elementTable.updateDone(key);
        }else if (type==OMConstants.UPDATE_NAMESPACE) {
            elementTable.updateNameSpace(key,intValue);
        }else if(type == OMConstants.UPDATE_NEXT_SIBLING){
            elementTable.updateNextSibling(key,
                    intValue,
                    Integer.parseInt(stringValue));//the type of the element must be passed
            //as the string value argument
        } else if(type == OMConstants.UPDATE_FIRST_CHILD){
            elementTable.updateFirstChild(key,
                    intValue,
                    Integer.parseInt(stringValue));//the type of the element must be passed
            //as the string value argument
        }else if(type == OMConstants.UPDATE_FIRST_ATTRIBUTE){
            elementTable.updateFirstAttribute(key,intValue);
        }

    }

    /**
     * @see org.apache.axis.om.OMModel#updateAttribute
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     * Supports updating namespace and next sibling
     */
    public void updateAttribute(int key, String stringValue, int intValue, int type) {

        if (type==OMConstants.UPDATE_NAMESPACE) {
            attribTable.updateNameSpace(key,intValue);
        }else if(type == OMConstants.UPDATE_NEXT_SIBLING){
            attribTable.updateNextSibling(key,intValue);
        }


    }

    /**
     * @see org.apache.axis.om.OMModel#updateText
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     * supports updating next sibling
     */
    public void updateText(int key, String stringValue, int intValue, int type) {

        if(type == OMConstants.UPDATE_NEXT_SIBLING){

            textTable.updateNextSibling(key,
                    intValue,
                    Integer.parseInt(stringValue));//the type of the element must be passed as the string value argument
        }

    }


    /**
     *
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     * Not yet supported!!!
     */
    public void updateNameSpace(int key, String stringValue, int intValue, int type) {

    }

    /**
     * @see org.apache.axis.om.OMModel#isComplete
     * @return
     */
    public boolean isComplete() {
        return completed;
    }

    /**
     * @see org.apache.axis.om.OMModel#setComplete
     * @param b
     */
    public void setComplete(boolean b) {
        this.completed = b;
    }


    /**
     * @see org.apache.axis.om.OMModel#getElement
     * @param key
     * @return
     */
    public OMElement getElement(int key) {
        OMElementImpl element = (OMElementImpl)OMElementPool.getInstance().getPooledObject();
        element.init(this,key,this.elementTable.getElement(key));
        return element;
    }

    /**
     * @see org.apache.axis.om.OMModel#getAttribute
     * @param key
     * @return
     */
    public OMAttribute getAttribute(int key) {
        OMAttributeImpl attrib = (OMAttributeImpl)OMAttributePool.getInstance().getPooledObject();
        attrib.init(this,key,this.attribTable.getAttribute(key));
        return attrib;
    }

    /**
     * @see org.apache.axis.om.OMModel#getText
     * @param key
     * @return
     */
    public OMText getText(int key) {
        OMTextImpl text = (OMTextImpl)OMTextPool.getInstance().getPooledObject();
        text.init(this,key,textTable.getText(key));
        return text;
    }

    /**
     * @see org.apache.axis.om.OMModel#getNamespace
     * @param key
     * @return
     */
    public OMNamespace getNamespace(int key) {
        OMNameSpaceImpl nameSpace = (OMNameSpaceImpl)OMNameSpacePool.getInstance().getPooledObject();
        nameSpace.init(this,key,nameSpaceTable.getNamespace(key));
        return nameSpace;
    }

    /**
     * @see org.apache.axis.om.OMModel#addEvent
     * @param type
     * @param reference
     * @return
     */
    public int addEvent(int type, int reference) {
        return eventTable.addEvent(type,reference);
    }

    /**
     * @see org.apache.axis.om.OMModel#getPullEvent
     * @param key
     * @return
     */
    public int getPullEvent(int key) {
        //todo fill this
        return eventTable.getPullEvent(key);


    }

    /**
     * @see org.apache.axis.om.OMModel#getEvent
     * @param key
     * @return
     */
    public Object getEvent(int key) {
        return eventTable.getEvent(key);


    }

    /**
     * @see org.apache.axis.om.OMModel#proceed
     * @return
     */
    public int proceed() {
        return this.builder.proceed();
    }


    /**
     * @see org.apache.axis.om.OMModel#getDocument
     * @return
     */
    public OMDocument getDocument() {
        return new OMDocumentImpl(this,
                OMConstants.DEFAULT_INT_VALUE,
                null);

    }

    /**
     * @see org.apache.axis.om.OMModel#update
     * @param key
     * @param type
     * @return
     */
    public Object update(int key, int type) {
        if (type==OMConstants.ELEMENT){
            return elementTable.getElement(key);
        }else  if (type==OMConstants.ATTRIBUTE){
            return attribTable.getAttribute(key);
        }else if (type==OMConstants.TEXT){
            return textTable.getText(key);
        }else if (type==OMConstants.CDATA){
            return textTable.getText(key);
        }else if (type==OMConstants.COMMENT){
            return textTable.getText(key);
        }
        return null;
    }

    /**
     * @return the root element. Uilised by internal methods
     */
    public OMElement getRootElement(){
        int size = elementTable.getSize();
        OMElementImpl elt=null;
        for (int i = 0; i < size; i++) {
            elt = (OMElementImpl)getElement(i);
            if (elt.getParentKey()==OMConstants.DEFAULT_INT_VALUE)
                return elt;

        }
        return null;
    }

    /**
     * gets the first child
     * @param key
     * @return
     */
    public int getFirstChild(int key) {
        return elementTable.getFirstChild(key);
    }

    /**
     * @see org.apache.axis.om.OMModel#getElementCount
     * @return
     */
    public int getElementCount() {
        return elementTable.getSize();
    }

    /**
     * @see org.apache.axis.om.OMModel#getAttributeCount
     * @return
     */
    public int getAttributeCount() {
        return attribTable.getSize();
    }

    /**
     * @see org.apache.axis.om.OMModel#getTextCount
     * @return
     */
    public int getTextCount() {
        return textTable.getSize();
    }

    /**
     * @see org.apache.axis.om.OMModel#getEventCount
     * @return
     */
    public int getEventCount() {
        return eventTable.getSize();
    }

    /**
     * @see org.apache.axis.om.OMModel#setCacheOff
     */
    public void setCacheOff() {
        builder.setCache(false);
    }

    /**
     * @see org.apache.axis.om.OMModel#getParser
     * @return the parser if the cache is off, otherwise throws an exception
     */
    public XmlPullParser getParser() {
        if (!builder.isCache())
            return builder.getPullparser();
        else
            throw new OMException("cache is on!!");

    }

    /**
     * removes the element
     * @param key
     * @return
     * Note - not yet implemented
     */
    public int removeElement(int key) {
        return 0;
    }


    /**
     * removes the given attribute
     * @param key
     * @return
     * Note - parent element must adjust itself when the child is the
     * first child
     */
    public int removeAttribute(int key) {
        return attribTable.removeAttribute(key);
    }


    /**
     *
     * @param key
     * @return
     * Note - not yet implemented
     */
    public int removeText(int key) {
        return 0;
    }

    /**
     *
     * @param key
     * @return
     * Note - not yet implemented
     */
    public int removeEvent(int key) {
        return 0;
    }


    /**
     * @see org.apache.axis.om.OMModel#resolveNamespace
     * @param parent
     * @param uri
     * @param prefix
     * @return
     */
    public int resolveNamespace(int parent,String uri, String prefix) {
        int namespace = nameSpaceTable.findNamespace(parent,uri,prefix);

        while (namespace==OMConstants.DEFAULT_INT_VALUE){
            //get the parent element and try to resolve it there
            parent = elementTable.getParent(parent);
            if (parent==OMConstants.DEFAULT_INT_VALUE){
                //we have reached the root. So break out. (there is no point of searching anymore
                break;
            }else{
                namespace =  nameSpaceTable.findNamespace(parent,uri,prefix);
            }
        }

        return namespace;
    }

    /**
     * Debugging method to dump the content of the model
     */
    public void dump(){

        elementTable.dumpValues();
        System.out.println("----------------------------");
        attribTable.dumpValues();
        System.out.println("----------------------------");
        textTable.dumpValues();
        System.out.println("----------------------------");
        //eventTable.dumpValues();
        //System.out.println("----------------------------");
        nameSpaceTable.dumpValues();
        System.out.println("----------------------------");

    }

}
