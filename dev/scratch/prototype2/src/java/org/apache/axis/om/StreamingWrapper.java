package org.apache.axis.om;

import javax.xml.namespace.QName;

import org.apache.axis.impl.llom.exception.OMStreamingException;


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
 * Time: 1:52:09 PM
 * 
 */
public interface StreamingWrapper {
    /*
    ######################################################################
    Following methods are removed. they seem to be duplicates
    */
        //int generatePullEvent();
        //void setCursor(int eventCount);

    /**
     * Allows to set parser wrapper to switch to the stream when
     * needed. Unless this switch is "on" the wrapper will always
     * cache the parsed items. If the need is there to get the pull
     * events <i>without</i> caching then this switch must be "on"
     * @param b
     */
    void setAllowSwitching(boolean b);

    /**
     * get the parser switching flag.
     * @see #setAllowSwitching
     * @return
     */
    boolean isAllowSwitching();


    /*
    ######################################################################
    These methods are actually from the XMLStreamReader interface which are StAX!
    Unwanted ones (such as the ones that include PI's) are removed
    */
     Object getProperty(String s) throws IllegalArgumentException;

    int next() throws OMStreamingException;

    String getElementText() throws OMStreamingException;

    int nextTag() throws OMStreamingException;

    boolean hasNext() throws OMStreamingException;

    void close() throws OMStreamingException;

    String getNamespaceURI(String s);

    boolean isStartElement();

    boolean isEndElement();

    boolean isCharacters();

    boolean isWhiteSpace();

    String getAttributeValue(String s, String s1);

    int getAttributeCount();

    QName getAttributeName(int i);

    String getAttributeNamespace(int i);

    String getAttributeLocalName(int i);

    String getAttributePrefix(int i);

    String getAttributeType(int i);

    String getAttributeValue(int i);

    boolean isAttributeSpecified(int i);

    int getNamespaceCount();

    String getNamespacePrefix(int i);

    String getNamespaceURI(int i);

//        NamespaceContext getNamespaceContext();

    int getEventType();

    String getText();

    char[] getTextCharacters();

    int getTextCharacters(int i, char[] chars, int i1, int i2) throws OMStreamingException;

    int getTextStart();

    int getTextLength();

    boolean hasText();

//        Location getLocation();

    QName getName();

    String getLocalName();

    boolean hasName();

    String getNamespaceURI();

    String getPrefix();



}
