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
package org.apache.axis2.saaj;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.dom.NodeImpl;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * A representation of a node (element) in a DOM representation of an XML document
 * that provides some tree manipulation methods.
 * This interface provides methods for getting the value of a node, for
 * getting and setting the parent of a node, and for removing a node.
 */
public abstract class NodeImplEx extends NodeImpl implements Node {

    private SOAPElement parentElement;

    /**
     * Removes this <code>Node</code> object from the tree. Once
     * removed, this node can be garbage collected if there are no
     * application references to it.
     */
    public void detachNode() {
        this.detach();
    }

    /**
     * Removes this <code>Node</code> object from the tree. Once
     * removed, this node can be garbage collected if there are no
     * application references to it.
     */
    public SOAPElement getParentElement() {
        return this.parentElement;
    }

    public OMContainer getParent() {
        return (OMContainer) this.parentElement;
    }

    /* public OMNode getOMNode() {
        return omNode;
    }*/

    /**
     * Returns the the value of the immediate child of this <code>Node</code>
     * object if a child exists and its valu e is text.
     *
     * @return a <code>String</code> with the text of the immediate child of
     *         this <code>Node</code> object if (1) there is a child and
     *         (2) the child is a <code>Text</code> object;
     *         <code>null</code> otherwise
     */
    public String getValue() {
        if (this.getNodeType() == Node.TEXT_NODE) {
            return this.getNodeValue();
        } else if (this.getNodeType() == Node.ELEMENT_NODE) {
            return ((NodeImplEx) (((OMElement) this).getFirstOMChild())).getValue();
        }
        return null;
    }

    /**
     * Notifies the implementation that this <code>Node</code>
     * object is no longer being used by the application and that the
     * implementation is free to reuse this object for nodes that may
     * be created later.
     * <p/>
     * Calling the method <code>recycleNode</code> implies that the method
     * <code>detachNode</code> has been called previously.
     */
    public void recycleNode() {
        // No corresponding implementation in OM
        // There is no implementation in Axis 1.2 also
    }

    /**
     * Sets the parent of this <code>Node</code> object to the given
     * <code>SOAPElement</code> object.
     *
     * @param parent the <code>SOAPElement</code> object to be set as
     *               the parent of this <code>Node</code> object
     * @throws SOAPException if there is a problem in setting the
     *                       parent to the given element
     * @see #getParentElement() getParentElement()
     */
    public void setParentElement(SOAPElement parent) throws SOAPException {
        this.parentElement = parent;
    }

    public void setType(int nodeType) throws OMException {
        throw new UnsupportedOperationException("TODO");
    }

    public int getType() {
        return this.getNodeType();
    }
}
