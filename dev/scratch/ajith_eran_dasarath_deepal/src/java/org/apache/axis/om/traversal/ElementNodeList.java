package org.apache.axis.om.traversal;

import org.apache.axis.om.storage.ElementRow;
import org.apache.axis.om.storage.NodeRow;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * Once a NodeList is returned from the Element implementation of AXIOM, we do not populate and give it to the
 * requesting party. Rather we put a reference to the Element, whose children being refered inside the NodeList we return and this one
 * will provide the details required, on the fly.
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Sep 22, 2004
 * Time: 4:03:03 PM
 */
public class ElementNodeList implements NodeList {

    private ElementRow elementRow;

    /**
     * this will hold the elements that have been refered by the ElementNodeList already.
     */
    private List childrenCache;

    public ElementNodeList(ElementRow elementRow) {
        this.elementRow = elementRow;
        childrenCache = new LinkedList();
        childrenCache.add(elementRow.getFirstChild());
    }

    /**
     * Use of this method is highly discouraged, as children are not being populated inside this list, and if one asks
     * the length, we have to access each and every child to provide this info.
     *
     * @return
     */
    public int getLength() {
        return 0;  //TODO implement this
    }

    public Node item(int index) {
        NodeRow nodeRow = null;

        // if the requested children is already available in the cache, get it
        if (index < childrenCache.size()) {
            nodeRow = (NodeRow) childrenCache.get(index);
        } else {
            // else get the last child available from the childrencache and from that proceed
            int cacheSize = childrenCache.size();
            nodeRow = (NodeRow) childrenCache.get(cacheSize-1);

            for (int i = 0; i < index - cacheSize+1; i++) {
                nodeRow = (NodeRow) nodeRow.getNextSibling();
                if (nodeRow == null) {
                    return null;
                } else {
                    childrenCache.add(nodeRow);
                }
            }

        }
        return nodeRow;
    }
}
