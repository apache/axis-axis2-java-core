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
    protected String[][] values;
    protected int key;

    protected String findValueByIdentifier(String identifier){
        String[] keyArray = values[0];
        String[] valueArray = values[1];

        for (int i = 0; i < keyArray.length; i++) {
            if (keyArray[i].equals(identifier))
                return valueArray[i];
        }

        return OMConstants.DEFAULT_STRING_VALUE;
    }




    public void init(OMModel model,int key,String[][] values) {
        this.model = model;
        this.key = key;
        this.values = values;
    }

    public void reset() {
        this.model = null;
        this.key = OMConstants.DEFAULT_INT_VALUE;
        this.values = null;
    }

    public int getNextSiblingKey(){
        return Integer.parseInt(findValueByIdentifier(OMConstants.NEXT_SIBLING_KEY));
    }

    public int getNextSiblingType(){
        return Integer.parseInt(findValueByIdentifier(OMConstants.NEXT_SIBLING_TYPE_KEY));
    }

    public int getType() {
        return Integer.parseInt(findValueByIdentifier(OMConstants.TYPE_KEY));
    }

    public Object getId() {
        return findValueByIdentifier(OMConstants.ID_KEY);
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

    //



}
