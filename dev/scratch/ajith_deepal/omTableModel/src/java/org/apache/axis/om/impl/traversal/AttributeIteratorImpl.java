package org.apache.axis.om.impl.traversal;

import org.apache.axis.om.impl.OMAttributeImpl;
import org.apache.axis.om.impl.OMElementImpl;
import org.apache.axis.om.util.OMConstants;
import org.apache.axis.om.OMModel;
import org.apache.axis.om.OMAttribute;

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
 * Date: Oct 5, 2004
 * Time: 1:53:12 PM
 * 
 */
public class AttributeIteratorImpl implements Iterator {

    private OMAttributeImpl lastAttribute;
    private OMElementImpl parentElement;
    private OMModel model;

    /**
     * Needs both the parent element and the model for this
     * attribute to be constructed
     * @param parentElement
     * @param model
     */
    public AttributeIteratorImpl(OMElementImpl parentElement, OMModel model) {
        this.parentElement = parentElement;
        this.model = model;
    }

    /**
     * @return whether the next attribute is available
     */
    public boolean hasNext() {
        boolean returnVal = true;
        if (lastAttribute==null){
            int firstAttribKey = parentElement.getFirstAttributeKey();
            returnVal =  (firstAttribKey!=OMConstants.DEFAULT_INT_VALUE);
        }else{
            returnVal = (lastAttribute.getNextSiblingKey()!=OMConstants.DEFAULT_INT_VALUE);
        }
        return returnVal;
    }


    /**
     *
     * @return the next object for this iterator
     */
    public Object next() {
        if (lastAttribute==null){
            lastAttribute = (OMAttributeImpl)parentElement.getFirstAttribute();
        }else{
            int attribKey = lastAttribute.getNextSiblingKey();
            if (attribKey!=OMConstants.DEFAULT_INT_VALUE){
                lastAttribute =  (OMAttributeImpl)model.getAttribute(attribKey);
            }
        }
        return lastAttribute;
    }


    /**
     * remove method is still not implemented
     */
    public void remove() {
        //todo
    }
}
