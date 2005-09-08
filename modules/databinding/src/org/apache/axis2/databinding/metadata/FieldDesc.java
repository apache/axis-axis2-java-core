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

import org.apache.axis2.databinding.Deserializer;
import org.apache.axis2.databinding.Serializer;
import org.apache.axis2.databinding.DeserializerFactory;

import javax.xml.namespace.QName;

/**
 * FieldDesc
 */
public abstract class FieldDesc implements FieldAccessor {
    protected QName qname;
    protected FieldAccessor accessor;
    protected DeserializerFactory deserializerFactory;
    protected Serializer ser;
    protected String fieldName;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public QName getQName() {
        return qname;
    }

    public void setQName(QName qname) {
        this.qname = qname;
    }

    public FieldAccessor getAccessor() {
        return accessor;
    }

    public void setAccessor(FieldAccessor accessor) {
        this.accessor = accessor;
    }

    public Object getValue(Object targetObject) throws Exception {
        return accessor.getValue(targetObject);
    }

    public void setValue(Object targetObject, Object value) throws Exception {
        accessor.setValue(targetObject, value);
    }

    public Serializer getSerializer() {
        return ser;
    }

    public void setSerializer(Serializer ser) {
        this.ser = ser;
    }

    Serializer getRawSerializer() {
        return ser;
    }

    public Deserializer getDeserializer(int index) {
        if (index > 0)
            throw new RuntimeException("Only one element " + qname + " allowed");
        return deserializerFactory.getDeserializer();
    }

    public void setDeserializerFactory(DeserializerFactory deser) {
        this.deserializerFactory = deser;
    }

    public DeserializerFactory getDeserializerFactory() {
        return deserializerFactory;
    }
}
