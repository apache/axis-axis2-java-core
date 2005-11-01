package org.apache.axis2.databinding.schema.typemap;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.databinding.schema.SchemaConstants;

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

/**
 * The java type map. uses a static map for caching
 */
public class JavaTypeMap implements TypeMap {
    
    public Map getTypeMap() {
        return typeMap;
    }

    private static Map typeMap =  new HashMap();

    static{
        // If SOAP 1.1 over the wire, map wrapper classes to XSD primitives.
        addTypemapping(SchemaConstants.XSD_STRING, java.lang.String.class.getName());

        // The XSD Primitives are mapped to java primitives.
        addTypemapping(SchemaConstants.XSD_BOOLEAN, boolean.class.getName());
        addTypemapping(SchemaConstants.XSD_DOUBLE, double.class.getName());
        addTypemapping(SchemaConstants.XSD_FLOAT, float.class.getName());
        addTypemapping(SchemaConstants.XSD_INT, int.class.getName());
        addTypemapping(SchemaConstants.XSD_LONG, long.class.getName());
        addTypemapping(SchemaConstants.XSD_SHORT, short.class.getName());
        addTypemapping(SchemaConstants.XSD_BYTE, byte.class.getName());
        addTypemapping(SchemaConstants.XSD_ANY, OMElement.class.getName());
        addTypemapping(SchemaConstants.XSD_ANYTYPE, OMElement.class.getName());

    }
    private static void addTypemapping(QName name,String str) {
        typeMap.put( name,str);
    }


}
