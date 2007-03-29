package org.apache.axis2.schema.typemap;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.types.Day;
import org.apache.axis2.databinding.types.Duration;
import org.apache.axis2.databinding.types.Entities;
import org.apache.axis2.databinding.types.Entity;
import org.apache.axis2.databinding.types.HexBinary;
import org.apache.axis2.databinding.types.IDRef;
import org.apache.axis2.databinding.types.IDRefs;
import org.apache.axis2.databinding.types.Id;
import org.apache.axis2.databinding.types.Language;
import org.apache.axis2.databinding.types.Month;
import org.apache.axis2.databinding.types.MonthDay;
import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.types.NMToken;
import org.apache.axis2.databinding.types.NMTokens;
import org.apache.axis2.databinding.types.Name;
import org.apache.axis2.databinding.types.NegativeInteger;
import org.apache.axis2.databinding.types.NonNegativeInteger;
import org.apache.axis2.databinding.types.NonPositiveInteger;
import org.apache.axis2.databinding.types.NormalizedString;
import org.apache.axis2.databinding.types.Notation;
import org.apache.axis2.databinding.types.PositiveInteger;
import org.apache.axis2.databinding.types.Time;
import org.apache.axis2.databinding.types.Token;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.UnsignedByte;
import org.apache.axis2.databinding.types.UnsignedInt;
import org.apache.axis2.databinding.types.UnsignedLong;
import org.apache.axis2.databinding.types.UnsignedShort;
import org.apache.axis2.databinding.types.Year;
import org.apache.axis2.databinding.types.YearMonth;
import org.apache.axis2.schema.SchemaConstants;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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
 * Most code from Axis 1 Codebase
 */
public class JavaTypeMap implements TypeMap {

    public Map getTypeMap() {
        return typeMap;
    }

    private static Map typeMap = new HashMap();

    static {
        // If SOAP 1.1 over the wire, map wrapper classes to XSD primitives.
        addTypemapping(SchemaConstants.XSD_STRING,
                java.lang.String.class.getName());

        // The XSD Primitives are mapped to java primitives.
        addTypemapping(SchemaConstants.XSD_BOOLEAN, boolean.class.getName());
        addTypemapping(SchemaConstants.XSD_DOUBLE, double.class.getName());
        addTypemapping(SchemaConstants.XSD_FLOAT, float.class.getName());
        addTypemapping(SchemaConstants.XSD_INT, int.class.getName());
        addTypemapping(SchemaConstants.XSD_INTEGER,
                java.math.BigInteger.class.getName());
        addTypemapping(SchemaConstants.XSD_LONG, long.class.getName());
        addTypemapping(SchemaConstants.XSD_SHORT, short.class.getName());
        addTypemapping(SchemaConstants.XSD_BYTE, byte.class.getName());
        addTypemapping(SchemaConstants.XSD_ANY, OMElement.class.getName());
        addTypemapping(SchemaConstants.XSD_DECIMAL, BigDecimal.class.getName());

        //anytype is mapped to the OMElement instead of the java.lang.Object
        addTypemapping(SchemaConstants.XSD_ANYTYPE,
                OMElement.class.getName());

        //Qname maps to  jax rpc QName class
        addTypemapping(SchemaConstants.XSD_QNAME,
                javax.xml.namespace.QName.class.getName());

        //xsd Date is mapped to the java.util.date!
        addTypemapping(SchemaConstants.XSD_DATE,
                java.util.Date.class.getName());

        // Mapping for xsd:time.  Map to Axis type Time
        addTypemapping(SchemaConstants.XSD_TIME,
                Time.class.getName());
        addTypemapping(SchemaConstants.XSD_DATETIME,
                java.util.Calendar.class.getName());

        //as for the base 64 encoded binary stuff we map it to a javax.
        // activation.Datahandler object
        addTypemapping(SchemaConstants.XSD_BASE64,
                javax.activation.DataHandler.class.getName());
        
        addTypemapping(SchemaConstants.XSD_HEXBIN,
                HexBinary.class.getName());

        // These are the g* types (gYearMonth, etc) which map to Axis types
        addTypemapping(SchemaConstants.XSD_YEARMONTH,
                YearMonth.class.getName());
        addTypemapping(SchemaConstants.XSD_YEAR,
                Year.class.getName());
        addTypemapping(SchemaConstants.XSD_MONTH,
                Month.class.getName());
        addTypemapping(SchemaConstants.XSD_DAY,
                Day.class.getName());
        addTypemapping(SchemaConstants.XSD_MONTHDAY,
                MonthDay.class.getName());

        // xsd:token
        addTypemapping(SchemaConstants.XSD_TOKEN, Token.class.getName());

        // a xsd:normalizedString
        addTypemapping(SchemaConstants.XSD_NORMALIZEDSTRING,
                NormalizedString.class.getName());

        // a xsd:unsignedLong
        addTypemapping(SchemaConstants.XSD_UNSIGNEDLONG,
                UnsignedLong.class.getName());

        // a xsd:unsignedInt
        addTypemapping(SchemaConstants.XSD_UNSIGNEDINT,
                UnsignedInt.class.getName());

        // a xsd:unsignedShort
        addTypemapping(SchemaConstants.XSD_UNSIGNEDSHORT,
                UnsignedShort.class.getName());

        // a xsd:unsignedByte
        addTypemapping(SchemaConstants.XSD_UNSIGNEDBYTE,
                UnsignedByte.class.getName());

        // a xsd:nonNegativeInteger
        addTypemapping(SchemaConstants.XSD_NONNEGATIVEINTEGER,
                NonNegativeInteger.class.getName());

        // a xsd:negativeInteger
        addTypemapping(SchemaConstants.XSD_NEGATIVEINTEGER,
                NegativeInteger.class.getName());

        // a xsd:positiveInteger
        addTypemapping(SchemaConstants.XSD_POSITIVEINTEGER,
                PositiveInteger.class.getName());

        // a xsd:nonPositiveInteger
        addTypemapping(SchemaConstants.XSD_NONPOSITIVEINTEGER,
                NonPositiveInteger.class.getName());

        // a xsd:Name
        addTypemapping(SchemaConstants.XSD_NAME, Name.class.getName());

        // a xsd:NCName
        addTypemapping(SchemaConstants.XSD_NCNAME, NCName.class.getName());

        // a xsd:ID
        addTypemapping(SchemaConstants.XSD_ID, Id.class.getName());

        // a xml:lang
        // addTypemapping(SchemaConstants.XML_LANG,Language.class.getName());

        // a xsd:language
        addTypemapping(SchemaConstants.XSD_LANGUAGE, Language.class.getName());

        // a xsd:NmToken
        addTypemapping(SchemaConstants.XSD_NMTOKEN, NMToken.class.getName());

        // a xsd:NmTokens
        addTypemapping(SchemaConstants.XSD_NMTOKENS, NMTokens.class.getName());

        // a xsd:NOTATION
        addTypemapping(SchemaConstants.XSD_NOTATION, Notation.class.getName());

        // a xsd:XSD_ENTITY
        addTypemapping(SchemaConstants.XSD_ENTITY, Entity.class.getName());

        // a xsd:XSD_ENTITIES
        addTypemapping(SchemaConstants.XSD_ENTITIES, Entities.class.getName());

        // a xsd:XSD_IDREF
        addTypemapping(SchemaConstants.XSD_IDREF, IDRef.class.getName());

        // a xsd:XSD_XSD_IDREFS
        addTypemapping(SchemaConstants.XSD_IDREFS, IDRefs.class.getName());

        // a xsd:Duration
        addTypemapping(SchemaConstants.XSD_DURATION, Duration.class.getName());

        // a xsd:anyURI
        addTypemapping(SchemaConstants.XSD_ANYURI, URI.class.getName());


    }

    private static void addTypemapping(QName name, String str) {
        typeMap.put(name, str);
    }


}
