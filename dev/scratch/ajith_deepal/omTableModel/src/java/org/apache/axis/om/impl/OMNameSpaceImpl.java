package org.apache.axis.om.impl;

import org.apache.axis.om.OMModel;
import org.apache.axis.om.OMNamespace;
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
 * Time: 9:39:52 PM
 */
public class OMNameSpaceImpl extends OMNodeImpl implements OMNamespace{
    public OMNameSpaceImpl() {
    }

    public OMNameSpaceImpl(OMModel model,int key,String[][] values) {
        init(model,key,values);
    }

    public String getURI() {
        return findValueByIdentifier(OMConstants.NAMESPACE_URI_KEY);
    }

    public String getPrefix() {
        return findValueByIdentifier(OMConstants.NAMESPACE_PREFIX_KEY);
    }

     public void update() {
       this.values =(String[][]) model.update(key,OMConstants.NAMESPACE);
    }

    public boolean equals(OMNamespace ns) {
        return false;
    }

    public boolean equals(String uri, String prefix) {
        return false;
    }

    public boolean isDefaultNs() {
        return false;
    }
}
