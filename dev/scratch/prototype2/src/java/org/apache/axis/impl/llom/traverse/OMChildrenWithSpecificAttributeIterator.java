package org.apache.axis.impl.llom.traverse;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNode;

import javax.xml.namespace.QName;

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
 */
public class OMChildrenWithSpecificAttributeIterator extends OMChildrenIterator {
    private QName attributeName;
    private String attributeValue;
    private boolean detach;

    public OMChildrenWithSpecificAttributeIterator(OMNode currentChild, QName attributeName, String attributeValue, boolean detach) {
        super(currentChild);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.detach = detach;
    }

    public boolean hasNext() {
        // First check whether we have a child, using the super class
        if (currentChild == null)
            return false;
        boolean isMatchingNodeFound = false;
        boolean needToMoveForward = true;

        // now we have a child to check. If its an OMElement and matches the criteria, then we are done
        while (needToMoveForward) {
            // check the current node for the criteria
            if (currentChild instanceof OMElement) {
                OMAttribute attr = ((OMElement) currentChild).getAttributeWithQName(attributeName);
                if (attr != null && attr.getValue().equalsIgnoreCase(attributeValue)) {
                    isMatchingNodeFound = true;
                    needToMoveForward = false;
                }else{
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

    public Object next() {
        Object o = super.next();
        if (o != null && detach) {
            ((OMElement) o).detach();
        }
        return o;
    }

}
