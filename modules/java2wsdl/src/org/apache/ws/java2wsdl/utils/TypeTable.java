package org.apache.ws.java2wsdl.utils;

import org.apache.axiom.om.OMElement;
import org.apache.ws.java2wsdl.Java2WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private HashMap complexTypeMap;

    private static final Log log = LogFactory.getLog(TypeTable.class);
    private static final QName ANY_TYPE = new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs");

    public TypeTable() {
        simpleTypetoxsd = new HashMap();
        complexTypeMap = new HashMap();
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
                ANY_TYPE);
        simpleTypetoxsd.put("java.lang.Integer",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "int", "xs"));
        simpleTypetoxsd.put("java.lang.Double",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "double", "xs"));
        simpleTypetoxsd.put("java.lang.Float",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "float", "xs"));
        simpleTypetoxsd.put("java.lang.Long",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "long", "xs"));
        simpleTypetoxsd.put("java.lang.Character",
                ANY_TYPE);
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

         //consider BigDecimal, BigInteger, Day, Duration, Month, MonthDay,
        //Time, Year, YearMonth as SimpleType as well
        simpleTypetoxsd.put("java.math.BigDecimal",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "decimal", "xs"));
        simpleTypetoxsd.put("java.math.BigInteger",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "integer", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Day",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gDay", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Duration",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "duration", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Month",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gMonth", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.MonthDay",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gMonthDay", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Time",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "time", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Year",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gYear", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.YearMonth",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gYearMonth", "xs"));       

        simpleTypetoxsd.put("java.lang.Object",
                ANY_TYPE);

        // Any types
        simpleTypetoxsd.put(OMElement.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(ArrayList.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(Vector.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(List.class.getName(),
                ANY_TYPE);
         simpleTypetoxsd.put(HashMap.class.getName(),
                 ANY_TYPE);
         simpleTypetoxsd.put(Hashtable.class.getName(),
                 ANY_TYPE);
        //byteArrat
        simpleTypetoxsd.put("base64Binary",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "base64Binary", "xs"));
    }

    public QName getSimpleSchemaTypeName(String typeName) {
        QName qName = (QName) simpleTypetoxsd.get(typeName);
        if(qName == null){
            if(typeName.startsWith("java.lang")||typeName.startsWith("javax.")){
                return ANY_TYPE;
            }
        }
        return qName;
    }

    public boolean isSimpleType(String typeName) {
        Iterator keys = simpleTypetoxsd.keySet().iterator();
        while (keys.hasNext()) {
            String s = (String) keys.next();
            if (s.equals(typeName)) {
                return true;
            }
        }
        if(typeName.startsWith("java.lang")||typeName.startsWith("javax.")){
            return true;
        }
        return false;
    }

    public Map getComplexSchemaMap() {
        return complexTypeMap;
    }

    public void addComplexSchema(String name, QName schemaType) {
        complexTypeMap.put(name, schemaType);
    }

    public QName getComplexSchemaType(String name) {
        return (QName) complexTypeMap.get(name);
    }

    public QName getQNamefortheType(String typeName) {
        QName type = getSimpleSchemaTypeName(typeName);
        if (type == null) {
            type = getComplexSchemaType(typeName);
        }
        return type;
    }
}


