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

package org.apache.axis2.databinding.utils;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.databinding.types.NormalizedString;

public class ConverterUtilTest extends TestCase {

    /** Test conversion of Big Integer */
    public void testBigInteger() {
        List l = new ArrayList();
        l.add("23445");
        l.add("23446");
        l.add("23456646");
        l.add("1113646");

        Object convertedObj = ConverterUtil.convertToArray(
                BigInteger.class, l);

        assertTrue(convertedObj.getClass().isArray());
        assertTrue(convertedObj.getClass().equals(BigInteger[].class));

    }

    public void testBigDecimal() {
        String inputString = "0.0000000000";
        BigDecimal decimal = new BigDecimal(inputString);
        String outputString = ConverterUtil.convertToString(decimal);
        System.out.println("BigDecimal==> " + outputString);

        assertEquals(inputString, outputString);
    }

    /** integer arrays */
    public void testInt() {
        List l = new ArrayList();
        l.add("23445");
        l.add("23446");
        l.add("23456646");
        l.add("1113646");

        Object convertedObj = ConverterUtil.convertToArray(
                int.class, l);

        assertTrue(convertedObj.getClass().isArray());
        assertTrue(convertedObj.getClass().equals(int[].class));

    }

    /** boolean arrays */
    public void testBool() {
        List l = new ArrayList();
        l.add("true");
        l.add("false");
        l.add("true");
        l.add("false");

        Object convertedObj = ConverterUtil.convertToArray(
                boolean.class, l);

        assertTrue(convertedObj.getClass().isArray());
        assertTrue(convertedObj.getClass().equals(boolean[].class));

    }

    public void testConvertToDateTime() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Calendar calendar;

        calendar = ConverterUtil.convertToDateTime("2007-02-15T14:54:29");
        System.out.println("String   ==> " + "2007-02-15T14:54:29");
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));

        calendar = ConverterUtil.convertToDateTime("2007-02-15T14:54:29.399");
        System.out.println("String   ==> " + "2007-02-15T14:54:29.399");
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));

        calendar = ConverterUtil.convertToDateTime("2007-02-15T14:54:29+05:30");
        System.out.println("String   ==> " + "2007-02-15T14:54:29+05:30");
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));

        calendar = ConverterUtil.convertToDateTime("2007-02-15T14:54:29.399+05:30");
        System.out.println("String   ==> " + "2007-02-15T14:54:29.399+05:30");
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));

        calendar = ConverterUtil.convertToDateTime("2007-02-15T14:54:29Z");
        System.out.println("String   ==> " + "2007-02-15T14:54:29Z");
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));

        calendar = ConverterUtil.convertToDateTime("2007-02-15T14:54:29.399Z");
        System.out.println("String   ==> " + "2007-02-15T14:54:29.399Z");
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));

        calendar = ConverterUtil.convertToDateTime("2006-12-11T23:57:16.625Z");
        System.out.println("String   ==> " + "2006-12-11T23:57:16.625Z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));

        calendar = ConverterUtil.convertToDateTime("2007-02-15T14:54:29.399-05:30");
        System.out.println("String   ==> " + "2007-02-15T14:54:29.399-05:30");
        System.out.println("calendar ==> " + simpleDateFormat.format(calendar.getTime()));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(calendar));
        
        TimeZone currentTimeZone = TimeZone.getDefault();
        System.out.println("before  Test: TimeZone is "+TimeZone.getDefault().getDisplayName());
        try {
            // run tests with default JVM time zone
            this.internalTestConvertToDateTime();

            // We use two time zone with GMT+1. One north and one south. 
            // We hope that one is currently using daylight savings and the
            // other is not using it.

            // run tests with time zone "Europe/Berlin"
            System.out.println( "setting time zone to Europe/Berlin" );
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
            this.internalTestConvertToDateTime();
            
            // run tests with time zone "Africa/Windhoek"
            System.out.println( "setting time zone to Africa/Windhoek" );
            TimeZone.setDefault(TimeZone.getTimeZone("Africa/Windhoek"));
            this.internalTestConvertToDateTime();

            // run tests with time zone "Australia/Darwin"
            System.out.println( "setting time zone to Australia/Darwin" );
            TimeZone.setDefault(TimeZone.getTimeZone("Australia/Darwin"));
            this.internalTestConvertToDateTime();
                        
            // run tests with time zone "US/Mountain"
            System.out.println( "setting time zone to US/Mountain" );
            TimeZone.setDefault(TimeZone.getTimeZone("US/Mountain"));
            this.internalTestConvertToDateTime();

        } finally {
            TimeZone.setDefault( currentTimeZone );
            System.out.println("after   Test: TimeZone is "+TimeZone.getDefault().getDisplayName());
        }

    }

    public void testConvertToDateString() {
        Date date = new Date();
        String dateString = ConverterUtil.convertToString(date);
        System.out.println("Date ==> " + dateString);
    }

    public void testConvertToDate() {

        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddZ");
        date = ConverterUtil.convertToDate("2007-02-15");
        System.out.println("String   ==> " + "2007-02-15");
        System.out.println("calendar ==> " + simpleDateFormat.format(date));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(date));

        date = ConverterUtil.convertToDate("2007-02-15Z");
        System.out.println("String   ==> " + "2007-02-15Z");
        System.out.println("calendar ==> " + simpleDateFormat.format(date));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(date));

        date = ConverterUtil.convertToDate("2007-02-15+05:30");
        System.out.println("String   ==> " + "2007-02-15+05:30");
        System.out.println("calendar ==> " + simpleDateFormat.format(date));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(date));

        date = ConverterUtil.convertToDate("2007-02-15-12:30");
        System.out.println("String   ==> " + "2007-02-15-12:30");
        System.out.println("calendar ==> " + simpleDateFormat.format(date));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(date));
        
        //Included two new tests from here http://www.w3.org/TR/NOTE-datetime
        date = ConverterUtil.convertToDate("2011-11-05T08:15:30-05:00");
        System.out.println("String   ==> " + "2011-11-05T08:15:30-05:00");
        System.out.println("calendar ==> " + simpleDateFormat.format(date));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(date));
        
        date = ConverterUtil.convertToDate("1994-11-05T13:15:30Z");
        System.out.println("String   ==> " + "1994-11-05T13:15:30Z");
        System.out.println("calendar ==> " + simpleDateFormat.format(date));
        System.out.println("calendar ==> " + ConverterUtil.convertToString(date));

    }

    public void testConvertCalendarToString() {
        
        TimeZone timeZone = TimeZone.getTimeZone("Australia/Perth");
        Calendar c = Calendar.getInstance(timeZone);
        c.clear();
        c.set(2008, Calendar.JANUARY, 1);
        TestCase.assertTrue(ConverterUtil.convertToString(c).endsWith("+09:00"));
        
    }
    
    public void testconvertToDateXML() {

        Date date = null;
        String dateStr = null;

        dateStr = "2007-02-15";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

        dateStr = "2007-02-15Z";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

        dateStr = "2007-02-15+05:30";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

        dateStr = "2007-02-15-12:30";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

        dateStr = "1997-07-16T19:20:30.45+01:00";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

        dateStr = "2011-09-27T14:43:55.162+05:30";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

        dateStr = "1997-07-16T19:20:30+01:00";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

        dateStr = "1994-11-05T13:15:30Z";
        date = ConverterUtil.convertXmlToDate(dateStr);
        assertNotNull(date);

    }
    
	public void testConvertToStringFromDataHandler() {
		String inStr = "Sample Data";
		DataSource ds = new ByteArrayDataSource(inStr.getBytes());
		DataHandler dh = new DataHandler(ds);
		String rawOutStr = ConverterUtil.convertToString(dh);
		String outStr = new String(Base64Utils.decode(rawOutStr));
		assertEquals("Not expected content", inStr, outStr);
	}
	
	/**
     * Used to by formatCalendar* to format millisecond field.
     * @param millis the millisecond value
     * @param digits the number of digits to use
     *        (0=none)
     * @return something like ".123" or "" (when digits=0)
     */
    private static String formatMillis(int millis, int digits) {
        if (digits == 0) return "";
        StringBuffer sb = new StringBuffer(16);
        sb.append('.');
        sb.append(millis);
        while( sb.length() > digits && sb.charAt(sb.length()-1) == '0' ) {
            sb.deleteCharAt(sb.length()-1); // remove trailing '0'
        }
        while( sb.length() < digits+1 ) {
            sb.append('0'); // add trailing '0'
        }
        return sb.toString();
    }
    
    /**
     * Format a Calendar object to a schema datetime string.
     * Used by testConvertToDateTime().
     * @param c the object to format
     * @param millisDigits number of digits used for millisecond field
     *                     (0 = none)
     * @param tz the time zone to use (null = default time zone)
     * @return something like "1111-22-33T44:55:66.789"
     */
    private static String formatCalendarXsd(Calendar c, int millisDigits, TimeZone tz)
    {
        String formatString = "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
        if (tz != null) simpleDateFormat.setTimeZone(tz);
        StringBuffer sb = new StringBuffer(simpleDateFormat.format(c.getTime()));
        sb.append(formatMillis(c.get(Calendar.MILLISECOND), millisDigits));
        return sb.toString();
    }
    
    /**
     * Format a Calendar object to a schema datetime string with time zone
     * Used by testConvertToDateTime().
     * @param c the object to format
     * @param millisDigits number of digits used for millisecond field
     *                     (0 = none)
     * @param tz the timezone to use (null = default timezone)
     * @return something like "1111-22-33T44:55:66.789+01:00"
     */
    private static String formatCalendarXsdWithTz(Calendar c, int millisDigits, TimeZone tz)
    {
        String formatString = "yyyy-MM-dd'T'HH:mm:ssZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
        if (tz != null) simpleDateFormat.setTimeZone(tz);
        StringBuffer sb = new StringBuffer(simpleDateFormat.format(c.getTime()));
        sb.insert(19, formatMillis(c.get(Calendar.MILLISECOND), millisDigits));
        return sb.insert(sb.length()-2, ':').toString(); // fix tz format
    }
    
    /**
     * Format a Calendar object to a schema datetime string with time zone UTC
     * Used by testConvertToDateTime().
     * @param c the object to format
     * @param millisDigits number of digits used for millisecond field
     *                     (0 = none)
     * @return something like "1111-22-33T44:55:66.789Z"
     */
    private static String formatCalendarXsdZulu(Calendar c, int millisDigits)
    {
        String formatString = "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        StringBuffer sb = new StringBuffer(simpleDateFormat.format(c.getTime()));
        sb.append(formatMillis(c.get(Calendar.MILLISECOND), millisDigits));
        sb.append('Z');
        return sb.toString();
    }
    
    private void internalTestConvertToDateTime()
    {
        System.out.println("testing with TimeZone "
                + TimeZone.getDefault().getDisplayName());
        System.out.println("uses daylight time: "
                + TimeZone.getDefault().useDaylightTime());
        System.out.println("we are in daylight time: "
                + TimeZone.getDefault().inDaylightTime(new Date()));

        String testValue;  // holds the testvalue
        TimeZone timeZone; // the (custom) time zone of testvalue
        Calendar calendar; // the value to test
        String back;       // the value converted back by convertToString
        
        // local date without daylight savings in "Europe/Berlin"
        testValue = "2007-02-15T14:54:29";
        timeZone = null;
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 0, null));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsd(calendar, 0, null));
        assertEquals("should be in local timezone",
                     TimeZone.getDefault().getOffset(calendar.getTimeInMillis()),
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // local date with daylight savings in "Europe/Berlin"
        testValue = "2007-05-27T23:20:33";
        timeZone = null;
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 0, null));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsd(calendar, 0, null));
        assertEquals("should be in local timezone",
                     TimeZone.getDefault().getOffset(calendar.getTimeInMillis()),
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // local date with milliseconds without daylight savings
        testValue = "2007-02-15T14:54:29.399";
        timeZone = null;
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 3, null));
        System.out.println("back      ==> " + back);
        assertEquals(testValue,
                     formatCalendarXsd(calendar, 3, null));
        assertEquals("should be in local timezone",
                     TimeZone.getDefault().getOffset(calendar.getTimeInMillis()),
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // local date with milliseconds with daylight savings
        testValue = "2009-06-12T23:20:33.2";
        timeZone = null;
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 1, null));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsd(calendar, 1, null));
        assertEquals("should be in local timezone",
                     TimeZone.getDefault().getOffset(calendar.getTimeInMillis()),
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date with time zone (positive) no milliseconds
        testValue = "2007-02-15T14:54:29+05:30";
        timeZone = TimeZone.getTimeZone("GMT+05:30"); //custom time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 0, timeZone));
        System.out.println("back      ==> " + back);
        // make sure that timeZone.getRawOffset() is sufficient
        assertFalse ("custom time zone should not use day light saving", 
                     timeZone.useDaylightTime());
        assertEquals(testValue, 
                     formatCalendarXsdWithTz(calendar, 0, timeZone));
        assertEquals("should be in timezone +05:30",
                     timeZone.getRawOffset(),
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date with time zone (negative) with milliseconds
        testValue = "2007-02-15T14:54:29.399-05:30";
        timeZone = TimeZone.getTimeZone("GMT-05:30"); //custom time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 3, timeZone));
        System.out.println("back      ==> " + back);
        // make sure that timeZone.getRawOffset() is sufficient
        assertFalse ("custom time zone should not use day light saving", 
                     timeZone.useDaylightTime());
        assertEquals(testValue, 
                     formatCalendarXsdWithTz(calendar, 3, timeZone));
        assertEquals("should be in timezone -05:30",
                     timeZone.getRawOffset(),
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date with time zone and milliseconds
        testValue = "2007-02-15T14:54:29.399+05:30";
        timeZone = TimeZone.getTimeZone("GMT+05:30"); //custom time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 3, timeZone));
        System.out.println("back      ==> " + back);
        // make sure that timeZone.getRawOffset() is sufficient
        assertFalse ("custom time zone should not use day light saving", 
                     timeZone.useDaylightTime());
        assertEquals(testValue, 
                     formatCalendarXsdWithTz(calendar, 3, timeZone));
        assertEquals("should be in timezone +05:30",
                     timeZone.getRawOffset(),
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date within UTC (Zulu time zone)
        testValue = "2007-02-15T14:54:29Z";
        timeZone = TimeZone.getTimeZone("GMT"); //UTC time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 0, timeZone));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsdZulu(calendar,0));
        assertEquals("should be in UTC",
                     0,
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date within UTC (Zulu time zone) with milliseconds
        testValue = "2007-02-15T14:54:29.399Z";
        timeZone = TimeZone.getTimeZone("GMT"); //UTC time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 3, timeZone));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsdZulu(calendar, 3));
        assertEquals("should be in UTC",
                     0,
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date within UTC (Zulu time zone) with milliseconds
        testValue = "2006-12-11T23:57:16.62Z";
        timeZone = TimeZone.getTimeZone("GMT"); //UTC time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 2, timeZone));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsdZulu(calendar, 2));
        assertEquals("should be in UTC",
                     0,
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date within UTC (Zulu time zone) with milliseconds
        testValue = "2012-05-18T13:57:01.7Z";
        timeZone = TimeZone.getTimeZone("GMT"); //UTC time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 1, timeZone));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsdZulu(calendar, 1));
        assertEquals("should be in UTC",
                     0,
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));

        // date within UTC (Zulu time zone) with milliseconds
        testValue = "2012-05-17T13:57:01.123000000Z";
        timeZone = TimeZone.getTimeZone("GMT"); //UTC time zone
        calendar = ConverterUtil.convertToDateTime(testValue);
        back = ConverterUtil.convertToString(calendar);
        System.out.println("testValue ==> " + testValue);
        System.out.println("calendar  ==> " + formatCalendarXsdWithTz(calendar, 9, timeZone));
        System.out.println("back      ==> " + back);
        assertEquals(testValue, 
                     formatCalendarXsdZulu(calendar, 9));
        assertEquals("should be in UTC",
                     0,
                     calendar.get(Calendar.ZONE_OFFSET)+calendar.get(Calendar.DST_OFFSET));        
    }
    
    public void testConvertToNormalizedString() {
        NormalizedString str = ConverterUtil.convertToNormalizedString("abc");
        assertNotNull(str);      
        str = ConverterUtil.convertToNormalizedString("");
        assertNotNull(str);
        str = ConverterUtil.convertToNormalizedString(null);
        assertNull(str);      

    }


}
