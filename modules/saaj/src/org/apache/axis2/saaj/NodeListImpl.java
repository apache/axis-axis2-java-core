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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ashutosh Shahi
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeListImpl implements NodeList {

    List mNodes;

    public static final NodeList EMPTY_NODELIST = new NodeListImpl(
            Collections.EMPTY_LIST);

    /**
     * Constructor and Setter is intensionally made package access only.
     */
    NodeListImpl() {
        mNodes = new ArrayList();
    }

    NodeListImpl(List nodes) {
        this();
        mNodes.addAll(nodes);
    }

    void addNode(org.w3c.dom.Node node) {
        mNodes.add(node);
    }

    void addNodeList(org.w3c.dom.NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            mNodes.add(nodes.item(i));
        }
    }

    /**
     * Interface Implemented
     *
     * @param index
     * @return
     */
    public Node item(int index) {
        if (mNodes != null && mNodes.size() > index) {
            return (Node) mNodes.get(index);
        } else {
            return null;
        }
    }

    public int getLength() {
        return mNodes.size();
    }

}
