package org.apache.axis.om.impl;

import org.apache.axis.om.OMModel;
import org.apache.axis.om.OMModelImpl;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMDocument;
import org.apache.axis.om.util.OMConstants;

/**
 *Copyright 2001-2004 The Apache Software Foundation.
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
 * Time: 10:36:05 PM
 * 
 */
public class OMDocumentImpl extends OMNodeImpl implements OMDocument{
    public OMDocumentImpl() {
    }

    public OMDocumentImpl(OMModel model,int key,String[] values) {
        init(model,key,values);
    }


    public OMElement getDocumentElement() {
        //search through the model to find an element with the parent (DEFAULT) whic means
        //that the particular element has no child
        while(((OMModelImpl)model).getRootElement()==null){
            model.proceed();
        }
        return ((OMModelImpl)model).getRootElement();


    }

    public int getType() {
        return 0;
    }

    public void update() {
        //do nothing
    }

    public boolean isComplete() {
        return super.isComplete();
    }

    /**
     *
     * @return
     */
    public int getNextSiblingKey() {
        return 0;
    }

    /**
     *
     * @return
     */
    public int getNextSiblingType() {
        return 0;
    }
}
