package org.apache.axis.om.impl;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMModel;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.pool.PooledObject;
import org.apache.axis.om.util.OMConstants;

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
 * Time: 9:22:42 PM
 */
public abstract class OMNodeImpl implements OMNode,PooledObject{

    protected OMModel model ;
    protected String[] values;
    protected int key;
    //The boolean that specifies whether the object can
    //be "constructed"
    protected boolean creatable=true;

    public void init(OMModel model,int key,String[] values) {
        this.model = model;
        this.key = key;
        this.values = values;
         //once the init method is called this object becomes uncreatable
        creatable=false;

    }

    public void reset() {
        this.model = null;
        this.key = OMConstants.DEFAULT_INT_VALUE;
        this.values = null;
        //reset the flag
        creatable = true;
    }


    public abstract int getType() ;


    public Object getId() {
        return values[OMConstants.ID_INDEX];
    }

    public abstract void update();


    public OMElement getParent() throws OMException {
        return null;
    }

    public void setParent(OMElement element) {
        //not allowed yet
    }

    public OMNode getNextSibling() throws OMException {
        return null;
    }

    public void setNextSibling(OMNode node) {
    }

    public String getValue() throws OMException {
        return null;
    }

    public void setValue(String value) {
    }

    public boolean isComplete() {
        return false;
    }

    public void setComplete(boolean state) {
    }

    public void detach() throws OMException {
    }

    public void insertSiblingAfter(OMNode sibling) throws OMException {
    }

    public void insertSiblingBefore(OMNode sibling) throws OMException {
    }

     public int getNextSiblingKey() {
        return OMConstants.DEFAULT_INT_VALUE;
    }

    public int getNextSiblingType() {
        return OMConstants.DEFAULT_INT_VALUE;
    }



}
