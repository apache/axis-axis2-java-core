package org.apache.axis.wsdl.databinding;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.om.OMElement;

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
* Default abstract implementation of the type mapper
*/
public abstract class  TypeMappingAdapter implements TypeMapper{
    protected static final String XSD_SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";
    //hashmap that contains the type mappings
    protected  HashMap map = new HashMap();
    //counter variable to generate unique parameter ID's
    protected int counter=0;
    //Upper limit for the paramete count
    protected static final int UPPER_PARAM_LIMIT = 1000;

    /**
     *
     * @see TypeMapper#getTypeMapping(javax.xml.namespace.QName)
     */
    public Class getTypeMapping(QName qname) {
        if ((qname!=null)){
            Object o = map.get(qname);
            if (o!=null){
               return (Class)o;
            }else{
                return OMElement.class;
            }
        }

        return null;
    }

    /**
     * @see TypeMapper#getParameterName(javax.xml.namespace.QName)
     */
    public String getParameterName(QName qname) {
        if (counter==UPPER_PARAM_LIMIT){
            counter=0;
        }
        return "param" + counter++;
    }

    /**
     * @see TypeMapper#addTypeMapping(javax.xml.namespace.QName, Object)
     */
    public void addTypeMapping(QName qname, Object value) {
        map.put(qname,value);
    }
}
