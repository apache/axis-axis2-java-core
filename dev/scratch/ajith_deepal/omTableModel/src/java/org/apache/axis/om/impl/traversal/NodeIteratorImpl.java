package org.apache.axis.om.impl.traversal;

import org.apache.axis.om.OMModel;
import org.apache.axis.om.impl.OMElementImpl;
import org.apache.axis.om.impl.OMNodeImpl;
import org.apache.axis.om.util.OMConstants;

import java.util.Iterator;

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
 *
 * @author Axis team
 * Date: Sep 28, 2004
 * Time: 11:45:40 PM
 */
public class NodeIteratorImpl implements Iterator{

    private OMElementImpl parentElement;
    private OMModel model;
    private OMNodeImpl lastNode;

    /**
     * Construction needs both the
     * @param parentElement
     * @param model
     */
    public NodeIteratorImpl(OMElementImpl parentElement, OMModel model) {
        this.parentElement = parentElement;
        this.model = model;
    }

    /**
     * Next method of the iterator.
     * @return an OMNode that represents the next element or null if
     * there are none
     */
    public Object next() {

        if (lastNode==null){
            lastNode =(OMNodeImpl)parentElement.getFirstChild();
            return lastNode;
        }else{
            while ( !hasNext() && lastNode.getNextSiblingKey()==OMConstants.DEFAULT_INT_VALUE){
                model.proceed();
                lastNode.update();
            }

            if (lastNode.getNextSiblingKey()!=OMConstants.DEFAULT_INT_VALUE){
                //create the last node by the relevant type
                int nextSiblingKey = lastNode.getNextSiblingKey();
                int nextSiblingType = lastNode.getNextSiblingType();

                if (nextSiblingType==OMConstants.ELEMENT){
                    lastNode = (OMNodeImpl)model.getElement(nextSiblingKey);
                }else if (nextSiblingType==OMConstants.TEXT){
                    lastNode = (OMNodeImpl)model.getText(nextSiblingKey);
                }

                return lastNode;
            }else if (hasNext()){
                return null;
            }
        }

        return null;
    }

    /**
     *
     * @return a boolean values that says whether there are
     * any more children.
     */
    public boolean hasNext() {
        return parentElement.isComplete() || model.isComplete() ;
    }

    public void remove() {
        //do nothing!!
    }
}
