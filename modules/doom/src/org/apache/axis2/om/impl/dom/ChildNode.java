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
package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMNodeEx;
import org.w3c.dom.Node;

public abstract class ChildNode extends NodeImpl {

    protected ChildNode previousSibling;

    protected ChildNode nextSibling;

    protected ParentNode parentNode;


    /**
     * @param ownerNode
     */
    protected ChildNode(DocumentImpl ownerDocument) {
        super(ownerDocument);
    }

    protected ChildNode() {

    }

    public OMNode getNextOMSibling() throws OMException {
        while ((nextSibling == null) && !this.parentNode.done) {
            this.parentNode.buildNext();
        }
        return nextSibling;
    }

    public Node getNextSibling() {
        return (Node) this.getNextOMSibling();
    }

    public OMNode getPreviousOMSibling() {
        return this.previousSibling;
    }

    public Node getPreviousSibling() {
        return this.previousSibling;
    }

    ///
    ///OMNode methods
    ///
    public void setNextOMSibling(OMNode node) {
        if (node instanceof ChildNode)
            this.nextSibling = (ChildNode) node;
        else
            throw new OMException("The node is not a " + ChildNode.class);
    }

    public void setPreviousOMSibling(OMNode node) {
        if (node instanceof ChildNode)
            this.previousSibling = (ChildNode) node;
        else
            throw new OMException("The node is not a " + ChildNode.class);
    }

    public OMContainer getParent() throws OMException {
        return (OMContainer) this.parentNode;
    }

    public Node getParentNode() {
        return this.parentNode;
    }

    public void setParent(OMContainer element) {
        if (element instanceof ParentNode)
            this.parentNode = (ParentNode) element;
        else
            throw new OMException("The given parent is not of the type " + ParentNode.class);

    }

    public OMNode detach() throws OMException {
        if (this.parentNode == null) {
            throw new OMException("Parent level elements cannot be ditached");
        } else {
            if (previousSibling == null) { // This is the first child
                if (nextSibling != null) {
                    this.parentNode.setFirstChild(nextSibling);
                } else {
                    this.parentNode.firstChild = null;
                    this.parentNode.lastChild = null;
                }
            } else if(nextSibling != null){
                ((OMNodeEx) this.getPreviousOMSibling()).setNextOMSibling(nextSibling);
            }
            if (this.nextSibling != null) {
                this.nextSibling.setPreviousOMSibling(this.previousSibling);
            }
            this.parentNode = null;
        }
        return this;
    }

    public void discard() throws OMException {
        throw new UnsupportedOperationException("Cannot discard this node");
    }

    /**
     * Inserts the given sibling next to this item.
     */
    public void insertSiblingAfter(OMNode sibling) throws OMException {

        if (this.parentNode != null) {
            ((OMNodeEx) sibling).setParent(this.parentNode);
        }

        if (sibling instanceof ChildNode) {
            ChildNode domSibling = (ChildNode) sibling;
            domSibling.previousSibling = this;
            if (this.nextSibling != null) {
                this.nextSibling.previousSibling = domSibling;
            }
            domSibling.nextSibling = this.nextSibling;
            this.nextSibling = domSibling;

        } else {
            throw new OMException("The given child is not of type " + ChildNode.class);
        }
    }

    /**
     * Inserts the given sibling before this item.
     */
    public void insertSiblingBefore(OMNode sibling) throws OMException {
//		((OMNodeEx)sibling).setParent(this.parentNode);
        if (sibling instanceof ChildNode) {
//			ChildNode domSibling = (ChildNode)sibling;
//			domSibling.nextSibling = this;
//			if(this.previousSibling != null) {
//				this.previousSibling.nextSibling = domSibling;
//			}
//			domSibling.previousSibling = this.previousSibling;
//			this.previousSibling = domSibling;
            ChildNode siblingImpl = (ChildNode) sibling;
            siblingImpl.nextSibling = this;
            if (previousSibling == null) {
                this.parentNode.setFirstChild(siblingImpl);
                siblingImpl.previousSibling = null;
            } else {
                siblingImpl.setParent(this.parentNode);
                previousSibling.setNextOMSibling(siblingImpl);
                siblingImpl.setPreviousOMSibling(previousSibling);
            }
            previousSibling = siblingImpl;

        } else {
            throw new OMException("The given child is not of type " + ChildNode.class);
        }

    }


    public Node cloneNode(boolean deep) {

        ChildNode newnode = (ChildNode) super.cloneNode(deep);

        // Need to break the association w/ original kids
        newnode.previousSibling = null;
        newnode.nextSibling = null;
        newnode.isFirstChild(false);

        return newnode;
    }

}
