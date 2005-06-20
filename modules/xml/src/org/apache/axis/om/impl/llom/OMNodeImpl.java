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
package org.apache.axis.om.impl.llom;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMXMLParserWrapper;

/**
 * Class OMNodeImpl
 */
public abstract class OMNodeImpl implements OMNode {
    /**
     * Field parent
     */
    protected OMElementImpl parent;

    /**
     * Field nextSibling
     */
    protected OMNodeImpl nextSibling;

    /**
     * Field previousSibling
     */
    protected OMNodeImpl previousSibling;
       /**
     * Field builder
     */
    protected OMXMLParserWrapper builder;
   
    /**
     * Field done
     */
    protected boolean done = false;

    /**
     * Field nodeType
     */
    protected int nodeType;

    /**
     * Constructor OMNodeImpl
     */
    public OMNodeImpl() {
    }

    /**
     * For a node to exist there must be a parent
     *
     * @param parent
     */
    public OMNodeImpl(OMElement parent) {
        if ((parent != null) && (parent.getType() == OMNode.ELEMENT_NODE)) {
            this.parent = (OMElementImpl) parent;
            parent.addChild(this);
        }
    }

    /**
     * This method should return the immediate parent of the node.
     * Parent is always an Element
     *
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public OMElement getParent() throws OMException {
        return parent;
    }

    /**
     * Method setParent
     *
     * @param element
     */
    public void setParent(OMElement element) {

        if( ((OMElement)this.parent) == element){
            return;
        }

        if (element instanceof OMNodeImpl) {//todo fix me
            if(this.parent != null){
                this.detach();
            }
            this.parent = (OMElementImpl) element;
        }
    }

    /**
     * This will give the next sibling. This can be an OMAttribute for OMAttribute or OMText or OMELement for others.
     *
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public OMNode getNextSibling() throws OMException {
        if ((nextSibling == null) && (parent != null) && !parent.isComplete()) {
            parent.buildNext();
        }
        return nextSibling;
    }

    /**
     * Method setNextSibling
     *
     * @param node
     */
    public void setNextSibling(OMNode node) {
        this.nextSibling = (OMNodeImpl) node;
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
     * This will remove this information item and its children, from the model completely
     *
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public OMNode detach() throws OMException {
        if (parent == null) {
            throw new OMException(
                    "Elements that doesn't have a parent can not be detached");
        }
        OMNodeImpl nextSibling = (OMNodeImpl) getNextSibling();
        if (previousSibling == null) {
            parent.setFirstChild(nextSibling);
        } else {
            previousSibling.setNextSibling(nextSibling);
        }
        if (nextSibling != null) {
            nextSibling.setPreviousSibling(previousSibling);
        }

        return this;
    }

    /**
     * This will insert a sibling just after the current information item.
     *
     * @param sibling
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public void insertSiblingAfter(OMNode sibling) throws OMException {
        if (parent == null) {
            throw new OMException();
        }
        sibling.setParent(parent);
        if (sibling instanceof OMNodeImpl) {
            OMNodeImpl siblingImpl = (OMNodeImpl) sibling;
            if (nextSibling == null) {
                getNextSibling();
            }
            siblingImpl.setPreviousSibling(this);
            if (nextSibling != null) {
                nextSibling.setPreviousSibling(sibling);
            }
            sibling.setNextSibling(nextSibling);
            nextSibling = siblingImpl;
        }
    }

    /**
     * This will insert a sibling just before the current information item
     *
     * @param sibling
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public void insertSiblingBefore(OMNode sibling) throws OMException {
        if (parent == null) {
            throw new OMException();
        }
        sibling.setParent(parent);
        if (sibling instanceof OMNodeImpl) {
            OMNodeImpl siblingImpl = (OMNodeImpl) sibling;
            siblingImpl.setPreviousSibling(previousSibling);
            siblingImpl.setNextSibling(this);
            if (previousSibling == null) {
                parent.setFirstChild(siblingImpl);
            } else {
                previousSibling.setNextSibling(siblingImpl);
            }
            previousSibling = siblingImpl;
        }
    }

    /**
     * This is to get the type of node, as this is the super class of all the nodes
     *
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public int getType() throws OMException {
        return nodeType;
    }

    /**
     * Method setType
     *
     * @param nodeType
     * @throws OMException
     */
    public void setType(int nodeType) throws OMException {
        this.nodeType = nodeType;
    }

    /**
     * Method getPreviousSibling
     *
     * @return
     */
    public OMNode getPreviousSibling() {
        return previousSibling;
    }

    /**
     * Method setPreviousSibling
     *
     * @param previousSibling
     */
    public void setPreviousSibling(OMNode previousSibling) {
        this.previousSibling = (OMNodeImpl) previousSibling;
    }



   /**
     * This will completely parse this node and build the object structure in the memory.
     * However a programmatically created node will have done set to true by default and will cause
     * populateyourself not to work properly!
     * @throws OMException
     */
    public void build() throws OMException {
        while(!done){
            builder.next();
        }
    }





}
