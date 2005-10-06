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

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class NodeListImpl implements NodeList  {
	
    protected NodeImpl rootNode; 
    protected String tagName;  
    protected Vector nodes;
    
    protected String nsName;
    protected boolean enableNS = false;
	
	
    /** Constructor. */
    public NodeListImpl(NodeImpl rootNode, String tagName) {
        this.rootNode = rootNode;
        this.tagName  = tagName;
        nodes = new Vector();
    }  

    /** Constructor for Namespace support. */
    public NodeListImpl(NodeImpl rootNode,
                            String nsName, String tagName) {
        this(rootNode, tagName);
        this.nsName = (nsName != null && !nsName.equals("")) ? nsName : null;
        enableNS = true;
    }

	/* (non-Javadoc)
	 * @see org.w3c.dom.NodeList#getLength()
	 */
	public int getLength() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NodeList#item(int)
	 */
	public Node item(int arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
}
