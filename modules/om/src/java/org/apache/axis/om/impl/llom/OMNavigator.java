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
package org.apache.axis.om.impl.llom;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNode;

/**
 * Refer to the testClass to find out how to use
 * features like isNavigable, isComplete and step
 */
public class OMNavigator {

    protected OMNode node;
    private boolean visited;
    private OMNode next;

    //root is the starting element. Once the navigator comes back to the
    //root, the traversal is terminated
    private OMNode root;
    private boolean backtracked;

    //flags that tell the status of the navigator
    private boolean end = false;
    private boolean start = true;

    public OMNavigator() {
    }

    public OMNavigator(OMNode node) {
        init(node);
    }

    public void init(OMNode node) {
        next = node;
        root = node;
        backtracked = false;
    }

    /**
     * get the next node
     *
     * @return OMnode in the sequence of preorder traversal. Note however that an element node is
     *         treated slightly diffrently. Once the element is passed it returns the same element in the
     *         next encounter as well
     */
    public OMNode next() {
        if (next == null) {
            return null;
        }
        node = next;
        visited = backtracked;
        backtracked = false;

        updateNextNode();
        //set the starting and ending flags
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
        if (next instanceof OMElement && !visited) {

            OMElementImpl e = (OMElementImpl) next;
            if (e.firstChild != null)
                next = e.firstChild;
            else if (e.isComplete())
                backtracked = true;
            else
                next = null;

        } else {
            OMNode nextSibling = ((OMNodeImpl) next).nextSibling;
            OMNode parent = next.getParent();

            if (nextSibling != null)
                next = nextSibling;
            else if (parent != null && parent.isComplete()) {
                next = parent;
                backtracked = true;
            } else
                next = null;
        }
    }


    public boolean visited() {
        return visited;
    }

    /**
     * This is a very special method. This allows the navigator to step
     * once it has reached the existing om. At this point the isNavigable
     * method will return false but the isComplete method may return false
     * which means that the navigating the given element is not complete but
     * the navigator cannot proceed
     */
    public void step() {
        if (!end) {
            next = node;
            updateNextNode();
        }
    }

    /**
     * the navigable status
     *
     * @return
     */
    public boolean isNavigable() {
        if (end)
            return false;
        else
            return !(next == null);
    }

    /**
     * The completed status
     *
     * @return
     */
    public boolean isCompleted() {
        return end;
    }
}
