/*
 * Copyright  2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/*
 * Created on Sep 25, 2004
 *
 */
package org.apache.axis.om.impl;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNode;

/**
 * @author Dasarath Weeratunge
 */
public class OMNavigator {
    protected OMNode node;
    private boolean visited;
    private OMNode next;
    private boolean backtracked;

    public OMNavigator() {
    }

    public OMNavigator(OMNode node) {
        init(node);
    }

    public void init(OMNode node) {
        next = node;
        backtracked = false;
    }

    public OMNode next() {
        if (next == null)
            return null;
        node = next;
        visited = backtracked;
        backtracked = false;
        if (next instanceof OMElement && !visited) {
            OMElementImpl e = (OMElementImpl) next;
            if (e.getFirstChild() != null)
                next = e.getFirstChild();
            else if (e.isComplete())
                backtracked = true;
            else
                next = null;
            return node;
        }
        OMNodeImpl n = (OMNodeImpl) next;
        if (n.nextSibling != null)
            next = n.nextSibling;
        else if (n.parent != null && n.parent.isComplete()) {
            next = n.parent;
            backtracked = true;
        } else
            next = null;
        return node;
    }

    public boolean visited() {
        return visited;
    }
}
