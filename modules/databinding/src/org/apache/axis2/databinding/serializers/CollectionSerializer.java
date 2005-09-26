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
import org.apache.axis2.databinding.utils.Converter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.Array;
import java.util.Collection;

/**
 * CollectionSerializer
 */
public class CollectionSerializer extends AbstractSerializer {
    public final QName DEFAULT_ITEM_QNAME = new QName("item");

    boolean isWrapped;
    QName itemQName;
    Serializer serializer;

    public CollectionSerializer(QName itemQName,
                                boolean isWrapped,
                                Serializer serializer) {
        this.isWrapped = isWrapped;
        this.itemQName = itemQName == null ? DEFAULT_ITEM_QNAME : itemQName;
        this.serializer = serializer;
    }

    public void serialize(Object object, SerializationContext context) throws Exception {
        if (isWrapped) {
            // We have a wrapper element, so it's fine to do multiref checking
            // on the actual collection object
            super.serialize(object, context);
            return;
        }

        // No wrapper, so need to do multiref checks on individual elements.
        serializeData(object, context);
    }

    public void serializeData(Object object, SerializationContext context)
            throws Exception {
        if (object instanceof Collection) {
            Converter.convert(object, Object[].class);
        }
        XMLStreamWriter writer = context.getWriter();
        if (isWrapped) {
            writer.writeStartElement(itemQName.getNamespaceURI(),
                                     itemQName.getLocalPart());
        }

        int len = Array.getLength(object);
        for (int i = 0; i < len; i++) {
            Object item = Array.get(object, i);
            if (i == 0) {
                if (isWrapped) {
                    if (!context.checkMultiref(item, serializer)) {
                        serializer.serializeData(item, context);
                    }
                } else {
                    serializer.serializeData(item, context);
                }
            } else {
                context.serializeElement(itemQName, item, serializer);
            }
        }

//        Collection coll = (Collection)object;
//        for (Iterator i = coll.iterator(); i.hasNext();) {
//            Object item = i.next();
//            context.serializeElement(itemQName, item, serializer);
//        }

        if (isWrapped) {
            writer.writeEndElement();
        }
    }
}
