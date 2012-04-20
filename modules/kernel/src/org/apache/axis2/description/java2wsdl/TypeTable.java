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

package org.apache.axis2.description.java2wsdl;

import org.apache.axiom.om.OMElement;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;


public class TypeTable {
    
    private static HashMap<String,QName>  simpleTypetoxsd;
    public static final QName ANY_TYPE = new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs");

    private HashMap<String,QName> complexTypeMap;

    private HashMap<String , QName> simpleTypeEnum;

    /**
     * this map is used to keep the class names with the Qnames.
     */
    private Map<QName, String> qNameToClassMap;
    /**
     * Keep simpleType to Java mapping separately so that
     * this table does not not populate it over and over.  
     */
    private static Map<QName, String> qNameToJavaTypeMap;

    public TypeTable() {
        //complex type table is resetted every time this is
        //instantiated
        complexTypeMap = new HashMap<String,QName>();
        this.qNameToClassMap = new HashMap<QName, String>();

        // keep qname of enum
        simpleTypeEnum = new HashMap<String , QName>();
    }

    /* statically populate the simple type map  - this is not likely to
    * change and we need not populate it over and over */
    static{
          populateSimpleTypes();
          populateJavaTypeMap();
    }

    /* populate the simpletype hashmap */
    private static void populateSimpleTypes() {
        simpleTypetoxsd = new HashMap<String,QName>();
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
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "unsignedShort", "xs"));
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
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "date", "xs"));
        simpleTypetoxsd.put("java.util.Calendar",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "dateTime", "xs"));        

        // SQL date time
         simpleTypetoxsd.put("java.sql.Date",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "date", "xs"));
         simpleTypetoxsd.put("java.sql.Time",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "time", "xs"));
        simpleTypetoxsd.put("java.sql.Timestamp",
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
        simpleTypetoxsd.put("java.lang.Object",ANY_TYPE);

        simpleTypetoxsd.put(URI.class.getName(), new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyURI", "xs"));

        simpleTypetoxsd.put(OMElement.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(ArrayList.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(Vector.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(List.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(Document.class.getName(), ANY_TYPE);
        //byteArrat
        simpleTypetoxsd.put("base64Binary",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "base64Binary", "xs"));
        simpleTypetoxsd.put(XMLGregorianCalendar.class.getName(),
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "date", "xs"));
    }
    
    private static void populateJavaTypeMap(){
    	/*
    	 * This Table populated according to the JAXB 2.0 XSD2Java binding. 
    	 * According to following table http://download.oracle.com/javaee/5/tutorial/doc/bnazq.html#bnazu 
    	 */
    	qNameToJavaTypeMap = new HashMap<QName, String>();
    	qNameToJavaTypeMap.put(Constants.XSD_STRING, String.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_INT, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_INTEGER, BigInteger.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_LONG, Long.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_SHORT, Short.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_DECIMAL, BigDecimal.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_FLOAT, Float.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_DOUBLE, Double.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_BOOLEAN, Boolean.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_BYTE, Byte.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_QNAME, QName.class.getName());     	
    	qNameToJavaTypeMap.put(Constants.XSD_UNSIGNEDINT, Long.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_UNSIGNEDSHORT, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_UNSIGNEDBYTE, Short.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_UNSIGNEDLONG, BigInteger.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_TIME, XMLGregorianCalendar.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_DATE, XMLGregorianCalendar.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_DATETIME, XMLGregorianCalendar.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_DURATION, Duration.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_NOTATION, QName.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_ANYURI, URI.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_ANY, Object.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_ANYSIMPLETYPE, Object.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_ANYTYPE, Object.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_NONNEGATIVEINTEGER, BigInteger.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_NONPOSITIVEINTEGER, BigInteger.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_NEGATIVEINTEGER, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_POSITIVEINTEGER, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_NORMALIZEDSTRING,String.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_POSITIVEINTEGER, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_POSITIVEINTEGER, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_POSITIVEINTEGER, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_POSITIVEINTEGER, Integer.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_BASE64, DataHandler.class.getName());
    	qNameToJavaTypeMap.put(Constants.XSD_HEXBIN, DataHandler.class.getName());
    	
    }

    /**
     * Return the schema type QName given the type class name
     * @param typeName  the name of the type
     * @return   the name of the simple type or null if it is not a simple type
     */
    public QName getSimpleSchemaTypeName(String typeName) {
        QName qName = (QName) simpleTypetoxsd.get(typeName);
        if(qName == null){
            if((typeName.startsWith("java.lang")||typeName.startsWith("javax.")) &&
                    !Exception.class.getName().equals(typeName)){
                return ANY_TYPE;
            }
        }
        return qName;
    }

    /**
     * Return whether the given type is a simple type or not
     * @param typeName the name of the type
     * @return  true if the type is a simple type
     */
    public boolean isSimpleType(String typeName) {
        
        if (simpleTypetoxsd.keySet().contains(typeName)){
            return true;
        }else if(typeName.startsWith("java.lang")||typeName.startsWith("javax.")){
            return true;
        }
        return false;
    }

    /**
     * Return the complex type map
     * @return  the map with complex types
     */
    public Map<String,QName> getSimpleTypeEnumMap() {
        return simpleTypeEnum;
    }

    public void addSimpleTypeEnum(String className, QName simpleSchemaType) {
        simpleTypeEnum.put(className, simpleSchemaType);
    }

    public QName getSimpleTypeEnum(String className) {
        return (QName) simpleTypeEnum.get(className);
    }

    /**
        * Return the complex type map
        * @return  the map with complex types
        */
       public Map<String,QName> getComplexSchemaMap() {
           return complexTypeMap;
       }

       public void addComplexSchema(String name, QName schemaType) {
           complexTypeMap.put(name, schemaType);
       }

       public QName getComplexSchemaType(String name) {
           return (QName) complexTypeMap.get(name);
       }

 
    /**
     * Gets the class name for QName.
     * first try the complex types if not try the simple types.
     *
     * @param qname the qname
     * @return the class name for QName
     */
    public String getClassNameForQName(QName qname) {
        String className = this.qNameToClassMap.get(qname);
        if(className == null){
        	className = qNameToJavaTypeMap.get(qname);
        }
		return className;
    }

    public void addClassNameForQName(QName qname, String className) {
        this.qNameToClassMap.put(qname, className);
    }

    /**
     * Get the qname for a type
     * first try the simple types if not try the complex types
     * @param typeName  name of the type
     * @return  the Qname for this type
     */
    public QName getQNamefortheType(String typeName) {
        QName type = getSimpleSchemaTypeName(typeName);
        if (type == null) {
            type = getComplexSchemaType(typeName);
        }
        return type;
    }
    
	/**
	 * Gets the schema type name.
	 *
	 * @param name the name
	 * @return the schema type name
	 */
	public QName getSchemaTypeName(String name) {
		QName qName = getSimpleSchemaTypeName(name);
		if (qName == null) {
		    qName = getSchemaTypeNameByClass(name);
		}
		if( qName == null){
			qName = getComplexSchemaType(name);
		}
		return qName;
	}
	
    /**
     * Gets the schema type name by class name. Sometimes it's required perform class
     * name mapping to find correct Schema type.
     * 
     * @param name
     *            the name
     * @return the schema type name by class
     */
    private QName getSchemaTypeNameByClass(String name) {
        /*
         * e.g 
         * XMLGregorianCalendar can be found as following classes.
         * 1.)com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl
         * 2.)org.apache.xerces.jaxp.datatype.XMLGregorianCalendarImpl
         */
        try {
            Class thisClass = Class.forName(name);
            if(XMLGregorianCalendar.class.isAssignableFrom(thisClass)) {
                return (QName) simpleTypetoxsd.get(XMLGregorianCalendar.class
                        .getName());   
                
            } else if(Calendar.class.isAssignableFrom(thisClass)) {     
                return (QName) simpleTypetoxsd.get(Calendar.class
                        .getName());                 
            }
        } catch (ClassNotFoundException e) {           
            e.printStackTrace();
        }
        
        return null;
    }
}


