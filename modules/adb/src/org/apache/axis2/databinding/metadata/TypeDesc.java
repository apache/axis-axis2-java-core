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

package org.apache.axis2.databinding.metadata;

import org.apache.axis2.databinding.utils.MethodCache;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * TypeDesc
 */
public class TypeDesc {
    public static final Class [] noClasses = new Class [] {};
    public static final Object[] noObjects = new Object[] {};

    /** A map of class -> TypeDesc */
    private static Map classMap = new Hashtable();

    /**
     * Static function for centralizing access to type metadata for a
     * given class.
     *
     * This checks for a static getTypeDesc() method on the
     * class or _Helper class.
     * Eventually we may extend this to provide for external
     * metadata config (via files sitting in the classpath, etc).
     *
     */
    public static TypeDesc getTypeDescForClass(Class cls)
    {
        // First see if we have one explicitly registered
        // or cached from previous lookup
        TypeDesc result = (TypeDesc)classMap.get(cls);

        if (result == null) {
            try {
                Method getTypeDesc =
                    MethodCache.getInstance().getMethod(cls,
                                                        "getTypeDesc",
                                                        noClasses);
                if (getTypeDesc != null) {
                    result = (TypeDesc)getTypeDesc.invoke(null, noObjects);
                    if (result != null) {
                        classMap.put(cls, result);
                    }
                }
            } catch (Exception e) {
            }
        }

        return result;
    }

    Map elements = new HashMap();
    Map fieldsByName = new HashMap();
    Map attrs = new HashMap();
    Class javaClass;

    public Iterator getAttributeDescs() {
        return attrs.values().iterator();
    }

    public void addField(ElementDesc fieldDesc) {
        elements.put(fieldDesc.getQName(), fieldDesc);
        fieldsByName.put(fieldDesc.getFieldName(), fieldDesc);
    }

    public void addField(AttributeDesc fieldDesc) {
        attrs.put(fieldDesc.getQName(), fieldDesc);
    }

    public ElementDesc getElementDesc(QName qname) {
        return (ElementDesc)elements.get(qname);
    }

    public ElementDesc getElementDesc(String fieldName) {
        return (ElementDesc)fieldsByName.get(fieldName);
    }

    public Iterator getElementDescs() {
        return elements.values().iterator();
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }
}
