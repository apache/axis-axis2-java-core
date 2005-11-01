package org.apache.axis2.databinding.schema;

import javax.xml.namespace.QName;
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
 * Constants for the QNames of standard schema types
 */
public class SchemaConstants {



    public static final String URI_DEFAULT_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";
    public static final QName XSD_STRING = new QName(URI_DEFAULT_SCHEMA_XSD, "string");
    public static final QName XSD_BOOLEAN = new QName(URI_DEFAULT_SCHEMA_XSD, "boolean");
    public static final QName XSD_DOUBLE = new QName(URI_DEFAULT_SCHEMA_XSD, "double");
    public static final QName XSD_FLOAT = new QName(URI_DEFAULT_SCHEMA_XSD, "float");
    public static final QName XSD_INT = new QName(URI_DEFAULT_SCHEMA_XSD, "int");
    public static final QName XSD_INTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "integer");
    public static final QName XSD_LONG = new QName(URI_DEFAULT_SCHEMA_XSD, "long");
    public static final QName XSD_SHORT = new QName(URI_DEFAULT_SCHEMA_XSD, "short");
    public static final QName XSD_BYTE = new QName(URI_DEFAULT_SCHEMA_XSD, "byte");
    public static final QName XSD_DECIMAL = new QName(URI_DEFAULT_SCHEMA_XSD, "decimal");
    public static final QName XSD_BASE64 = new QName(URI_DEFAULT_SCHEMA_XSD, "base64Binary");
    public static final QName XSD_HEXBIN = new QName(URI_DEFAULT_SCHEMA_XSD, "hexBinary");
    public static final QName XSD_ANYSIMPLETYPE = new QName(URI_DEFAULT_SCHEMA_XSD, "anySimpleType");
    public static final QName XSD_ANYTYPE = new QName(URI_DEFAULT_SCHEMA_XSD, "anyType");
    public static final QName XSD_ANY = new QName(URI_DEFAULT_SCHEMA_XSD, "any");
    public static final QName XSD_QNAME = new QName(URI_DEFAULT_SCHEMA_XSD, "QName");
    public static final QName XSD_DATETIME = new QName(URI_DEFAULT_SCHEMA_XSD, "dateTime");
    public static final QName XSD_DATE = new QName(URI_DEFAULT_SCHEMA_XSD, "date");
    public static final QName XSD_TIME = new QName(URI_DEFAULT_SCHEMA_XSD, "time");



    public static final Integer ATTRIBUTE_TYPE = new Integer(0);
    public static final Integer ANY_TYPE = new Integer(1);
    public static final Integer ELEMENT_TYPE = new Integer(2);
    public static final Integer ANY_ATTRIBUTE_TYPE = new Integer(3);
    public static final Integer ANY_ARRAY_TYPE = new Integer(4); 

}

