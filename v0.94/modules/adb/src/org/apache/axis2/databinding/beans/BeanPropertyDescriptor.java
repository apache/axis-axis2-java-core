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

package org.apache.axis2.databinding.beans;

import org.apache.axis2.databinding.metadata.FieldAccessor;
import org.apache.axis2.databinding.metadata.IndexedFieldAccessor;

import java.lang.reflect.Method;

/**
 * BeanPropertyDescriptor
 */
public class BeanPropertyDescriptor implements FieldAccessor, IndexedFieldAccessor {
    Method readMethod;
    Method writeMethod;
    Method indexedReadMethod;
    Method indexedWriteMethod;

    public Method getReadMethod() {
        return readMethod;
    }

    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    public Method getIndexedReadMethod() {
        return indexedReadMethod;
    }

    public void setIndexedReadMethod(Method indexedReadMethod) {
        this.indexedReadMethod = indexedReadMethod;
    }

    public Method getIndexedWriteMethod() {
        return indexedWriteMethod;
    }

    public void setIndexedWriteMethod(Method indexedWriteMethod) {
        this.indexedWriteMethod = indexedWriteMethod;
    }

    public Object getValue(Object targetObject) throws Exception {
        return readMethod.invoke(targetObject, null);
    }

    public void setValue(Object targetObject, Object value) throws Exception {
        writeMethod.invoke(targetObject, new Object [] { value });
    }
    
    public Object getIndexedValue(Object targetObject, int index)
            throws Exception {
        return indexedReadMethod.invoke(targetObject,
                                        new Object [] { new Integer(index) });
    }

    public void setIndexedValue(Object targetObject,
                                Object value,
                                int index) throws Exception {
        indexedWriteMethod.invoke(targetObject,
                                  new Object [] { new Integer(index), value });
    }
}
