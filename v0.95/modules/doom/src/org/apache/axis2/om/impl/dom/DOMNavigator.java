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

import org.apache.ws.commons.om.OMContainer;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNode;

/**
 * This is exactly the same as org.apache.ws.commons.om.impl.om.OMNavigator, only the
 * llom specifics are changed to dom. Refer to the testClass to find out how to
 * use features like isNavigable, isComplete and step.
 */
public class DOMNavigator {
    /**
     * Field node
     */
    protected OMNode node;

    /**
     * Field visited
     */
    private boolean visited;

    /**
     * Field next
     */
    private OMNode next;

    // root is the starting element. Once the navigator comes back to the
    // root, the traversal is terminated

    /**
     * Field root
     */
    private OMNode root;

    /**
     * Field backtracked
     */
    private boolean backtracked;

    // flags that tell the status of the navigator

    /**
     * Field end
     */
    private boolean end = false;

    /**
     * Field start
     */
    private boolean start = true;

    /**
     * Constructor OMNavigator.
     */
    public DOMNavigator() {
    }

    /**
     * Constructor OMNavigator.
     * 
     * @param node
     */
    public DOMNavigator(OMNode node) {
        init(node);
    }

    /**
     * Method init.
     * 
     * @param node
     */
    public void init(OMNode node) {
        next = node;
        root = node;
        backtracked = false;
    }

    /**
     * Gets the next node.
     * 
     * @return Returns OMNode in the sequence of preorder traversal. Note
     *         however that an element node is treated slightly differently.
     *         Once the element is passed it returns the same element in the
     *         next encounter as well.
     */
    public OMNode next() {
        if (next == null) {
            return null;
        }
        node = next;
        visited = backtracked;
        backtracked = false;
        updateNextNode();

        // set the starting and ending flags
        if (root.equals(node)) {
            if (!start) {
                end = true;
            } else {
                start = false;
            }
        }
        return node;
    }

    /**
     * Private method to encapsulate the searching logic
     */
    private void updateNextNode() {
        if ((next instanceof OMElement) && !visited) {
            ElementImpl e = (ElementImpl) next;
            if (e.firstChild != null) {
                next = e.firstChild;
            } else if (e.isComplete()) {
                backtracked = true;
            } else {
                next = null;
            }
        } else {
            OMNode nextSibling = ((ChildNode) next).nextSibling;
            OMContainer parent = next.getParent();
            if (nextSibling != null) {
                next = nextSibling;
            } else if ((parent != null) && parent.isComplete()) {
                next = (NodeImpl) parent;
                backtracked = true;
            } else {
                next = null;
            }
        }
    }

    /**
     * Method visited.
     * 
     * @return Returns boolean.
     */
    public boolean visited() {
        return visited;
    }

    /**
     * This is a very special method. This allows the navigator to step once it
     * has reached the existing OM. At this point the isNavigable method will
     * return false but the isComplete method may return false which means that
     * the navigating the given element is not complete but the navigator cannot
     * proceed.
     */
    public void step() {
        if (!end) {
            next = node;
            updateNextNode();
        }
    }

    /**
     * Returns the navigable status.
     * 
     * @return Returns boolean.
     */
    public boolean isNavigable() {
        if (end) {
            return false;
        } else {
            return !(next == null);
        }
    }

    /**
     * Returns the completed status.
     * 
     * @return Returns boolean.
     */
    public boolean isCompleted() {
        return end;
    }
}
