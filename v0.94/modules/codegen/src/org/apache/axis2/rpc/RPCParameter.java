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

package org.apache.axis2.rpc;

import org.apache.axis2.databinding.DeserializationTarget;
import org.apache.axis2.databinding.Deserializer;
import org.apache.axis2.databinding.SerializationContext;
import org.apache.axis2.databinding.metadata.ElementDesc;
import org.apache.axis2.databinding.utils.Converter;

import javax.xml.namespace.QName;

/**
 * RPCParameter
 */
public class RPCParameter extends ElementDesc {
    class ParamTarget implements DeserializationTarget {
        QName paramName;
        RPCValues values;

        public ParamTarget(QName paramName, RPCValues values) {
            this.paramName = paramName;
            this.values = values;
        }

        public void setValue(Object value) throws Exception {
            values.setValue(paramName, value);
        }
    }

    class IndexedParamTarget extends ParamTarget {
        int index;

        public IndexedParamTarget(QName paramName,
                                  RPCValues values,
                                  int index) {
            super(paramName, values);
            this.index = index;
        }

        public void setValue(Object value) throws Exception {
            values.setIndexedValue(paramName, index, value);
        }
    }

    public static final int MODE_IN = 0;
    public static final int MODE_OUT = 1;
    public static final int MODE_INOUT = 2;

    int mode;
    Class destClass;

    public Object getValue(RPCValues values) {
        Object value = values.getValue(qname);

        if (destClass != null)
            return Converter.convert(value, destClass);

        return value;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public Class getDestClass() {
        return destClass;
    }

    public void setDestClass(Class destClass) {
        this.destClass = destClass;
    }

    public void serialize(SerializationContext context, Object value)
            throws Exception {
        if (ser == null) {
            // no serializer!
            throw new Exception("No serializer in RPCParameter");
        }
        context.serializeElement(qname, value, ser);
    }

    public Deserializer getDeserializer(int i, RPCValues values) {
        if (i > maxOccurs && maxOccurs > -1) {
            // fail
            throw new RuntimeException("Too many elements");
        }

        DeserializationTarget target;
        if (maxOccurs == -1 || maxOccurs > 1) {
            target = new IndexedParamTarget(qname, values, i);
        } else {
            target = new ParamTarget(qname, values);
        }

        Deserializer dser = deserializerFactory.getDeserializer();
        dser.setTarget(target);
        return dser;
    }
}
