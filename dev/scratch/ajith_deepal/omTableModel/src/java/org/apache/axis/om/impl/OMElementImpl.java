package org.apache.axis.om.impl;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.traversal.NodeIteratorImpl;
import org.apache.axis.om.impl.traversal.AttributeIteratorImpl;
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
 * Time: 3:52:24 PM
 */
public class OMElementImpl extends OMNodeImpl implements OMElement {

    public OMElementImpl() {
    }

    /**
     * Not operational yet
     * @param model
     */
    public OMElementImpl(OMModel model) {
        this.model = model;

        //create an Element in the model
       // this.key = model.addElement();



    }

    public OMElementImpl(OMModel model,int key,String[] values) {
        init(model,key,values);

    }

    public OMNode getFirstChild() {

        int firstChildKey = Integer.parseInt(values[OMConstants.FIRST_CHILD_INDEX]);
        while (firstChildKey==OMConstants.DEFAULT_INT_VALUE && !isComplete()){
            model.proceed();
            //update to syncronize the values
            update();
            firstChildKey = Integer.parseInt(values[OMConstants.FIRST_CHILD_INDEX]);
        }

        if (isComplete() && firstChildKey==OMConstants.DEFAULT_INT_VALUE)
            return null;

        int firstChildType = Integer.parseInt(values[OMConstants.FIRST_CHILD_TYPE_INDEX]);
        if (firstChildType==OMConstants.ELEMENT){
            return model.getElement(firstChildKey);
        }else if (firstChildType==OMConstants.TEXT){
            return model.getText(firstChildKey);
        }
        return null;
    }


    public String getLocalName() {
        return values[OMConstants.LOCAL_NAME_INDEX];
    }

    public Iterator getChildren() {
        return new NodeIteratorImpl(this,this.model);
    }

    public boolean isComplete() {
        return values[OMConstants.ELEMENT_DONE_INDEX].equals("1")?true:false;
    }


    public void update() {
        this.values =(String[]) model.update(key,OMConstants.ELEMENT);
    }

    public OMElement getParent() throws OMException {
        int parentKey = getParentKey();
        if (parentKey!=OMConstants.DEFAULT_INT_VALUE){
            return model.getElement(parentKey);
        }else{
            return null;
        }

    }

    public int getParentKey(){
        return Integer.parseInt(values[OMConstants.PARENT_INDEX]);
    }

    public void addChild(OMNode omNode) {
    }


    public OMNamespace createNamespace(String uri, String prefix) {
        //create a new namespace in the model
        int nameSpaceKey = model.addNamespace(uri,prefix,this.key);
        if (nameSpaceKey!=OMConstants.DEFAULT_INT_VALUE)
            return model.getNamespace(nameSpaceKey);
        else
            return null;
    }


    public OMNamespace resolveNamespace(String uri, String prefix) throws OMException {

        int nameSpaceKey = model.resolveNamespace(Integer.parseInt(values[OMConstants.ID_INDEX]),
                uri,
                prefix);
        if (nameSpaceKey!=OMConstants.DEFAULT_INT_VALUE)
            return model.getNamespace(nameSpaceKey);
        else
            return null;
    }

    /**
     * Note -  the attributes will be updated along with the START_ELEMENT event which creates
     * this element. So no need to proceed through the model.
     * @return
     */
    public int getFirstAttributeKey(){
        return  Integer.parseInt(values[OMConstants.FIRST_ATTRIBUTE_INDEX]);
    }
    /**
     *
     * @return
     */
    public OMAttribute getFirstAttribute() {

        int firstAttribKey = getFirstAttributeKey();
        if (firstAttribKey!=OMConstants.DEFAULT_INT_VALUE){
            return model.getAttribute(firstAttribKey);
        }else{
            return null;
        }
    }

    public Iterator getAttributes() {
        return new AttributeIteratorImpl(this,this.model);
    }

    public void insertAttribute(OMAttribute attr) {
    }

    public void removeAttribute(OMAttribute attr) {
        OMAttributeImpl omAttribute = ((OMAttributeImpl)attr);
        int attribKey = omAttribute.key;
        int parentKey = omAttribute.getParentKey();
        if (parentKey!=this.key){
            throw new OMException("Attributes that are not part of this element cannot be removed");
        }
        if (this.getFirstAttributeKey()==attribKey){
            int nextAttributeKey = omAttribute.getNextSiblingKey();
            model.updateElement(this.key,OMConstants.DEFAULT_STRING_VALUE,nextAttributeKey,OMConstants.UPDATE_FIRST_ATTRIBUTE);
        }
        model.removeAttribute(attribKey);

        //update tbe model
        update();

    }


    public void setLocalName(String localName) {
        model.updateElement(this.key,localName,OMConstants.DEFAULT_INT_VALUE,OMConstants.UPDATE_ELEMENT_LOCALNAME);
        update();
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

    public void setNamespace(OMNamespace namespace) {
    }

    public int getNextSiblingKey() {
        return Integer.parseInt(values[OMConstants.NEXT_SIBLING_INDEX]);
    }

    public int getNextSiblingType() {
        return Integer.parseInt(values[OMConstants.ELEMENT_NEXTSIBLING_TYPE_INDEX]);
    }

    public int getType() {
        return OMNode.ELEMENT_NODE;
    }
}
