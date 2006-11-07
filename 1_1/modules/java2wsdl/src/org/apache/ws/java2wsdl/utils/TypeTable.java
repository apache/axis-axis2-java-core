package org.apache.ws.java2wsdl.utils;

import org.apache.axiom.om.OMElement;
import org.apache.ws.java2wsdl.Java2WSDLConstants;

import javax.xml.namespace.QName;
import java.util.*;
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
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "int", "xs"));
        simpleTypetoxsd.put("java.lang.String",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "string", "xs"));
        simpleTypetoxsd.put("boolean",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "boolean", "xs"));
        simpleTypetoxsd.put("float",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "float", "xs"));
        simpleTypetoxsd.put("double",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "double", "xs"));
        simpleTypetoxsd.put("short",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "short", "xs"));
        simpleTypetoxsd.put("long",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "long", "xs"));
        simpleTypetoxsd.put("byte",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "byte", "xs"));
        simpleTypetoxsd.put("char",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
        simpleTypetoxsd.put("java.lang.Integer",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "int", "xs"));
        simpleTypetoxsd.put("java.lang.Double",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "double", "xs"));
        simpleTypetoxsd.put("java.lang.Float",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "float", "xs"));
        simpleTypetoxsd.put("java.lang.Long",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "long", "xs"));
        simpleTypetoxsd.put("java.lang.Character",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
        simpleTypetoxsd.put("java.lang.Boolean",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "boolean", "xs"));
        simpleTypetoxsd.put("java.lang.Byte",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "byte", "xs"));
        simpleTypetoxsd.put("java.lang.Short",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "short", "xs"));
        simpleTypetoxsd.put("java.util.Date",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "dateTime", "xs"));
        simpleTypetoxsd.put("java.util.Calendar",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "dateTime", "xs"));

        simpleTypetoxsd.put("java.lang.Object",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));

        // Any types
        simpleTypetoxsd.put(OMElement.class.getName(),
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
        simpleTypetoxsd.put(ArrayList.class.getName(),
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
        simpleTypetoxsd.put(Vector.class.getName(),
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
        simpleTypetoxsd.put(List.class.getName(),
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
         simpleTypetoxsd.put(HashMap.class.getName(),
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
         simpleTypetoxsd.put(Hashtable.class.getName(),
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs"));
        //byteArrat
        simpleTypetoxsd.put("base64Binary",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "base64Binary", "xs"));
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

    public void addComplexSchema(String name, QName schemaType) {
        complecTypeMap.put(name, schemaType);
    }

    public QName getComplexSchemaType(String name) {
        return (QName) complecTypeMap.get(name);
    }

    public QName getQNamefortheType(String typeName) {
        QName type = getSimpleSchemaTypeName(typeName);
        if (type == null) {
            type = getComplexSchemaType(typeName);
        }
        return type;
    }
}


