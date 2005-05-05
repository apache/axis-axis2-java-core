package org.apache.axis.wsdl.databinding;

import org.apache.axis.om.OMElement;

import javax.xml.namespace.QName;
import java.util.HashMap;

/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/
public abstract class  TypeMappingAdapter implements TypeMapper{

    protected  HashMap map = new HashMap();
    protected int counter=0;
    protected static final int UPPER_PARAM_LIMIT = 1000;

    public Class getTypeMapping(QName qname) {
        if (qname==null) return OMElement.class;

        if (map.containsKey(qname.getLocalPart())){
            return map.get(qname.getLocalPart()).getClass();
        }
        return Object.class;
    }

    public String getParameterName(QName qname) {
        if (counter==UPPER_PARAM_LIMIT){
            counter=0;
        }
        return "param" + counter++;
    }

    public void addTypeMapping(QName qname, Object value) {
        map.put(qname,value);
    }
}
