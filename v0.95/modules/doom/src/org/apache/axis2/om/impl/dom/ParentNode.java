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

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.om.impl.OMContainerEx;
import org.apache.ws.commons.om.impl.OMNodeEx;
import org.apache.ws.commons.om.impl.traverse.OMChildrenIterator;
import org.apache.ws.commons.om.impl.traverse.OMChildrenQNameIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.Iterator;

public abstract class ParentNode extends ChildNode implements OMContainerEx {

    protected ChildNode firstChild;

    protected ChildNode lastChild;

    /**
     * @param ownerDocument
     */
    protected ParentNode(DocumentImpl ownerDocument, OMFactory factory) {
        super(ownerDocument, factory);
    }

    protected ParentNode(OMFactory factory) {
        super(factory);
    }

    // /
    // /OMContainer methods
    // /

    public void addChild(OMNode omNode) {
        this.appendChild((Node) omNode);
    }

    public void buildNext() {
        if (!this.done)
            builder.next();
    }

    public Iterator getChildren() {
        return new OMChildrenIterator(this.firstChild);
    }

    /**
     * Returns an iterator of child nodes having a given qname.
     * 
     * @see org.apache.ws.commons.om.OMContainer#getChildrenWithName
     * (javax.xml.namespace.QName)
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException {
        return new OMChildrenQNameIterator(getFirstOMChild(), elementQName);
    }

    /**
     * Returns the first OMElement child node.
     * 
     * @see org.apache.ws.commons.om.OMContainer#getFirstChildWithName
     * (javax.xml.namespace.QName)
     */
    public OMElement getFirstChildWithName(QName elementQName)
            throws OMException {
        Iterator children = new OMChildrenQNameIterator(getFirstOMChild(),
                elementQName);
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();

            // Return the first OMElement node that is found
            if (node instanceof OMElement) {
                return (OMElement) node;
            }
        }
        return null;
    }

    public OMNode getFirstOMChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return firstChild;
    }

    public void setFirstChild(OMNode omNode) {
        if (firstChild != null) {
            ((OMNodeEx) omNode).setParent(this);
        }
        this.firstChild = (ChildNode) omNode;
    }

    // /
    // /DOM Node methods
    // /

    public NodeList getChildNodes() {
        if (!this.done) {
            this.build();
        }
        return new NodeListImpl(this, null, null);
    }

    public Node getFirstChild() {
        return (Node) this.getFirstOMChild();
    }

    public Node getLastChild() {
        if (!this.done) {
            this.build();
        }
        return this.lastChild;
    }

    public boolean hasChildNodes() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return this.firstChild != null;
    }

    /**
     * Inserts newChild before the refChild. If the refChild is null then the
     * newChild is made the last child.
     */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {

        ChildNode newDomChild = (ChildNode) newChild;
        ChildNode refDomChild = (ChildNode) refChild;

        if (this == newChild || !isAncestor(newChild)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "HIERARCHY_REQUEST_ERR", null));
        }

        if (!(this instanceof Document)
                && !(this.ownerNode == newDomChild.getOwnerDocument())) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "WRONG_DOCUMENT_ERR", null));
        }

        if (this.isReadonly()) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NO_MODIFICATION_ALLOWED_ERR", null));
        }

        if (this instanceof Document) {
            if (((DocumentImpl) this).documentElement != null
                    && !(newDomChild instanceof CommentImpl)) {
                // Throw exception since there cannot be two document elements
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "HIERARCHY_REQUEST_ERR", null));
            } else if (newDomChild instanceof ElementImpl) {
                if (newDomChild.parentNode == null) {
                    newDomChild.parentNode = this;
                }
                // set the document element
                ((DocumentImpl) this).documentElement = (ElementImpl) newDomChild;
            }
        }

        if (refChild == null) { // Append the child to the end of the list
            // if there are no children
            if (this.lastChild == null && firstChild == null) {
                this.lastChild = newDomChild;
                this.firstChild = newDomChild;
                this.firstChild.isFirstChild(true);
                newDomChild.setParent(this);
            } else {
                this.lastChild.nextSibling = newDomChild;
                newDomChild.previousSibling = this.lastChild;

                this.lastChild = newDomChild;
            }
            if (newDomChild.parentNode == null) {
                newDomChild.parentNode = this;
            }
            return newChild;
        } else {
            Iterator children = this.getChildren();
            boolean found = false;
            while (children.hasNext()) {
                ChildNode tempNode = (ChildNode) children.next();

                if (tempNode.equals(refChild)) {
                    // RefChild found
                    if (this.firstChild == tempNode) { // If the refChild is the
                                                    // first child

                        if (newChild instanceof DocumentFragmentimpl) {
                            // The new child is a DocumentFragment
                            DocumentFragmentimpl docFrag = 
                                                (DocumentFragmentimpl) newChild;
                            this.firstChild = docFrag.firstChild;
                            docFrag.lastChild.nextSibling = refDomChild;
                            refDomChild.previousSibling = 
                                                docFrag.lastChild.nextSibling;

                        } else {

                            // Make the newNode the first Child
                            this.firstChild = newDomChild;

                            newDomChild.nextSibling = refDomChild;
                            refDomChild.previousSibling = newDomChild;

                            this.firstChild.isFirstChild(true);
                            refDomChild.isFirstChild(false);
                            newDomChild.previousSibling = null; // Just to be
                                                                // sure :-)

                        }
                    } else { // If the refChild is not the fist child
                        ChildNode previousNode = refDomChild.previousSibling;

                        if (newChild instanceof DocumentFragmentimpl) {
                            // the newChild is a document fragment
                            DocumentFragmentimpl docFrag = 
                                                (DocumentFragmentimpl) newChild;

                            previousNode.nextSibling = docFrag.firstChild;
                            docFrag.firstChild.previousSibling = previousNode;

                            docFrag.lastChild.nextSibling = refDomChild;
                            refDomChild.previousSibling = docFrag.lastChild;
                        } else {

                            previousNode.nextSibling = newDomChild;
                            newDomChild.previousSibling = previousNode;

                            newDomChild.nextSibling = refDomChild;
                            refDomChild.previousSibling = newDomChild;
                        }

                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "NOT_FOUND_ERR", null));
            }

            if (newDomChild.parentNode == null) {
                newDomChild.parentNode = this;
            }

            return newChild;
        }
    }

    /**
     * Replaces the oldChild with the newChild.
     */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        ChildNode newDomChild = (ChildNode) newChild;
        ChildNode oldDomChild = (ChildNode) oldChild;

        if (this == newChild || !isAncestor(newChild)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "HIERARCHY_REQUEST_ERR", null));
        }

        if (!this.ownerNode.equals(newDomChild.ownerNode)) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "WRONG_DOCUMENT_ERR", null));
        }

        if (this.isReadonly()) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NO_MODIFICATION_ALLOWED_ERR", null));
        }

        Iterator children = this.getChildren();
        boolean found = false;
        while (children.hasNext()) {
            ChildNode tempNode = (ChildNode) children.next();
            if (tempNode.equals(oldChild)) {
                if (newChild instanceof DocumentFragmentimpl) {
                    DocumentFragmentimpl docFrag = 
                                            (DocumentFragmentimpl) newDomChild;
                    ChildNode child = (ChildNode) docFrag.getFirstChild();
                    child.parentNode = this;
                    this.replaceChild(child, oldChild);
                } else {
                    if (this.firstChild == oldDomChild) {
                        
                        newDomChild.parentNode = this;
                        
                        if(this.firstChild.nextSibling != null) {
                            this.firstChild.nextSibling.previousSibling = newDomChild;
                            newDomChild.nextSibling = this.firstChild.nextSibling;
                        }
                        
                        //Cleanup the current first child
                        this.firstChild.parentNode = null;
                        this.firstChild.nextSibling = null;
                        
                        //Set the new first child
                        this.firstChild = newDomChild;
                        
                    } else {
                        newDomChild.nextSibling = oldDomChild.nextSibling;
                        newDomChild.previousSibling = oldDomChild.previousSibling;

                        oldDomChild.previousSibling.nextSibling = newDomChild;

                        // If the old child is not the last
                        if (oldDomChild.nextSibling != null) {
                            oldDomChild.nextSibling.previousSibling = newDomChild;
                        } else {
                            this.lastChild = newDomChild;
                        }

                        if (newDomChild.parentNode == null) {
                            newDomChild.parentNode = this;
                        }
                    }
                }
                found = true;

                // remove the old child's references to this tree
                oldDomChild.nextSibling = null;
                oldDomChild.previousSibling = null;
                oldDomChild.parentNode = null;
            }
        }

        if (!found)
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR",
                            null));

        return oldChild;
    }

    /**
     * Removes the given child from the DOM Tree.
     */
    public Node removeChild(Node oldChild) throws DOMException {
        // Check if this node is readonly
        if (this.isReadonly()) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NO_MODIFICATION_ALLOWED_ERR", null));
        }

        // Check if the Child is there
        Iterator children = this.getChildren();
        boolean childFound = false;
        while (children.hasNext()) {
            ChildNode tempNode = (ChildNode) children.next();
            if (tempNode.equals(oldChild)) {

                if (this.firstChild == tempNode) {
                    // If this is the first child
                    this.firstChild = null;
                    this.lastChild = null;
                    tempNode.parentNode = null;
                } else if (this.lastChild == tempNode) {
                    // not the first child, but the last child
                    ChildNode prevSib = tempNode.previousSibling;
                    prevSib.nextSibling = null;
                    tempNode.parentNode = null;
                    tempNode.previousSibling = null;
                } else {

                    ChildNode oldDomChild = (ChildNode) oldChild;
                    ChildNode privChild = oldDomChild.previousSibling;

                    privChild.nextSibling = oldDomChild.nextSibling;
                    oldDomChild.nextSibling.previousSibling = privChild;

                    // Remove old child's references to this tree
                    oldDomChild.nextSibling = null;
                    oldDomChild.previousSibling = null;
                }
                // Child found
                childFound = true;
            }
        }

        if (!childFound)
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR",
                            null));

        return oldChild;
    }

    private boolean isAncestor(Node newNode) {

        // TODO isAncestor
        return true;
    }

    public Node cloneNode(boolean deep) {

        ParentNode newnode = (ParentNode) super.cloneNode(deep);

        // set owner document
        newnode.ownerNode = ownerNode;

        // Need to break the association w/ original kids
        newnode.firstChild = null;
        newnode.lastChild = null;

        // Then, if deep, clone the kids too.
        if (deep) {
            for (ChildNode child = firstChild; child != null; 
                    child = child.nextSibling) {
                newnode.appendChild(child.cloneNode(true));
            }
        }

        return newnode;

    }
}
