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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNode;

import javax.xml.namespace.QName;

/**
 * Class OMChildrenQNameIterator
 */
public class OMChildrenQNameIterator extends OMChildrenIterator {
    /**
     * Field givenQName
     */
    private QName givenQName;

    /**
     * Field needToMoveForward
     */
    private boolean needToMoveForward = true;

    /**
     * Field isMatchingNodeFound
     */
    private boolean isMatchingNodeFound = false;

    /**
     * Constructor OMChildrenQNameIterator.
     *
     * @param currentChild
     * @param givenQName
     */
    public OMChildrenQNameIterator(OMNode currentChild, QName givenQName) {
        super(currentChild);
        this.givenQName = givenQName;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return Returns <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        while (needToMoveForward) {
            if (currentChild != null) {

                // check the current node for the criteria
                if ((currentChild instanceof OMElement)
                        && (isQNamesMatch(
                                ((OMElement) currentChild).getQName(),
                                this.givenQName))) {
                    isMatchingNodeFound = true;
                    needToMoveForward = false;
                } else {

                    // get the next named node
                    currentChild = currentChild.getNextOMSibling();
                    isMatchingNodeFound = needToMoveForward = !(currentChild
                            == null);
                }
            } else {
                needToMoveForward = false;
            }
        }
        return isMatchingNodeFound;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return Returns the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    public Object next() {

        // reset the flags
        needToMoveForward = true;
        isMatchingNodeFound = false;
        nextCalled = true;
        removeCalled = false;
        lastChild = currentChild;
        currentChild = currentChild.getNextOMSibling();
        return lastChild;
    }

    /**
     * Cannot use the overridden equals method of QName, as one might want to get
     * some element just by giving the localname, even though a matching element 
     * has a namespace uri as well.
     * This is not supported in the equals method of the QName.
     *
     * @param elementQName
     * @param qNameToBeMatched
     * @return Returns boolean.
     */
    private boolean isQNamesMatch(QName elementQName, QName qNameToBeMatched) {

        // if no QName was given, that means user is asking for all
        if (qNameToBeMatched == null) {
            return true;
        }

        // if the given localname is null, whatever value this.qname has, its a match. But can one give a QName without a localName ??
        boolean localNameMatch =
                (qNameToBeMatched.getLocalPart() == null)
                || (qNameToBeMatched.getLocalPart() == "")
                ||
                ((elementQName != null)
                &&
                elementQName.getLocalPart().equalsIgnoreCase(
                        qNameToBeMatched.getLocalPart()));
        boolean namespaceURIMatch =
                (qNameToBeMatched.getNamespaceURI() == null)
                || (qNameToBeMatched.getNamespaceURI() == "")
                ||
                ((elementQName != null)
                &&
                elementQName.getNamespaceURI().equalsIgnoreCase(
                        qNameToBeMatched.getNamespaceURI()));
        return localNameMatch && namespaceURIMatch;
    }
}
