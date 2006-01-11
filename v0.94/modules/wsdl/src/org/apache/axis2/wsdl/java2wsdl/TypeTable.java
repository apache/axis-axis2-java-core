package org.apache.axis2.wsdl.java2wsdl;

import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
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
*
*/

public class TypeTable {
    private HashMap simpleTypetoxsd;
    private HashMap complecTypeMap;

    public TypeTable() {
        simpleTypetoxsd = new HashMap();
        complecTypeMap = new HashMap();
        populateSimpleTypes();
    }

    private void populateSimpleTypes() {
        //todo pls use the types from org.apache.ws.commons.schema.constants.Constants
        simpleTypetoxsd.put("int",
                new QName("http://www.w3.org/2001/XMLSchema", "int", "xs"));
        simpleTypetoxsd.put("java.lang.String",
                new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        simpleTypetoxsd.put("boolean",
                new QName("http://www.w3.org/2001/XMLSchema", "boolean", "xs"));
        simpleTypetoxsd.put("float",
                new QName("http://www.w3.org/2001/XMLSchema", "float", "xs"));
        simpleTypetoxsd.put("double",
                new QName("http://www.w3.org/2001/XMLSchema", "double", "xs"));
        simpleTypetoxsd.put("short",
                new QName("http://www.w3.org/2001/XMLSchema", "short", "xs"));
        simpleTypetoxsd.put("long",
                new QName("http://www.w3.org/2001/XMLSchema", "long", "xs"));
        simpleTypetoxsd.put("byte",
                new QName("http://www.w3.org/2001/XMLSchema", "byte", "xs"));
        simpleTypetoxsd.put("char",
                new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xs"));
        simpleTypetoxsd.put("java.lang.Integer",
                new QName("http://www.w3.org/2001/XMLSchema", "int", "xs"));
        simpleTypetoxsd.put("java.lang.Double",
                new QName("http://www.w3.org/2001/XMLSchema", "double", "xs"));
        simpleTypetoxsd.put("java.lang.Float",
                new QName("http://www.w3.org/2001/XMLSchema", "float", "xs"));
        simpleTypetoxsd.put("java.lang.Long",
                new QName("http://www.w3.org/2001/XMLSchema", "long", "xs"));
        simpleTypetoxsd.put("java.lang.Character",
                new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xs"));
        simpleTypetoxsd.put("java.lang.Boolean",
                new QName("http://www.w3.org/2001/XMLSchema", "boolean", "xs"));
        simpleTypetoxsd.put("java.lang.Byte",
                new QName("http://www.w3.org/2001/XMLSchema", "byte", "xs"));
        simpleTypetoxsd.put("java.lang.Short",
                new QName("http://www.w3.org/2001/XMLSchema", "short", "xs"));
        simpleTypetoxsd.put("java.util.Date",
                new QName("http://www.w3.org/2001/XMLSchema", "dateTime", "xs"));
        simpleTypetoxsd.put("java.util.Calendar",
                new QName("http://www.w3.org/2001/XMLSchema", "dateTime", "xs"));

        simpleTypetoxsd.put("java.lang.Object",
                new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xs"));

        // Any types
        simpleTypetoxsd.put(OMElement.class.getName(),
                new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xs"));
        simpleTypetoxsd.put(ArrayList.class.getName(),
                new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xs"));
        simpleTypetoxsd.put(Vector.class.getName(),
                new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xs"));
        simpleTypetoxsd.put(List.class.getName(),
                new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xs"));
    }

    public QName getSimpleSchemaTypeName(String typename) {
        return (QName) simpleTypetoxsd.get(typename);
    }

    public boolean isSimpleType(String typeName) {
        Iterator keys = simpleTypetoxsd.keySet().iterator();
        while (keys.hasNext()) {
            String s = (String) keys.next();
            if (s.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    public void addComplexScheam(String name, QName schemaType) {
        complecTypeMap.put(name, schemaType);
    }

    public QName getComplexScheamType(String name) {
        return (QName) complecTypeMap.get(name);
    }

    public QName getQNamefortheType(String typeName) {
        QName type = getSimpleSchemaTypeName(typeName);
        if (type == null) {
            type = getComplexScheamType(typeName);
        }
        return type;
    }
}

