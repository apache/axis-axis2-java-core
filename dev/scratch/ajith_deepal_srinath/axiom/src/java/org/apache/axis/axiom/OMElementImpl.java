package org.apache.axis.axiom;

import org.apache.axis.axiom.infoset.OMElement;

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
 *
 * Date: 15-Sep-2004
 * Time: 13:46:46
 * To change this template use Options | File Templates.
 */
public class OMElementImpl implements OMElement {
    private String namespaceName;
    private String localName;
    private String prefix;
    private String text;
    private List children;
    private List attributes;
    private OMElement parent; //OMElementImpl????? what about the root that needs the document object as the parent???

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List getChildren() {
        // we throw an unsupported operation exception since we
        // dont want to parse the whole thing
        throw new UnsupportedOperationException();
    }

    public void setChildren(List children) {
        this.children = children;
    }

    /**
     * This returns a list of OMAttributes
     * @return
     */
    public List getAttributes() {
        return attributes;
    }

    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }

    public OMElement getParent() {
        return parent;
    }

    public void setParent(OMElement parent) {
        this.parent = parent;
    }

    //newly introduced methods

    /**
     * Get the next child of this element. The parser will proceed from the
     * current cursor position
     * @return
     */
    public OMElement getNextChild(){
        return null;
    }

    /**
     * returns the particular element with the given local name and the namespace
     * which is the next child in the
     * @param namespace
     * @param localName
     * @return
     */
    public OMElement getNextChild(String namespace,String localName){
        return null;
    }

    /**
     * This will add a child element to the list of children
     * @param child
     */
    public void addChild(OMElement child){

    }
}
