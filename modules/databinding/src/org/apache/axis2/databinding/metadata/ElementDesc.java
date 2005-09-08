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
import org.apache.axis2.databinding.DeserializationTarget;
import org.apache.axis2.databinding.Serializer;
import org.apache.axis2.databinding.DeserializerFactory;
import org.apache.axis2.databinding.serializers.CollectionSerializer;

import javax.xml.namespace.QName;

/**
 * ElementDesc
 */
public class ElementDesc extends FieldDesc {
    protected IndexedFieldAccessor indexedAccessor;
    protected QName itemQName;

    class FieldTarget implements DeserializationTarget {
        Object targetObject;
        FieldAccessor accessor;

        public FieldTarget(Object targetObject, FieldAccessor accessor) {
            this.targetObject = targetObject;
            this.accessor = accessor;
        }

        public void setValue(Object value) throws Exception {
            accessor.setValue(targetObject, value);
        }
    }

    protected int minOccurs = 0;

    // Might seem strange to default this to zero, but we do it because
    // we want to be able to tell in BeanManager.fillInTypeDesc() if someone
    // set this manually or not.  If it's still zero in there (meaning default
    // values are good) we'll set it to 1 for a normal field and -1 (unbounded)
    // for a collection field.
    protected int maxOccurs = 0;

    public int getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    public int getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public QName getItemQName() {
        return itemQName;
    }

    public void setItemQName(QName itemQName) {
        this.itemQName = itemQName;
    }

    public Deserializer getDeserializer(int index, Object targetObject) {
        if (index > maxOccurs && maxOccurs > -1) {
            throw new RuntimeException("Too many elements (maxOccurs = " +
                                       maxOccurs + ") called " + qname + " !");
        }

        Deserializer dser = deserializerFactory.getDeserializer();
        FieldAccessor accessor;
        if (maxOccurs > 1 || maxOccurs == -1) {
            accessor = new IndexedAccessorWrapper(indexedAccessor, index);
        } else {
            accessor = this.accessor;
        }

        dser.setTarget(new FieldTarget(targetObject, accessor));

        return dser;
    }

    public Serializer getSerializer() {
        if (maxOccurs > 1 || maxOccurs == -1) {
            if (itemQName != null) {
                return new CollectionSerializer(itemQName,
                                                true,
                                                super.getSerializer());
            } else {
                return new CollectionSerializer(qname,
                                                false,
                                                super.getSerializer());
            }
        }
        return super.getSerializer();
    }

    public void setIndexedAccessor(IndexedFieldAccessor indexedAccessor) {
        this.indexedAccessor = indexedAccessor;
    }
}
