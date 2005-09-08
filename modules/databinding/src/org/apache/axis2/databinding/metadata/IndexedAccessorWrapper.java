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

/**
 * IndexedAccessorWrapper
 */
public class IndexedAccessorWrapper implements FieldAccessor {
    IndexedFieldAccessor accessor;
    int index;

    public IndexedAccessorWrapper(IndexedFieldAccessor accessor, int index) {
        this.accessor = accessor;
        this.index = index;
    }

    public Object getValue(Object targetObject) throws Exception {
        return accessor.getIndexedValue(targetObject, index);
    }

    public void setValue(Object targetObject, Object value) throws Exception {
        accessor.setIndexedValue(targetObject, value, index);
    }
}
