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

import org.apache.axis2.om.impl.OMContainerEx;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Vector;

/**
 * Implementation of org.w3c.dom.NodeList
 */
public class NodeListImpl implements NodeList {

    protected NodeImpl rootNode;

    protected String tagName;

    protected Vector nodes;

    protected String nsName;

    protected boolean enableNS = false;

    /** Constructor. */
    public NodeListImpl(NodeImpl rootNode, String tagName) {
        this.rootNode = rootNode;
        this.tagName = (tagName != null && !tagName.equals("")) ? tagName
                : null;
        nodes = new Vector();
    }

    /** Constructor for Namespace support. */
    public NodeListImpl(NodeImpl rootNode, String namespaceURI, 
                                                    String localName) {
        this(rootNode, localName);
        this.nsName = (namespaceURI != null && !namespaceURI.equals("")) 
                       ? namespaceURI
                       : null;
        if (this.nsName != null) {
            enableNS = true;
        }
    }

    /**
     * Returns the number of nodes.
     * 
     * @see org.w3c.dom.NodeList#getLength()
     */
    public int getLength() {
        Iterator children;
        if (this.tagName == null) {
            children = ((OMContainerEx) rootNode).getChildren();
        } else if (!enableNS) {
            children = ((OMContainerEx) rootNode)
                    .getChildrenWithName(new QName(this.tagName));
        } else {
            if (DOMUtil.getPrefix(this.tagName) != null) {
                children = ((OMContainerEx) rootNode)
                        .getChildrenWithName(new QName(this.nsName, DOMUtil
                                .getLocalName(this.tagName), DOMUtil
                                .getPrefix(this.tagName)));
            } else {
                children = ((OMContainerEx) rootNode)
                        .getChildrenWithName(new QName(this.nsName, DOMUtil
                                .getLocalName(this.tagName)));
            }
        }
        int count = 0;
        while (children.hasNext()) {
            count++;
            children.next();
        }
        return count;
    }

    /**
     * Returns the node at the given index. Returns null if the index is
     * invalid.
     * 
     * @see org.w3c.dom.NodeList#item(int)
     */
    public Node item(int index) {
        Iterator children;

        if (this.tagName == null) {
            children = ((OMContainerEx) rootNode).getChildren();
        } else if (!enableNS) {
            children = ((OMContainerEx) rootNode)
                    .getChildrenWithName(new QName(this.tagName));
        } else {
            if (DOMUtil.getPrefix(this.tagName) != null) {
                children = ((OMContainerEx) rootNode)
                        .getChildrenWithName(new QName(this.nsName, DOMUtil
                                .getLocalName(this.tagName), DOMUtil
                                .getPrefix(this.tagName)));
            } else {
                children = ((OMContainerEx) rootNode)
                        .getChildrenWithName(new QName(this.nsName, DOMUtil
                                .getLocalName(this.tagName)));
            }
        }

        int count = 0;
        while (children.hasNext()) {
            if (count == index) {
                return (Node) children.next();
            } else {
                children.next();
            }
            count++;
        }
        return null;
    }
}
