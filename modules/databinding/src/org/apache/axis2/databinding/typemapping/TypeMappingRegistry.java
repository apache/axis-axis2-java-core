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
package org.apache.axis2.databinding.typemapping;

import org.apache.axis2.databinding.metadata.BeanManager;
import org.apache.axis2.databinding.DeserializerFactory;
import org.apache.axis2.databinding.Serializer;
import org.apache.axis2.databinding.metadata.BeanManager;
import org.apache.axis2.databinding.deserializers.BeanDeserializerFactory;
import org.apache.axis2.databinding.deserializers.SimpleDeserializerFactory;
import org.apache.axis2.databinding.serializers.BeanSerializer;
import org.apache.axis2.databinding.serializers.SimpleSerializer;
import org.apache.axis.xsd.Constants;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.Date;

/**
 * TypeMappingRegistry
 */
public class TypeMappingRegistry {
    public static boolean isPrimitive(Object value) {
        if (value == null) return true;

        Class javaType = (value instanceof Class) ? (Class)value :
                value.getClass();

        if (javaType.isPrimitive()) return true;

        if (javaType == String.class) return true;
        if (Calendar.class.isAssignableFrom(javaType)) return true;
        if (Date.class.isAssignableFrom(javaType)) return true;
//        if (HexBinary.class.isAssignableFrom(javaType)) return true;
        if (Element.class.isAssignableFrom(javaType)) return true;
        if (javaType == byte[].class) return true;
        if (Number.class.isAssignableFrom(javaType)) return true;

        // There has been discussion as to whether arrays themselves should
        // be regarded as multi-ref.
        // Here are the three options:
        //   1) Arrays are full-fledged Objects and therefore should always be
        //      multi-ref'd  (Pro: This is like java.  Con: Some runtimes don't
        //      support this yet, and it requires more stuff to be passed over the wire.)
        //   2) Arrays are not full-fledged Objects and therefore should
        //      always be passed as single ref (note the elements of the array
        //      may be multi-ref'd.) (Pro:  This seems reasonable, if a user
        //      wants multi-referencing put the array in a container.  Also
        //      is more interop compatible.  Con: Not like java serialization.)
        //   3) Arrays of primitives should be single ref, and arrays of
        //      non-primitives should be multi-ref.  (Pro: Takes care of the
        //      looping case.  Con: Seems like an obtuse rule.)
        //
        // Changing the code from (1) to (2) to see if interop fairs better.
        if (javaType.isArray()) return true;

        return false;
    }

    public DeserializerFactory getDeserializerFactory(Class cls) {
        if (isPrimitive(cls)) {
            return new SimpleDeserializerFactory(cls, new QName("xsd", cls.getName()));
        }

        return new BeanDeserializerFactory(BeanManager.getTypeDesc(cls));
    }

    public Serializer getSerializer(Class cls) {
        if (isPrimitive(cls)) {
            return new SimpleSerializer();
        }

        return new BeanSerializer(BeanManager.getTypeDesc(cls));
    }
    
    public void registerMapping(Class javaType,
                                QName xmlType,
                                Serializer serializer,
                                DeserializerFactory deserFactory) {

    }

    public void initializeSchemaTypes() {
        myRegisterSimple(Constants.XSD_STRING, java.lang.String.class);
        myRegisterSimple(Constants.XSD_BOOLEAN, java.lang.Boolean.class);
        myRegisterSimple(Constants.XSD_DOUBLE, java.lang.Double.class);
        myRegisterSimple(Constants.XSD_FLOAT, java.lang.Float.class);
        myRegisterSimple(Constants.XSD_INT, java.lang.Integer.class);
        myRegisterSimple(Constants.XSD_INTEGER, java.math.BigInteger.class
        );
        myRegisterSimple(Constants.XSD_DECIMAL, java.math.BigDecimal.class
        );
        myRegisterSimple(Constants.XSD_LONG, java.lang.Long.class);
        myRegisterSimple(Constants.XSD_SHORT, java.lang.Short.class);
        myRegisterSimple(Constants.XSD_BYTE, java.lang.Byte.class);

        // The XSD Primitives are mapped to java primitives.
        myRegisterSimple(Constants.XSD_BOOLEAN, boolean.class);
        myRegisterSimple(Constants.XSD_DOUBLE, double.class);
        myRegisterSimple(Constants.XSD_FLOAT, float.class);
        myRegisterSimple(Constants.XSD_INT, int.class);
        myRegisterSimple(Constants.XSD_LONG, long.class);
        myRegisterSimple(Constants.XSD_SHORT, short.class);
        myRegisterSimple(Constants.XSD_BYTE, byte.class);
    }

    private void myRegisterSimple(QName xmlType, Class javaType) {
        DeserializerFactory dser = new SimpleDeserializerFactory(javaType,
                                                                 xmlType);
        Serializer ser = new SimpleSerializer();
        registerMapping(javaType, xmlType, ser, dser);
    }
}
