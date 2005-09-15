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
package org.apache.axis2.databinding.serializers;

import org.apache.axis2.databinding.SerializationContext;
import org.apache.axis2.databinding.Serializer;
import org.apache.axis2.databinding.metadata.ElementDesc;
import org.apache.axis2.databinding.metadata.TypeDesc;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * BeanSerializer
 */
public class BeanSerializer extends AbstractSerializer {
    TypeDesc typeDesc;

    public BeanSerializer(TypeDesc typeDesc) {
        this.typeDesc = typeDesc;
    }

    public void serializeData(Object object, SerializationContext context)
            throws Exception {
        // Write any attributes that need writing here

        Iterator elements = typeDesc.getElementDescs();
        while (elements.hasNext()) {
            ElementDesc desc = (ElementDesc) elements.next();
            Object fieldValue = desc.getValue(object);
            Serializer ser = desc.getSerializer();
            QName qname = desc.getQName();
            context.serializeElement(qname, fieldValue, ser);
        }

        context.getWriter().writeEndElement();
    }
}
