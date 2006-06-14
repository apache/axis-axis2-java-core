package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.databinding.types.*;
import org.apache.axis2.namespace.Constants;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

/**
 * The java type map. uses a static map for caching
 * Most code from Axis 1 Codebase*
 * Most code JavaTypeMap
 */
public class CTypeInfo {

    public static final String URI_DEFAULT_SCHEMA_XSD = Constants.URI_2001_SCHEMA_XSD;
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


    public static final QName XSD_UNSIGNEDLONG = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedLong");
    public static final QName XSD_UNSIGNEDINT = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedInt");
    public static final QName XSD_UNSIGNEDSHORT = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedShort");
    public static final QName XSD_UNSIGNEDBYTE = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedByte");
    public static final QName XSD_POSITIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "positiveInteger");
    public static final QName XSD_NEGATIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "negativeInteger");
    public static final QName XSD_NONNEGATIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "nonNegativeInteger");
    public static final QName XSD_NONPOSITIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "nonPositiveInteger");

    public static final QName XSD_YEARMONTH = new QName(URI_DEFAULT_SCHEMA_XSD, "gYearMonth");
    public static final QName XSD_MONTHDAY = new QName(URI_DEFAULT_SCHEMA_XSD, "gMonthDay");
    public static final QName XSD_YEAR = new QName(URI_DEFAULT_SCHEMA_XSD, "gYear");
    public static final QName XSD_MONTH = new QName(URI_DEFAULT_SCHEMA_XSD, "gMonth");
    public static final QName XSD_DAY = new QName(URI_DEFAULT_SCHEMA_XSD, "gDay");
    public static final QName XSD_DURATION = new QName(URI_DEFAULT_SCHEMA_XSD, "duration");

    public static final QName XSD_NAME = new QName(URI_DEFAULT_SCHEMA_XSD, "Name");
    public static final QName XSD_NCNAME = new QName(URI_DEFAULT_SCHEMA_XSD, "NCName");
    public static final QName XSD_NMTOKEN = new QName(URI_DEFAULT_SCHEMA_XSD, "NMTOKEN");
    public static final QName XSD_NMTOKENS = new QName(URI_DEFAULT_SCHEMA_XSD, "NMTOKENS");
    public static final QName XSD_NOTATION = new QName(URI_DEFAULT_SCHEMA_XSD, "NOTATION");
    public static final QName XSD_ENTITY = new QName(URI_DEFAULT_SCHEMA_XSD, "ENTITY");
    public static final QName XSD_ENTITIES = new QName(URI_DEFAULT_SCHEMA_XSD, "ENTITIES");
    public static final QName XSD_IDREF = new QName(URI_DEFAULT_SCHEMA_XSD, "IDREF");
    public static final QName XSD_IDREFS = new QName(URI_DEFAULT_SCHEMA_XSD, "IDREFS");
    public static final QName XSD_ANYURI = new QName(URI_DEFAULT_SCHEMA_XSD, "anyURI");
    public static final QName XSD_LANGUAGE = new QName(URI_DEFAULT_SCHEMA_XSD, "language");
    public static final QName XSD_ID = new QName(URI_DEFAULT_SCHEMA_XSD, "ID");
    public static final QName XSD_SCHEMA = new QName(URI_DEFAULT_SCHEMA_XSD, "schema");

    public static final QName XSD_NORMALIZEDSTRING = new QName(URI_DEFAULT_SCHEMA_XSD, "normalizedString");
    public static final QName XSD_TOKEN = new QName(URI_DEFAULT_SCHEMA_XSD, "token");


    public static Map getTypeMap() {
        return CTypeInfo.typeMap;
    }

    private static Map typeMap = new HashMap();

    static {
        // If SOAP 1.1 over the wire, map wrapper classes to XSD primitives.
        CTypeInfo.addTypemapping(XSD_STRING,
                "axis2_char_t*");

        // The XSD Primitives are mapped to java primitives.
        CTypeInfo.addTypemapping(XSD_BOOLEAN,"axis2_bool_t");
        //CTypeMap.addTypemapping(XSD_DOUBLE, double.class.getName());
        CTypeInfo.addTypemapping(XSD_FLOAT, "float");
        CTypeInfo.addTypemapping(XSD_INT, "int");
        CTypeInfo.addTypemapping(XSD_INTEGER,
                "int");
        CTypeInfo.addTypemapping(XSD_LONG, "long");
        CTypeInfo.addTypemapping(XSD_SHORT, "short");
        CTypeInfo.addTypemapping(XSD_BYTE, "byte");
        CTypeInfo.addTypemapping(XSD_ANY,  "axiom_node_t*");
        CTypeInfo.addTypemapping(XSD_DECIMAL, "int");

        //anytype is mapped to the OMElement instead of the java.lang.Object
        CTypeInfo.addTypemapping(XSD_ANYTYPE,
                "axiom_node_t*");

        //Qname maps to  jax rpc QName class
        CTypeInfo.addTypemapping(XSD_QNAME,
                "axis2_qname_t*");

        //xsd Date is mapped to the java.util.date!
        CTypeInfo.addTypemapping(XSD_DATE,
                "axis2_date_time_t*");

        // Mapping for xsd:time.  Map to Axis type Time
        CTypeInfo.addTypemapping(XSD_TIME,
                "axis2_date_time_t*");
        CTypeInfo.addTypemapping(XSD_DATETIME,
                "axis2_date_time_t*");

        //as for the base 64 encoded binary stuff we map it to a javax.
        // activation.Datahandler object
        CTypeInfo.addTypemapping(XSD_BASE64,
                "axis2_base64_binary_t*");

        CTypeInfo.addTypemapping(XSD_HEXBIN,
                "void*");

        // These are the g* types (gYearMonth, etc) which map to Axis types
        CTypeInfo.addTypemapping(XSD_YEARMONTH,
                "int");
        CTypeInfo.addTypemapping(XSD_YEAR,
                "int");
        CTypeInfo.addTypemapping(XSD_MONTH,
                "int");
        CTypeInfo.addTypemapping(XSD_DAY,
                "int");
        CTypeInfo.addTypemapping(XSD_MONTHDAY,
                "int");

        // xsd:token
        CTypeInfo.addTypemapping(XSD_TOKEN, Token.class.getName());

        // a xsd:normalizedString
        CTypeInfo.addTypemapping(XSD_NORMALIZEDSTRING,
                "axis2_char*");

        // a xsd:unsignedLong
        CTypeInfo.addTypemapping(XSD_UNSIGNEDLONG,
                "unsigned long");

        // a xsd:unsignedInt
        CTypeInfo.addTypemapping(XSD_UNSIGNEDINT,
                "unsigned int");

        // a xsd:unsignedShort
        CTypeInfo.addTypemapping(XSD_UNSIGNEDSHORT,
                "unsigned short");

        // a xsd:unsignedByte
        CTypeInfo.addTypemapping(XSD_UNSIGNEDBYTE,
                "unsinged char");

        // a xsd:nonNegativeInteger
        CTypeInfo.addTypemapping(XSD_NONNEGATIVEINTEGER,
                "unsinged int");

        // a xsd:negativeInteger
        CTypeInfo.addTypemapping(XSD_NEGATIVEINTEGER,
                "int");

        // a xsd:positiveInteger
        CTypeInfo.addTypemapping(XSD_POSITIVEINTEGER,
                "unsinged int");

        // a xsd:nonPositiveInteger
        CTypeInfo.addTypemapping(XSD_NONPOSITIVEINTEGER,
                "int");

        // a xsd:Name
        CTypeInfo.addTypemapping(XSD_NAME, Name.class.getName());

        // a xsd:NCName
        CTypeInfo.addTypemapping(XSD_NCNAME, NCName.class.getName());

        // a xsd:ID
        CTypeInfo.addTypemapping(XSD_ID, Id.class.getName());

        // a xml:lang
        // addTypemapping(XML_LANG,Language.class.getName());

        // a xsd:language
        CTypeInfo.addTypemapping(XSD_LANGUAGE, Language.class.getName());

        // a xsd:NmToken
        CTypeInfo.addTypemapping(XSD_NMTOKEN, NMToken.class.getName());

        // a xsd:NmTokens
        CTypeInfo.addTypemapping(XSD_NMTOKENS, NMTokens.class.getName());

        // a xsd:NOTATION
        CTypeInfo.addTypemapping(XSD_NOTATION, Notation.class.getName());

        // a xsd:XSD_ENTITY
        CTypeInfo.addTypemapping(XSD_ENTITY, Entity.class.getName());

        // a xsd:XSD_ENTITIES
        CTypeInfo.addTypemapping(XSD_ENTITIES, Entities.class.getName());

        // a xsd:XSD_IDREF
        CTypeInfo.addTypemapping(XSD_IDREF, IDRef.class.getName());

        // a xsd:XSD_XSD_IDREFS
        CTypeInfo.addTypemapping(XSD_IDREFS, IDRefs.class.getName());

        // a xsd:Duration
        CTypeInfo.addTypemapping(XSD_DURATION, Duration.class.getName());

        // a xsd:anyURI
        CTypeInfo.addTypemapping(XSD_ANYURI, URI.class.getName());


    }

    private static void addTypemapping(QName name, String str) {
        CTypeInfo.typeMap.put(name, str);
    }


}
