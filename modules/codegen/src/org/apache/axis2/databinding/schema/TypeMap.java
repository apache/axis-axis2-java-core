package org.apache.axis2.databinding.schema;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
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

public class TypeMap {
    
    public static Map getTypeMap() {
        return typeMap;
    }

    private static Map typeMap =  new HashMap();

    static{
// If SOAP 1.1 over the wire, map wrapper classes to XSD primitives.
        addTypemapping(Constants.XSD_STRING, java.lang.String.class);
        addTypemapping(Constants.XSD_BOOLEAN, java.lang.Boolean.class);
        addTypemapping(Constants.XSD_DOUBLE, java.lang.Double.class);
        addTypemapping(Constants.XSD_FLOAT, java.lang.Float.class);
        addTypemapping(Constants.XSD_INT, java.lang.Integer.class);
        addTypemapping(Constants.XSD_INTEGER, java.math.BigInteger.class
        );
        addTypemapping(Constants.XSD_DECIMAL, java.math.BigDecimal.class
        );
        addTypemapping(Constants.XSD_LONG, java.lang.Long.class);
        addTypemapping(Constants.XSD_SHORT, java.lang.Short.class);
        addTypemapping(Constants.XSD_BYTE, java.lang.Byte.class);

        // The XSD Primitives are mapped to java primitives.
        addTypemapping(Constants.XSD_BOOLEAN, boolean.class);
        addTypemapping(Constants.XSD_DOUBLE, double.class);
        addTypemapping(Constants.XSD_FLOAT, float.class);
        addTypemapping(Constants.XSD_INT, int.class);
        addTypemapping(Constants.XSD_LONG, long.class);
        addTypemapping(Constants.XSD_SHORT, short.class);
        addTypemapping(Constants.XSD_BYTE, byte.class);

    }
    private static void addTypemapping(QName name,Class clazz) {
        typeMap.put( name,clazz);
    }


}
