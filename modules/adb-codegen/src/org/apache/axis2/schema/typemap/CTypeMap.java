/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.schema.typemap;

import org.apache.axis2.wsdl.codegen.emitter.CTypeInfo;
import org.apache.axis2.schema.SchemaConstants;
import java.util.Map;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * The java type map. uses a static map for caching
 * Most code from Axis 1 Codebase*
 * Most code JavaTypeMap
 */
public class CTypeMap implements TypeMap{

    public Map getTypeMap(){
         return CTypeInfo.getTypeMap();
    }

    public Map getSoapEncodingTypesMap() {
        return soapEncodingTypeMap;
    }

    private static Map soapEncodingTypeMap = new HashMap();

    static {
        // populate the soapEncodingTypeMap
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_ARRAY,
                org.apache.axis2.databinding.types.soapencoding.Array.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_STRUCT,
                org.apache.axis2.databinding.types.soapencoding.Struct.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_BASE64,
                org.apache.axis2.databinding.types.soapencoding.Base64.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DURATION,
                org.apache.axis2.databinding.types.soapencoding.Duration.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DATETIME,
                org.apache.axis2.databinding.types.soapencoding.DateTime.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_NOTATION,
                org.apache.axis2.databinding.types.soapencoding.NOTATION.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_TIME,
                org.apache.axis2.databinding.types.soapencoding.Time.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DATE,
                org.apache.axis2.databinding.types.soapencoding.Date.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GYEARMONTH,
                org.apache.axis2.databinding.types.soapencoding.GYearMonth.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GYEAR,
                org.apache.axis2.databinding.types.soapencoding.GYear.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GMONTHDAY,
                org.apache.axis2.databinding.types.soapencoding.GMonthDay.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GDAY,
                org.apache.axis2.databinding.types.soapencoding.GDay.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_GMONTH,
                org.apache.axis2.databinding.types.soapencoding.GMonth.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_BOOLEAN,
                org.apache.axis2.databinding.types.soapencoding._boolean.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_BASE64BINARY,
                org.apache.axis2.databinding.types.soapencoding.Base64Binary.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_HEXBINARY,
                org.apache.axis2.databinding.types.soapencoding.HexBinary.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_FLOAT,
                org.apache.axis2.databinding.types.soapencoding._float.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_DOUBLE,
                org.apache.axis2.databinding.types.soapencoding._double.class.getName());
        addSoapEncodingTypeMapping(SchemaConstants.SOAP_ENCODING_ANYURI,
                org.apache.axis2.databinding.types.soapencoding.AnyURI.class.getName());
    }

    private static void addSoapEncodingTypeMapping(QName name, String className) {
        soapEncodingTypeMap.put(name, className);
    }
}
