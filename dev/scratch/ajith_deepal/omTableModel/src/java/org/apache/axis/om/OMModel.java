package org.apache.axis.om;


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
 * Date: Sep 28, 2004
 * Time: 10:40:52 AM
 *
 * This interface governs the methods Exposed by the "Model" of the OM
 * It is meant to cover the underlying implementation of the model
 *
 */
public interface OMModel {

    /**
     * Add a new element to the model
     * @param localName
     * @param parentElement
     * @return
     */
    int addElement(String localName,int parentElement);

    /**
     * Add an attribute to the model
     * @param localName
     * @param value
     * @param parentElement
     * @return
     */
    int addAttribute(String localName,String value,int parentElement);

    /**
     * Add a text node
     * @param value
     * @param parent
     * @return
     */
    int addText(String value,int parent);

    /**
     * Add a CDATA section
     * @param value
     * @param parent
     * @return
     */
    int addCDATA(String value,int parent);

    /**
     * Add a comment
     * @param value
     * @param parent
     * @return
     */
    int addComment(String value,int parent);

    /**
     * Add a namespace. Namespaces are treated as a seperate entitiy
     * @param URI
     * @param prefix
     * @param declaredElementKey
     * @return
     */
    int addNamespace(String URI,String prefix, int declaredElementKey);

    /**
     * Add an event into this
     * @param type
     * @param reference
     * @return
     */
    int addEvent(int type,int reference);

    /**
     * Removes the element given the key
     * @param key
     * @return
     */
    int removeElement(int key);

    /**
     * remove the attribute given the key
     * @param key
     * @return
     */
    int removeAttribute(int key);

    /**
     * Remove the text node given the key
     * @param key
     * @return
     */
    int removeText(int key);

    /**
     * Remove the event
     * @param key
     * @return
     */
    int removeEvent(int key);

    /**
     * this is a very special method that allows the updation of several
     * attributes. The type parameter determines what should be updated.
     * Similar methods exist for attributes and other infoset items
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     */
    void updateElement(int key,String stringValue,int intValue,int type);
    /**
     * @see #updateElement
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     */
    void updateAttribute(int key,String stringValue,int intValue,int type);

    /**
     * @see #updateElement
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     */
    void updateText(int key,String stringValue,int intValue,int type);

    /**
     * @see #updateElement
     * @param key
     * @param stringValue
     * @param intValue
     * @param type
     */
    void updateNameSpace(int key,String stringValue,int intValue,int type);

    /**
     *
     * @param key
     * @return OMElement object
     */
    OMElement getElement(int key);

    /**
     *
     * @param key
     * @return
     */
    OMAttribute getAttribute(int key);

    /**
     *
     * @param key
     * @return
     */
    OMText getText(int key);

    /**
     *
     * @param key
     * @return
     */
    OMNamespace getNamespace(int key);

    /**
     *
     * @return the docuement object
     */
    OMDocument getDocument();

    /**
     *
     * @param key
     * @return
     */
    Object getEvent(int key);

    /**
     *
     * @param key
     * @return
     */
    int getPullEvent(int key);

    /**
     *
     * @return the number of elements (including the removed ones)
     */
    int getElementCount();

    /**
     *
     * @return The attribute count
     */
    int getAttributeCount();

    /**
     *
     * @return the number of text items
     */
    int getTextCount();

    /**
     *
     * @return the number of events
     */
    int getEventCount();

    /**
     * Set the caching off
     */
    void setCacheOff();

    /**
     * Return the parser underneath
     * @return
     */
    XmlPullParser getParser();

    /**
     * Move the underlying build mechanism one even further
     * @return
     */
    int proceed();

    /**
     * A flag saying whether the model is completed
     * @return
     */
    boolean isComplete();

    /**
     * Set the completed flag
     * @param b
     */
    void setComplete(boolean b);

    /**
     * Returns the status of the particular object
     * Used for synchronization
     * @param key
     * @param type
     * @return
     */
    Object update(int key,int type);

    /**
     * Resolve the namespace
     * @param parent
     * @param uri
     * @param prefix
     * @return
     */
    int resolveNamespace(int parent,String uri,String prefix);
}
