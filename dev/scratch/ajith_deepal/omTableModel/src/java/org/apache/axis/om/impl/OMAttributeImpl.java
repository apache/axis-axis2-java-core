package org.apache.axis.om.impl;

import org.apache.axis.om.*;
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
 * Time: 3:54:19 PM
 */
public class OMAttributeImpl extends OMNodeImpl implements OMAttribute{

    public OMAttributeImpl() {
    }

    /**
     * Special constructor to be used by the model implementation
     * @param model
     * @param key
     * @param values
     */
    public OMAttributeImpl(OMModel model,int key,String[] values) {
        init(model,key,values);
    }

    public void setValue(String value) {
    }

    /**
     * @see org.apache.axis.om.OMAttribute#getValue
     * @return
     */
    public String getValue() {
        return values[OMConstants.VALUE_INDEX];
    }

    public void update() {
        this.values =(String[]) model.update(key,OMConstants.ELEMENT);
    }

    public String getLocalName() {
        return values[OMConstants.LOCAL_NAME_INDEX];
    }

    public void setLocalName(String localName) {
        //todo
    }

    public OMNamespace getNamespace() throws OMException {
        //get the namespace key
        int nskey = Integer.parseInt(values[OMConstants.NAMESPACE_INDEX]);
        if (nskey!=OMConstants.DEFAULT_INT_VALUE){
            return model.getNamespace(nskey);
        }else{
            return null;
        }
    }

    /**
     *
     * @see
     */
    public void setNamespace(OMNamespace namespace) {
    }

    /**
     * Get the parent
     * @return
     */
    public int getParentKey(){
        return Integer.parseInt(values[OMConstants.PARENT_INDEX]);
    }

    public int getNextSiblingKey() {
        return Integer.parseInt(values[OMConstants.NEXT_SIBLING_INDEX]);
    }

    public int getNextSiblingType() {
        return OMNode.ATTRIBUTE_NODE;
    }

    public int getType() {
        return OMNode.ATTRIBUTE_NODE;
    }
}
