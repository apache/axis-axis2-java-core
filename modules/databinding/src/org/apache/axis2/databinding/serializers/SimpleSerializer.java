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

import javax.xml.namespace.QName;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * SimpleSerializer
 */
public class SimpleSerializer extends AbstractSerializer {
    private static SimpleDateFormat zulu =
       new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                         //  0123456789 0 123456789

    static {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void serialize(Object object, SerializationContext context) throws Exception {
        // Simple types never get multiref'd, so just write the data now.
        serializeData(object, context);
    }

    public void serializeData(Object object, SerializationContext context)
            throws Exception {
        String value = object.toString();
        context.getWriter().writeCharacters(value);
        context.getWriter().writeEndElement();
    }

    public String getValueAsString(Object value, SerializationContext context) {
        // We could have separate serializers/deserializers to take
        // care of Float/Double cases, but it makes more sence to
        // put them here with the rest of the java lang primitives.
        if (value instanceof Float ||
            value instanceof Double) {
            double data = 0.0;
            if (value instanceof Float) {
                data = ((Float) value).doubleValue();
            } else {
                data = ((Double) value).doubleValue();
            }
            if (Double.isNaN(data)) {
                return "NaN";
            } else if (data == Double.POSITIVE_INFINITY) {
                return "INF";
            } else if (data == Double.NEGATIVE_INFINITY) {
                return "-INF";
            }
        } else if (value instanceof QName) {
            return context.qName2String((QName)value);
        }

        return value.toString();
    }
}
