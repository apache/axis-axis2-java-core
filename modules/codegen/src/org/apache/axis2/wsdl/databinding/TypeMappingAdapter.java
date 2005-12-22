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
 */

package org.apache.axis2.wsdl.databinding;

import javax.xml.namespace.QName;
import java.util.HashMap;

public abstract class TypeMappingAdapter implements TypeMapper {

    //todo get this from a constant
    protected static final String XSD_SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";

    //hashmap that contains the type mapping names
    protected HashMap qName2NameMap = new HashMap();

    //hashmap that contains the type mapping objects
    protected HashMap qName2ObjectMap = new HashMap();

    //counter variable to generate unique parameter ID's
    protected int counter = 0;


    //Upper limit for the paramete count
    protected static final int UPPER_PARAM_LIMIT = 1000;
    private static final String PARAMETER_NAME_SUFFIX = "param";


    /**
     * Behavior of this method is such that when the type mapping is not found
     * it returns the  default type mapping from the constant
     *
     * @see TypeMapper#getTypeMappingName(javax.xml.namespace.QName)
     */
    public String getTypeMappingName(QName qname) {

        if ((qname != null)) {
            Object o = qName2NameMap.get(qname);
            if (o != null) {
                return (String) o;
            } else {
                return DEFAULT_CLASS_NAME;
            }
        }

        return null;
    }

    /**
     * @see TypeMapper#getParameterName(javax.xml.namespace.QName)
     */
    public String getParameterName(QName qname) {
        if (counter == UPPER_PARAM_LIMIT) {
            counter = 0;
        }
        return PARAMETER_NAME_SUFFIX + counter++;
    }

    /**
     * @see TypeMapper#addTypeMappingName(javax.xml.namespace.QName,String)
     */
    public void addTypeMappingName(QName qname, String value) {
        qName2NameMap.put(qname, value);
    }

    /**
     * @param qname
     * @return object represneting a specific form of the XSD compilation
     * @see TypeMapper#getTypeMappingObject(javax.xml.namespace.QName)
     */
    public Object getTypeMappingObject(QName qname) {
        return qName2ObjectMap.get(qname);
    }

    /**
     * @param qname
     * @param value
     * @see TypeMapper#addTypeMappingObject(javax.xml.namespace.QName, Object)
     */
    public void addTypeMappingObject(QName qname, Object value) {
        qName2ObjectMap.put(qname, value);
    }

    /**
     * @return
     */
    public HashMap getAllTypeMappings() {
        return qName2NameMap;
    }
}
