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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.DataHandler;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ws.commons.schema.constants.Constants;



/**
 * The Class TypeTableTest is used to test
 * {@link org.apache.axis2.description.java2wsdl.TypeTable TypeTable} class.
 * 
 * @since 1.7.0 
 * 
 */
public class TypeTableTest extends TestCase {
	
	/** The type table. */
	private TypeTable typeTable;	
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();
		typeTable = new TypeTable();
	}
 
	/**
	 * Test get class name for QName.
	 */
	public void testGetClassNameForQName() {
		assertEquals("Failed to receive expected Class type",
				String.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_STRING));
		
		assertEquals("Failed to receive expected Class type",
				BigInteger.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_INTEGER));
		
		assertEquals("Failed to receive expected Class type",
				QName.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_QNAME));
		
		assertEquals("Failed to receive expected Class type",
				Object.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_ANY));
		
		assertEquals("Failed to receive expected Class type",
				DataHandler.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_BASE64));
		
		assertEquals("Failed to receive expected Class type",
				DataHandler.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_HEXBIN));
		
		assertNull("NULl value expected",
				typeTable.getClassNameForQName(Constants.XSD_LANGUAGE));
	}
	
	
    public void testGetSchemaTypeName() {
        String className = null;    
        QName dateType = new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD,
                "date", "xs");
        TypeTable typeTable = new TypeTable();
        
        className = "com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl";
        assertEquals("Not the expected value", dateType,
                typeTable.getSchemaTypeName(className));
        
        className = TestXMLGregorianCalendarImpl.class.getName();
        assertEquals("Not the expected value", dateType,
                typeTable.getSchemaTypeName(className));
        
        className = GregorianCalendar.class.getName();
        dateType = new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD,
                "dateTime", "xs");
        System.out.println( typeTable.getSchemaTypeName(className));
        assertEquals("Not the expected value", dateType,
                typeTable.getSchemaTypeName(className));
        
        className = TestCalendarImpl.class.getName();
        assertNull("Not the expected value",
                typeTable.getSchemaTypeName(className));
    }
    
    
    //Following test is relted to apache XMLSchema libaray usage
    public void testXMLSchemaConstantsUsage(){
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_STRING), String.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_INT), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_INTEGER), BigInteger.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_LONG), Long.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_SHORT), Short.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_DECIMAL), BigDecimal.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_FLOAT), Float.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_DOUBLE), Double.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_BOOLEAN), Boolean.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_BYTE), Byte.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_QNAME), QName.class.getName());         
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_UNSIGNEDINT), Long.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_UNSIGNEDSHORT), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_UNSIGNEDBYTE), Short.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_UNSIGNEDLONG), BigInteger.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_TIME), XMLGregorianCalendar.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_DATE), XMLGregorianCalendar.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_DATETIME), XMLGregorianCalendar.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_DURATION), Duration.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_NOTATION), QName.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_ANYURI), URI.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_ANY), Object.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_ANYSIMPLETYPE), Object.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_ANYTYPE), Object.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_NONNEGATIVEINTEGER), BigInteger.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_NONPOSITIVEINTEGER), BigInteger.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_NEGATIVEINTEGER), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_POSITIVEINTEGER), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_NORMALIZEDSTRING),String.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_POSITIVEINTEGER), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_POSITIVEINTEGER), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_POSITIVEINTEGER), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_POSITIVEINTEGER), Integer.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_BASE64), DataHandler.class.getName());
        assertEquals(typeTable.getClassNameForQName(Constants.XSD_HEXBIN), DataHandler.class.getName());
    }

    class TestXMLGregorianCalendarImpl extends XMLGregorianCalendar {
        @Override
        public void clear() {
        }

        @Override
        public void reset() {

        }

        @Override
        public void setYear(BigInteger year) {

        }

        @Override
        public void setYear(int year) {

        }

        @Override
        public void setMonth(int month) {

        }

        @Override
        public void setDay(int day) {

        }

        @Override
        public void setTimezone(int offset) {

        }

        @Override
        public void setHour(int hour) {

        }

        @Override
        public void setMinute(int minute) {

        }

        @Override
        public void setSecond(int second) {

        }

        @Override
        public void setMillisecond(int millisecond) {

        }

        @Override
        public void setFractionalSecond(BigDecimal fractional) {

        }

        @Override
        public BigInteger getEon() {

            return null;
        }

        @Override
        public int getYear() {

            return 0;
        }

        @Override
        public BigInteger getEonAndYear() {

            return null;
        }

        @Override
        public int getMonth() {

            return 0;
        }

        @Override
        public int getDay() {

            return 0;
        }

        @Override
        public int getTimezone() {

            return 0;
        }

        @Override
        public int getHour() {

            return 0;
        }

        @Override
        public int getMinute() {

            return 0;
        }

        @Override
        public int getSecond() {

            return 0;
        }

        @Override
        public BigDecimal getFractionalSecond() {

            return null;
        }

        @Override
        public int compare(XMLGregorianCalendar xmlGregorianCalendar) {

            return 0;
        }

        @Override
        public XMLGregorianCalendar normalize() {

            return null;
        }

        @Override
        public String toXMLFormat() {

            return null;
        }

        @Override
        public QName getXMLSchemaType() {

            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void add(Duration duration) {

        }

        @Override
        public GregorianCalendar toGregorianCalendar() {

            return null;
        }

        @Override
        public GregorianCalendar toGregorianCalendar(TimeZone timezone,
                Locale aLocale, XMLGregorianCalendar defaults) {

            return null;
        }

        @Override
        public TimeZone getTimeZone(int defaultZoneoffset) {

            return null;
        }

        @Override
        public Object clone() {

            return null;
        }

    }

    class TestCalendarImpl {

    }

}
