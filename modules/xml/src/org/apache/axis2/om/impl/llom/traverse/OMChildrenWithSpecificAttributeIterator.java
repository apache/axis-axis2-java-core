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
package org.apache.axis2.om.impl.llom.traverse;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNode;

import javax.xml.namespace.QName;

/**
 * Class OMChildrenWithSpecificAttributeIterator
 */
public class OMChildrenWithSpecificAttributeIterator
        extends OMChildrenIterator {
    /**
     * Field attributeName
     */
    private QName attributeName;

    /**
     * Field attributeValue
     */
    private String attributeValue;

    /**
     * Field detach
     */
    private boolean detach;

    /**
     * Constructor OMChildrenWithSpecificAttributeIterator
     *
     * @param currentChild
     * @param attributeName
     * @param attributeValue
     * @param detach
     */
    public OMChildrenWithSpecificAttributeIterator(OMNode currentChild,
                                                   QName attributeName,
                                                   String attributeValue,
                                                   boolean detach) {
        super(currentChild);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.detach = detach;
    }

    /**
     * Method hasNext
     *
     * @return
     */
    public boolean hasNext() {

        // First check whether we have a child, using the super class
        if (currentChild == null) {
            return false;
        }
        boolean isMatchingNodeFound = false;
        boolean needToMoveForward = true;

        // now we have a child to check. If its an OMElement and matches the criteria, then we are done
        while (needToMoveForward) {

            // check the current node for the criteria
            if (currentChild instanceof OMElement) {
                OMAttribute attr =
                        ((OMElement) currentChild).getFirstAttribute(
                                attributeName);
                if ((attr != null)
                        && attr.getValue().equalsIgnoreCase(attributeValue)) {
                    isMatchingNodeFound = true;
                    needToMoveForward = false;
                } else {
                    currentChild = currentChild.getNextSibling();
                    needToMoveForward = !(currentChild == null);
                }
            } else {

                // get the next named node
                currentChild = currentChild.getNextSibling();
                needToMoveForward = !(currentChild == null);
            }
        }
        return isMatchingNodeFound;
    }

    /**
     * Method next
     *
     * @return
     */
    public Object next() {
        Object o = super.next();
        if ((o != null) && detach) {
            ((OMElement) o).detach();
        }
        return o;
    }
}
