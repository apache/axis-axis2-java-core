package org.apache.axis.om.impl;

import org.apache.axis.om.OMModel;
import org.apache.axis.om.OMText;
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
 * Time: 3:57:12 PM
 */
public class OMTextImpl extends OMNodeImpl implements OMText{
    public OMTextImpl() {
    }

    public OMTextImpl(OMModel model,int key,String[][] values) {
         init(model,key,values);
    }

    public void setValue(String value) {
        //todo
    }

    public String getValue() {
        return findValueByIdentifier(OMConstants.VALUE_KEY);
    }

     public void update() {
       this.values =(String[][]) model.update(key,OMConstants.TEXT);
    }

    public void setTextType(short type) {
    }

    public short getTextType() {
         return Short.parseShort(findValueByIdentifier(OMConstants.TYPE_KEY));
    }

}
