package org.apache.axis.impl.llom.traverse;

import org.apache.axis.impl.llom.OMNamedNodeImpl;
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
 * <p/>
 * @author Eran Chinthaka - Lanka Software Foundation
 * @author Ajith Ranabahu
 * Date: Oct 13, 2004
 * Time: 11:25:29 AM
 */
public class OMChildrenQNameIterator extends OMChildrenIterator{


    private QName givenQName;

    private boolean needToMoveForward = true;
    private boolean isMatchingNodeFound = false;

    public OMChildrenQNameIterator(OMNode currentChild, QName givenQName) {
        super(currentChild);
        this.givenQName = givenQName;
    }



    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        while (needToMoveForward) {
            if (currentChild != null) {
                // check the current node for the criteria
                if ((currentChild instanceof OMNamedNodeImpl) &&
                        (isQNamesMatch(((OMNamedNodeImpl) currentChild).getQName(), this.givenQName))) {
                    isMatchingNodeFound = true;
                    needToMoveForward = false;
                } else {
                    // get the next named node
                    currentChild = currentChild.getNextSibling();
                    isMatchingNodeFound = needToMoveForward = !(currentChild == null);
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
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    public Object next() {
        //reset the flags
        needToMoveForward=true;
        isMatchingNodeFound=false;
        nextCalled=true;
        removeCalled=false;

        lastChild = currentChild;
        currentChild = currentChild.getNextSibling();
        return lastChild;
    }

    /**
     *    Here I can not use the overriden equals method of QName, as one might want to get
     * some element just by giving the localname, even though a matching element has a namespace uri as well.
     * This will not be supported in the equals method of the QName
     * @param elementQName
     * @param qNameToBeMatched
     * @return
     */
    private boolean isQNamesMatch(QName elementQName, QName qNameToBeMatched){

        // if no QName was given, that means one needs all
        if(qNameToBeMatched == null){
            return true;
        }

        // if the given localname is null, whatever value this.qname has, its a match
        boolean localNameMatch = qNameToBeMatched.getLocalPart() == null ||
                qNameToBeMatched.getLocalPart() == "" ||
                (elementQName != null && elementQName.getLocalPart().equalsIgnoreCase(qNameToBeMatched.getLocalPart()));
        boolean namespaceURIMatch = qNameToBeMatched.getNamespaceURI() == null ||
                qNameToBeMatched.getNamespaceURI() == "" ||
                (elementQName != null && elementQName.getNamespaceURI().equalsIgnoreCase(qNameToBeMatched.getNamespaceURI()));

        return localNameMatch && namespaceURIMatch;


    }


}
