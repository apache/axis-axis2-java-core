package org.apache.axis.axiom.infoset;


import java.util.List;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Date: Sep 15, 2004
 * Time: 5:09:34 PM
 * The intention of this information item is to have a formal interface to the
 * Element. This contains a stripped down set of properties from the XML info set
 * specification
 */
public interface OMElement {
    /**
     *
     * @return
     */
    String getNamespaceName();

    /**
     *
     * @param namespaceName
     */
    void setNamespaceName(String namespaceName);

    /**
     *
     * @return
     */
    String getLocalName();

    /**
     *
     * @param localName
     */
    void setLocalName(String localName);

    /**
     *
     * @return
     */
    String getPrefix();

    /**
     *
     * @param prefix
     */
    void setPrefix(String prefix);

    /**
     *
     * @return
     */
    String getText();

    /**
     *
     * @param text
     */
    void setText(String text);

    /**
     * The actual implementation may not need to
     * do this since it may casue the document to
     * be parsed in an unwanted manner
     * @return a child element list
     */
    List getChildren();

    /**
     *
     * @param children a list of child elements
     */
    void setChildren(List children);

    /**
     *
     * @return List of OMattribute objects
     */
    List getAttributes();

    /**
     *
     * @param attributes
     */
    void setAttributes(List attributes);

    /**
     * @return The parent OMelement as described in the info set specification
     */
    OMElement getParent();

    /**
     * @param parent OMelement as described in the info set specification
     */
    void setParent(OMElement parent);

    /**
     * Get the next child of this element. The parser will proceed from the
     * current cursor position
     * @return
     */
    OMElement getNextChild();

    /**
     * returns the particular element with the given local name and the namespace
     * which is the next child in the
     * @param namespace
     * @param localName
     * @return
     */
    OMElement getNextChild(String namespace,String localName);

    /**
     * This will add a child element to the list of children
     * @param child
     */
    void addChild(OMElement child);
}
