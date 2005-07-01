/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */
package org.apache.axis2.om.impl.llom;

import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenIterator;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenQNameIterator;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Class OMDocument
 */
public class OMDocument implements OMContainer {
    /**
     * Field rootElement
     */
    private OMElement rootElement;
    
    /**
     * Field firstChild
     */
    protected OMNode firstChild;
    
    /**
     * Field lastChild
     */
    private OMNode lastChild;
    
    /**
     * Field done
     */
    protected boolean done = false;

    /**
     * Field parserWrapper
     */
    private OMXMLParserWrapper parserWrapper;

    /**
     * @param rootElement
     * @param parserWrapper
     */
    public OMDocument(OMElement rootElement, OMXMLParserWrapper parserWrapper) {
        this.rootElement = rootElement;
        this.parserWrapper = parserWrapper;
    }

    /**
     * @param parserWrapper
     */
    public OMDocument(OMXMLParserWrapper parserWrapper) {
        this.parserWrapper = parserWrapper;
    }

    /**
     * Method getRootElement
     *
     * @return
     */
    public OMElement getRootElement() {
        while (rootElement == null) {
            parserWrapper.next();
        }
        return rootElement;
    }

    /**
     * Method setRootElement
     *
     * @param rootElement
     */
    public void setRootElement(OMElement rootElement) {
        this.rootElement = rootElement;
    }
    
    /**
     * this will indicate whether parser has parsed this information item completely or not.
     * If somethings info are not available in the item, one has to check this attribute to make sure that, this
     * item has been parsed completely or not.
     *
     * @return
     */
    public boolean isComplete() {
        return done;
    }

    /**
     * Method setComplete
     *
     * @param state
     */
    public void setComplete(boolean state) {
        this.done = state;
    }
    
    /**
     * This will force the parser to proceed, if parser has not yet finished with the XML input
     */
    public void buildNext() {
        parserWrapper.next();
    }

    /**
     * This will add child to the element. One can decide whether he append the child or he adds to the
     * front of the children list
     *
     * @param child
     */
    public void addChild(OMNode child) {
        addChild((OMNodeImpl) child);
    }
    
    /**
     * Method addChild
     *
     * @param child
     */
    private void addChild(OMNodeImpl child) {
        if (firstChild == null) {
            firstChild = child;
            child.setPreviousSibling(null);
        } else {
            child.setPreviousSibling(lastChild);
            lastChild.setNextSibling(child);
        }
        child.setNextSibling(null);
        child.setParent(this);
        lastChild = child;

    }

    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     *
     * @return
     */
    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstChild());
    }

    /**
     * This will search for children with a given QName and will return an iterator to traverse through
     * the OMNodes.
     * This QName can contain any combination of prefix, localname and URI
     *
     * @param elementQName
     * @return
     * @throws org.apache.axis2.om.OMException
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException {
        return new OMChildrenQNameIterator((OMNodeImpl) getFirstChild(),
                elementQName);
    }

    /**
     * Method getFirstChild
     *
     * @return
     */
    public OMNode getFirstChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return firstChild;
    }

	/**
     * Method getFirstChildWithName
     *
     * @param elementQName
     * @return
     * @throws OMException
     */
    public OMElement getFirstChildWithName(QName elementQName) throws OMException {
        OMChildrenQNameIterator omChildrenQNameIterator =
                new OMChildrenQNameIterator((OMNodeImpl) getFirstChild(),
                        elementQName);
        OMNode omNode = null;
        if (omChildrenQNameIterator.hasNext()) {
            omNode = (OMNode) omChildrenQNameIterator.next();
        }

        return ((omNode != null) && (OMNode.ELEMENT_NODE == omNode.getType())) ? (OMElement) omNode : null;

    }

    /**
     * Method setFirstChild
     *
     * @param firstChild
     */
    public void setFirstChild(OMNode firstChild) {
        this.firstChild = firstChild;
    }
}
