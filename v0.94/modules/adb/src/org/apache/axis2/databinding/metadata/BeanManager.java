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

import org.apache.axis2.databinding.DeserializerFactory;
import org.apache.axis2.databinding.Serializer;
import org.apache.axis2.databinding.beans.BeanPropertyDescriptor;
import org.apache.axis2.databinding.typemapping.TypeMappingRegistry;

import javax.xml.namespace.QName;
import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * BeanManager
 */
public class BeanManager {
    static Map class2TypeMap = new HashMap();

    public static TypeDesc getTypeDesc(Class beanClass) {
        // If we're already done with this class, just return the cached one
        TypeDesc desc = (TypeDesc)class2TypeMap.get(beanClass);
        if (desc != null)
            return desc;

        // OK, nothing cached.  See if the class has supplied some data itself
        desc = TypeDesc.getTypeDescForClass(beanClass);
        if (desc == null) {
            desc = new TypeDesc();
        }

        class2TypeMap.put(beanClass, desc);

        try {
            fillInTypeDesc(desc, beanClass);
        } catch (Exception e) {
            desc = null;
            class2TypeMap.remove(beanClass);
        }

        return desc;
    }

    private static void fillInTypeDesc(TypeDesc desc, Class beanClass)
            throws Exception {
        TypeMappingRegistry tmr = new TypeMappingRegistry();

        desc.setJavaClass(beanClass);

        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor [] propDescs = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propDescs.length; i++) {
            PropertyDescriptor propDesc = propDescs[i];
            String name = propDesc.getName();

            if (name.equals("class"))
                continue;

            BeanPropertyDescriptor beanDesc = new BeanPropertyDescriptor();
            beanDesc.setReadMethod(propDesc.getReadMethod());
            beanDesc.setWriteMethod(propDesc.getWriteMethod());

            ElementDesc elDesc = desc.getElementDesc(name);
            boolean addDesc = true;  // Should we add this (new) element?
            if (elDesc == null) {
                elDesc = new ElementDesc();
                elDesc.setFieldName(name);
            } else {
                addDesc = false; // Already present, so don't add it again
            }
            Class type;
            boolean isCollection = false;
            if (propDesc instanceof IndexedPropertyDescriptor) {
                IndexedPropertyDescriptor iProp =
                        (IndexedPropertyDescriptor)propDesc;
                beanDesc.setIndexedReadMethod(iProp.getIndexedReadMethod());
                beanDesc.setIndexedWriteMethod(iProp.getIndexedWriteMethod());
                type = iProp.getIndexedPropertyType();
                elDesc.setIndexedAccessor(beanDesc);
                isCollection = true;
            } else {
                type = propDesc.getPropertyType();
                // TODO : check if this is a supported collection type
            }

            if (isCollection && elDesc.getMaxOccurs() == 0)
                elDesc.setMaxOccurs(-1);

            elDesc.setAccessor(beanDesc);

            if (elDesc.getQName() == null) {
                elDesc.setQName(new QName(name));
            }

            if (elDesc.getDeserializerFactory() == null) {
                DeserializerFactory dser = tmr.getDeserializerFactory(type);
                elDesc.setDeserializerFactory(dser);
            }

            if (elDesc.getRawSerializer() == null) {
                Serializer ser = tmr.getSerializer(type);
                elDesc.setSerializer(ser);
            }

            if (addDesc)
                desc.addField(elDesc);
        }
    }
}
