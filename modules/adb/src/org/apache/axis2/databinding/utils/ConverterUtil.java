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

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.llom.OMStAXWrapper;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;
import org.apache.axis2.databinding.i18n.ADBMessages;
import org.apache.axis2.databinding.types.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Converter methods to go from 1. simple type -> String 2. simple type -> Object 3. String ->
 * simpletype 4. Object list -> array
 */
public class ConverterUtil {

    private static Log log = LogFactory.getLog(ConverterUtil.class);

    private static final String POSITIVE_INFINITY = "INF";
    private static final String NEGATIVE_INFINITY = "-INF";

    public static final String SYSTEM_PROPERTY_ADB_CONVERTERUTIL = "adb.converterutil";

    private static boolean isCustomClassPresent;
    private static Class customClass;

    /* String conversion methods */
    public static String convertToString(int i) {
        return Integer.toString(i);
    }

    public static String convertToString(float i) {
        return Float.toString(i);
    }

    public static String convertToString(long i) {
        return Long.toString(i);
    }

    public static String convertToString(double i) {
        return Double.toString(i);
    }

    public static String convertToString(byte i) {
        return Byte.toString(i);
    }

    public static String convertToString(char i) {
        return Character.toString(i);
    }

    public static String convertToString(short i) {
        return Short.toString(i);
    }

    public static String convertToString(boolean i) {
        return Boolean.toString(i);
    }

    public static String convertToString(Date value) {
        if (isCustomClassPresent) {
            // this means user has define a seperate converter util class
            return invokeToStringMethod(value,Date.class);
        } else {
            // lexical form of the date is '-'? yyyy '-' mm '-' dd zzzzzz?
            // we have to serialize it with the GMT timezone
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddZ");
            // this does not create the semicolen need so add that.
            String dateString = simpleDateFormat.format(value);
            // append semicolen
            dateString = dateString.substring(0, dateString.length() - 2) +
                    ":" + dateString.substring(dateString.length() - 2);

            return dateString;
        }
    }

    private static String invokeToStringMethod(Object value, Class type) {

        try {
            Method method = customClass.getMethod("convertToString", new Class[]{type});
            String result = (String) method.invoke(null,new Object[]{value});
            return result;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("can not find the method convertToString("
                    + type.getName() + ") in converter util class " + customClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("can not access the method convertToString("
                    + type.getName() + ") in converter util class " + customClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("can not invocate the method convertToString("
                    + type.getName() + ") in converter util class " + customClass.getName(), e);
        }
    }

    public static String convertToString(Calendar value) {
        if (isCustomClassPresent) {
            return invokeToStringMethod(value,Calendar.class);
        } else {
            // lexical form of the calendar is '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
            SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
            // Sun JDK bug http://developer.java.sun.com/developer/bugParade/bugs/4229798.html
            return zulu.format(value.getTime());
        }
    }

    public static String convertToString(Day o) {
        return o.toString();
    }

    public static String convertToString(YearMonth o) {
        return o.toString();
    }

    public static String convertToString(Year o) {
        return o.toString();
    }

    public static String convertToString(HexBinary o) {
        return o.toString();
    }

    public static String convertToString(MonthDay o) {
        return o.toString();
    }

    public static String convertToString(Time o) {
        return o.toString();
    }

    public static String convertToString(Byte o) {
        return o.toString();
    }

    public static String convertToString(BigInteger o) {
        return o.toString();
    }

    public static String convertToString(Integer o) {
        return o.toString();
    }

    public static String convertToString(Long o) {
        return o.toString();
    }

    public static String convertToString(Short o) {
        return o.toString();
    }

    public static String convertToString(UnsignedByte o) {
        return o.toString();
    }

    public static String convertToString(UnsignedInt o) {
        return o.toString();
    }

    public static String convertToString(UnsignedLong o) {
        return o.toString();
    }

    public static String convertToString(QName o) {
        if (o != null) {
            return o.getLocalPart();
        } else {
            return "";
        }
    }

    public static String convertToString(Object o) {
        return o.toString();
    }

    public static String convertToString(Double o) {
        return o.toString();
    }

    public static String convertToString(Duration o) {
        return o.toString();
    }

    public static String convertToString(Float o) {
        return o.toString();
    }

    public static String convertToString(Month o) {
        return o.toString();
    }

    public static String convertToString(byte[] bytes) {
        return Base64.encode(bytes);
    }

    public static String convertToString(javax.activation.DataHandler handler) {
        return getStringFromDatahandler(handler);
    }

    /* ################################################################################ */
    /* String to java type conversions
       These methods have a special signature structure
       <code>convertTo</code> followed by the schema type name
       Say for int, convertToint(String) is the converter method

       Not very elegant but it seems to be the only way!

    */


    public static int convertToInt(String s) {
        if ((s == null) || s.equals("")){
            return Integer.MIN_VALUE;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return Integer.parseInt(s);
    }

    public static BigDecimal convertToBigDecimal(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new BigDecimal(s);
    }

    public static double convertToDouble(String s) {
        if ((s == null) || s.equals("")){
            return Double.NaN;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        if (POSITIVE_INFINITY.equals(s)) {
            return Double.POSITIVE_INFINITY;
        } else if (NEGATIVE_INFINITY.equals(s)) {
            return Double.NEGATIVE_INFINITY;
        }
        return Double.parseDouble(s);
    }

    public static BigDecimal convertToDecimal(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new BigDecimal(s);
    }

    public static float convertToFloat(String s) {
        if ((s == null) || s.equals("")){
            return Float.NaN;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        if (POSITIVE_INFINITY.equals(s)) {
            return Float.POSITIVE_INFINITY;
        } else if (NEGATIVE_INFINITY.equals(s)) {
            return Float.NEGATIVE_INFINITY;
        }
        return Float.parseFloat(s);
    }

    public static String convertToString(String s) {
        return s;
    }

    public static long convertToLong(String s) {
        if ((s == null) || s.equals("")){
            return Long.MIN_VALUE;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return Long.parseLong(s);
    }

    public static short convertToShort(String s) {
        if ((s == null) || s.equals("")){
            return Short.MIN_VALUE;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return Short.parseShort(s);
    }

    public static boolean convertToBoolean(String s) {

        boolean returnValue = false;
        if ((s != null) && (s.length() > 0)) {
            if ("1".equals(s) || s.toLowerCase().equals("true")) {
                returnValue = true;
            } else if (!"0".equals(s) && !s.toLowerCase().equals("false")) {
                throw new RuntimeException("in valid string -" + s + " for boolean value");
            }
        }
        return returnValue;
    }

    public static String convertToAnySimpleType(String s) {
        return s;
    }

    public static OMElement convertToAnyType(String s) {
        try {
            XMLStreamReader r = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(s.getBytes()));
            StAXOMBuilder builder = new StAXOMBuilder(OMAbstractFactory.getOMFactory(), r);
            return builder.getDocumentElement();
        } catch (XMLStreamException e) {
            return null;
        }
    }

    public static YearMonth convertToGYearMonth(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new YearMonth(s);
    }

    public static MonthDay convertToGMonthDay(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new MonthDay(s);
    }

    public static Year convertToGYear(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Year(s);
    }

    public static Month convertToGMonth(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Month(s);
    }

    public static Day convertToGDay(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Day(s);
    }

    public static Duration convertToDuration(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Duration(s);
    }


    public static HexBinary convertToHexBinary(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new HexBinary(s);
    }

    public static javax.activation.DataHandler convertToBase64Binary(String s) {
        // reusing the byteArrayDataSource from the Axiom classes
        if ((s == null) || s.equals("")){
            return null;
        }
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(
                Base64.decode(s)
        );
        return new DataHandler(byteArrayDataSource);
    }

    public static javax.activation.DataHandler convertToDataHandler(String s) {
        return convertToBase64Binary(s);
    }

    /**
     * Converts a given string into a date. Code from Axis1 DateDeserializer.
     *
     * @param source
     * @return Returns Date.
     */
    public static Date convertToDate(String source) {

        // the lexical form of the date is '-'? yyyy '-' mm '-' dd zzzzzz?
        if ((source == null) || source.equals("")){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = null;
        boolean bc = false;
        if (source.startsWith("-")) {
            source = source.substring(1);
            bc = true;
        }

        if ((source != null) && (source.length() >= 10)) {
            if (source.length() == 10) {
                //i.e this stirng has only the compulsory part
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            } else {
                String restpart = source.substring(10);
                if (restpart.startsWith("Z")) {
                    // this is a gmt time zone value
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                } else if (restpart.startsWith("+") || restpart.startsWith("-")) {
                    // this is a specific time format string
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddz");
                    // have to add the GMT part to process the message
                    source = source.substring(0, 10) + "GMT" + restpart;
                } else {
                    throw new RuntimeException("In valid string sufix");
                }
            }
        } else {
            throw new RuntimeException("In valid string to parse");
        }

        Date date;
        try {
            date = simpleDateFormat.parse(source);
            if (bc) {
                calendar.setTime(date);
                calendar.set(Calendar.ERA, GregorianCalendar.BC);
                date = calendar.getTime();
            }
        } catch (ParseException e) {
            throw new RuntimeException("In valid string to parse");
        }

        return date;
    }

    public static Time convertToTime(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Time(s);
    }

    public static Token convertToToken(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Token(s);
    }


    public static NormalizedString convertToNormalizedString(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new NormalizedString(s);
    }

    public static UnsignedLong convertToUnsignedLong(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new UnsignedLong(s);
    }

    public static UnsignedInt convertToUnsignedInt(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new UnsignedInt(s);
    }

    public static UnsignedShort convertToUnsignedShort(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new UnsignedShort(s);
    }

    public static UnsignedByte convertToUnsignedByte(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new UnsignedByte(s);
    }

    public static NonNegativeInteger convertToNonNegativeInteger(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new NonNegativeInteger(s);
    }

    public static NegativeInteger convertToNegativeInteger(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new NegativeInteger(s);
    }

    public static PositiveInteger convertToPositiveInteger(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new PositiveInteger(s);
    }

    public static NonPositiveInteger convertToNonPositiveInteger(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new NonPositiveInteger(s);
    }

    public static Name convertToName(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Name(s);
    }

    public static NCName convertToNCName(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new NCName(s);
    }

    public static Id convertToID(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Id(s);
    }

    public static Id convertToId(String s) {
        return convertToID(s);
    }

    public static Language convertToLanguage(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Language(s);
    }

    public static NMToken convertToNMTOKEN(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new NMToken(s);
    }

    public static NMTokens convertToNMTOKENS(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new NMTokens(s);
    }

    public static Notation convertToNOTATION(String s) {
        return null; //todo Need to fix this
        // return new Notation(s);
    }

    public static Entity convertToENTITY(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Entity(s);
    }

    public static Entities convertToENTITIES(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new Entities(s);
    }

    public static IDRef convertToIDREF(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new IDRef(s);
    }

    public static IDRefs convertToIDREFS(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        return new IDRefs(s);
    }

    public static URI convertToURI(String s){
        if ((s == null) || s.equals("")){
            return null;
        }
        return convertToAnyURI(s);
    }

    public static URI convertToAnyURI(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        try {
            return new URI(s);
        } catch (URI.MalformedURIException e) {
            throw new ObjectConversionException(
                    ADBMessages.getMessage("converter.cannotParse", s), e);
        }
    }

    public static BigInteger convertToInteger(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new BigInteger(s);
    }

    public static BigInteger convertToBigInteger(String s) {
        if ((s == null) || s.equals("")){
            return null;
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return convertToInteger(s);
    }

    public static byte convertToByte(String s) {
        if ((s == null) || s.equals("")){
            return Byte.MIN_VALUE;
        }
        return Byte.parseByte(s);
    }

    /**
     * Code from Axis1 code base Note - We only follow the convention in the latest schema spec
     *
     * @param source
     * @return Returns Calendar.
     */
    public static Calendar convertToDateTime(String source) {

        if ((source == null) || source.equals("")){
            return null;
        }
        // the lexical representation of the date time as follows
        // '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
        SimpleDateFormat simpleDateFormat = null;
        Date date = null;
        Calendar calendar = Calendar.getInstance();

        if (source.startsWith("-")) {
            source = source.substring(1);
            calendar.set(Calendar.ERA, GregorianCalendar.BC);
        }

        try {
            if ((source != null) && (source.length() >= 19)) {
                if (source.length() == 19) {
                    // i.e. this does not have any additional assume this time in current local
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                } else {
                    String rest = source.substring(19);
                    if (rest.startsWith(".")) {
                        // i.e this have the ('.'s+) part
                        if (rest.endsWith("Z")) {
                            // this is in gmt time zone
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        } else if ((rest.lastIndexOf("+") > 0) || (rest.lastIndexOf("-") > 0)) {
                            // this is given in a general time zione
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSz");
                            if (rest.lastIndexOf("+") > 0) {
                                source = source.substring(0, source.lastIndexOf("+")) + "GMT" +
                                        rest.substring(rest.lastIndexOf("+"));
                            } else if (rest.lastIndexOf("-") > 0) {
                                source = source.substring(0, source.lastIndexOf("-")) + "GMT" +
                                        rest.substring(rest.lastIndexOf("-"));
                            }

                        } else {
                            // i.e it does not have time zone
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
                        }

                    } else {
                        if (rest.startsWith("Z")) {
                            // this is in gmt time zone
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        } else if (rest.startsWith("+") || rest.startsWith("-")) {
                            // this is given in a general time zione
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
                            source = source.substring(0, 19) + "GMT" + rest;
                        } else {
                            throw new NumberFormatException("in valid time zone attribute");
                        }
                    }
                }
                date = simpleDateFormat.parse(source);
                calendar.setTime(date);

            } else {
                throw new NumberFormatException("date string can not be less than 19 charactors");
            }
        } catch (ParseException e) {
            throw new NumberFormatException(e.getMessage());
        }
        return calendar;
    }

    /**
     * Code from Axis1 code base
     *
     * @param source
     * @return Returns QName.
     */
    public static QName convertToQName(String source, String nameSpaceuri) {
        source = source.trim();
        int colon = source.lastIndexOf(":");
        //context.getNamespaceURI(source.substring(0, colon));
        String localPart = colon < 0 ? source : source.substring(colon + 1);
        String perfix = colon <= 0 ? "" : source.substring(0, colon);
        return new QName(nameSpaceuri, localPart, perfix);
    }

    /* ################################################################# */

    /* java Primitive types to Object conversion methods */
    public static Object convertToObject(String i) {
        return i;
    }

    public static Object convertToObject(boolean i) {
        return Boolean.valueOf(i);
    }

    public static Object convertToObject(double i) {
        return new Double(i);
    }

    public static Object convertToObject(byte i) {
        return new Byte(i);
    }

    public static Object convertToObject(char i) {
        return new Character(i);
    }

    public static Object convertToObject(short i) {
        return new Short(i);
    }

    /* list to array conversion methods */

    public static Object convertToArray(Class baseArrayClass, String[] valueArray) {
        //create a list using the string array
        List valuesList = new ArrayList(valueArray.length);
        for (int i = 0; i < valueArray.length; i++) {
            valuesList.add(valueArray[i]);

        }

        return convertToArray(baseArrayClass, valuesList);
    }


    /**
     * @param baseArrayClass
     * @param objectList     -> for primitive type array conversion we assume the content to be
     *                       strings!
     * @return Returns Object.
     */
    public static Object convertToArray(Class baseArrayClass, List objectList) {
        int listSize = objectList.size();
        Object returnArray = null;
        if (int.class.equals(baseArrayClass)) {
            int[] array = new int[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = Integer.parseInt(o.toString());
                } else {
                    array[i] = Integer.MIN_VALUE;
                }
            }
            returnArray = array;
        } else if (float.class.equals(baseArrayClass)) {
            float[] array = new float[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = Float.parseFloat(o.toString());
                } else {
                    array[i] = Float.NaN;
                }
            }
            returnArray = array;
        } else if (short.class.equals(baseArrayClass)) {
            short[] array = new short[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = Short.parseShort(o.toString());
                } else {
                    array[i] = Short.MIN_VALUE;
                }
            }
            returnArray = array;
        } else if (byte.class.equals(baseArrayClass)) {
            byte[] array = new byte[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = Byte.parseByte(o.toString());
                } else {
                    array[i] = Byte.MIN_VALUE;
                }
            }
            returnArray = array;
        } else if (long.class.equals(baseArrayClass)) {
            long[] array = new long[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = Long.parseLong(o.toString());
                } else {
                    array[i] = Long.MIN_VALUE;
                }
            }
            returnArray = array;
        } else if (boolean.class.equals(baseArrayClass)) {
            boolean[] array = new boolean[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = Boolean.parseBoolean(o.toString());
                }
            }
            returnArray = array;
        } else if (char.class.equals(baseArrayClass)) {
            char[] array = new char[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = o.toString().toCharArray()[0];
                }
            }
            returnArray = array;
        } else if (double.class.equals(baseArrayClass)) {
            double[] array = new double[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    array[i] = Double.parseDouble(o.toString());
                } else {
                    array[i] = Double.NaN;
                }
            }
            returnArray = array;
        } else if (Calendar.class.equals(baseArrayClass)) {
            Calendar[] array = new Calendar[listSize];
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    if (o instanceof String){
                        array[i] = ConverterUtil.convertToDateTime(o.toString());
                    } else if (o instanceof Calendar) {
                        array[i] = (Calendar) o;
                    }
                }
            }
            returnArray = array;
        } else {
            returnArray = Array.newInstance(baseArrayClass, listSize);
            ConvertToArbitraryObjectArray(returnArray, baseArrayClass, objectList);
        }
        return returnArray;
    }

    /**
     * @param returnArray
     * @param baseArrayClass
     * @param objectList
     */
    private static void ConvertToArbitraryObjectArray(Object returnArray,
                                                      Class baseArrayClass,
                                                      List objectList) {
        if (!(ADBBean.class.isAssignableFrom(baseArrayClass))) {
            try {
                for (int i = 0; i < objectList.size(); i++) {
                    Object o = objectList.get(i);
                    if (o == null) {
                        // if the string is null the object value must be null
                        Array.set(returnArray, i, null);
                    } else {
                        Array.set(returnArray, i, getObjectForClass(
                                baseArrayClass,
                                o.toString()));
                    }

                }
                return;
            } catch (Exception e) {
                //oops! - this cannot be converted fall through and
                //try the other alternative
            }
        }

        try {
            objectList.toArray((Object[])returnArray);
        } catch (Exception e) {
            //we are over with alternatives - throw the
            //converison exception
            throw new ObjectConversionException(e);
        }
    }

    /**
     * We could have used the Arraya.asList() method but that returns an *immutable* list !!!!!
     *
     * @param array
     * @return list
     */
    public static List toList(Object[] array) {
        if (array == null) {
            return new ArrayList();
        } else {
            ArrayList list = new ArrayList();
            for (int i = 0; i < array.length; i++) {
                list.add(array[i]);
            }
            return list;
        }
    }

    /**
     * @param intValue
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static int compare(int intValue, String value) {
        return intValue - Integer.parseInt(value);
    }

    /**
     * @param doubleValue
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static double compare(double doubleValue, String value) {
        return doubleValue - Double.parseDouble(value);
    }


    /**
     * @param floatValue
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static float compare(float floatValue, String value) {
        return floatValue - Float.parseFloat(value);
    }

    /**
     * @param longValue
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static long compare(long longValue, String value) {
        return longValue - Long.parseLong(value);
    }

    /**
     * @param shortValue
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static int compare(short shortValue, String value) {
        return shortValue - Short.parseShort(value);
    }

    /**
     * @param byteVlaue
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static int compare(byte byteVlaue, String value) {
        return byteVlaue - Byte.parseByte(value);
    }


    /**
     * @param binBigInteger
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static int compare(BigInteger binBigInteger, String value) {
        return binBigInteger.intValue() - Integer.parseInt(value);
    }

    /**
     * @param binBigDecimal
     * @param value
     * @return 0 if equal , + value if greater than , - value if less than
     */
    public static double compare(BigDecimal binBigDecimal, String value) {
        return binBigDecimal.doubleValue() - Double.parseDouble(value);
    }

    public static long compare(Duration duration, String value) {
        Duration compareValue = new Duration(value);
        return duration.compare(compareValue);
    }

    public static long compare(Date date, String value) {
        Date newDate = convertToDate(value);
        return date.getTime() - newDate.getTime();
    }

    public static long compare(Time time, String value) {
        Time newTime = new Time(value);
        return time.getAsCalendar().getTimeInMillis() - newTime.getAsCalendar().getTimeInMillis();
    }

    public static long compare(Calendar calendar, String value) {
        Calendar newCalendar = convertToDateTime(value);
        return calendar.getTimeInMillis() - newCalendar.getTimeInMillis();
    }

    /**
     * Converts the given .datahandler to a string
     *
     * @return string
     */
    public static String getStringFromDatahandler(DataHandler dataHandler) {
        try {
            InputStream inStream;
            if (dataHandler == null) {
                return "";
            }
            inStream = dataHandler.getDataSource().getInputStream();
            byte[] data = IOUtils.getStreamAsByteArray(inStream);
            return Base64.encode(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A reflection based method to generate an instance of a given class and populate it with a
     * given value
     *
     * @param clazz
     * @param value
     * @return object
     */
    public static Object getObjectForClass(Class clazz, String value) {
        //first see whether this class has a constructor that can
        //take the string as an argument.
        try {
            Constructor stringConstructor = clazz.getConstructor(new Class[] { String.class });
            return stringConstructor.newInstance(new Object[] { value });
        } catch (NoSuchMethodException e) {
            //oops - no such constructors - continue with the
            //parse method
        } catch (Exception e) {
            throw new ObjectConversionException(
                    ADBMessages.getMessage("converter.cannotGenerate",
                                           clazz.getName()), e);
        }

        try {
            Method parseMethod = clazz.getMethod("parse", new Class[] { String.class });
            Object instance = clazz.newInstance();
            return parseMethod.invoke(instance, new Object[] { value });
        } catch (NoSuchMethodException e) {
            throw new ObjectConversionException(e);
        } catch (Exception e) {
            throw new ObjectConversionException(
                    ADBMessages.getMessage("converter.cannotGenerate",
                                           clazz.getName()),
                    e);
        }

    }

    /** A simple exception that is thrown when the conversion fails */
    public static class ObjectConversionException extends RuntimeException {
        public ObjectConversionException() {
        }

        public ObjectConversionException(String message) {
            super(message);
        }

        public ObjectConversionException(Throwable cause) {
            super(cause);
        }

        public ObjectConversionException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    // serialization methods for xsd any type
    public static void serializeAnyType(Object value, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        if (value instanceof String) {
            serializeAnyType("string", value.toString(), xmlStreamWriter);
        } else if (value instanceof Integer) {
            serializeAnyType("int", value.toString(), xmlStreamWriter);
        } else if (value instanceof Boolean) {
            serializeAnyType("boolean", value.toString(), xmlStreamWriter);
        } else if (value instanceof URI) {
            serializeAnyType("anyURI", value.toString(), xmlStreamWriter);
        } else if (value instanceof Byte) {
            serializeAnyType("byte", value.toString(), xmlStreamWriter);
        } else if (value instanceof Date) {
            serializeAnyType("date", convertToString((Date) value), xmlStreamWriter);
        } else if (value instanceof Calendar) {
            serializeAnyType("dateTime", convertToString((Calendar) value), xmlStreamWriter);
        } else if (value instanceof Time) {
            serializeAnyType("time", convertToString((Time) value), xmlStreamWriter);
        } else if (value instanceof Float) {
            serializeAnyType("float", value.toString(), xmlStreamWriter);
        } else if (value instanceof Long) {
            serializeAnyType("long", value.toString(), xmlStreamWriter);
        } else if (value instanceof Double) {
            serializeAnyType("double", value.toString(), xmlStreamWriter);
        } else if (value instanceof Short) {
            serializeAnyType("short", value.toString(), xmlStreamWriter);
        } else if (value instanceof BigDecimal) {
            serializeAnyType("decimal", value.toString(), xmlStreamWriter);
        } else if (value instanceof DataHandler) {
            addTypeAttribute(xmlStreamWriter,"base64Binary");
            MTOMAwareXMLStreamWriter mtomAwareXMLStreamWriter = (MTOMAwareXMLStreamWriter) xmlStreamWriter;
            mtomAwareXMLStreamWriter.writeDataHandler((DataHandler)value);
        } else if (value instanceof QName) {
            QName qNameValue = (QName) value;
            String prefix = xmlStreamWriter.getPrefix(qNameValue.getNamespaceURI());
            if (prefix == null) {
                prefix = BeanUtil.getUniquePrefix();
                xmlStreamWriter.writeNamespace(prefix, qNameValue.getNamespaceURI());
                xmlStreamWriter.setPrefix(prefix, qNameValue.getNamespaceURI());
            }
            String attributeValue = qNameValue.getLocalPart();
            if (!prefix.equals("")) {
                attributeValue = prefix + ":" + attributeValue;
            }
            serializeAnyType("QName", attributeValue, xmlStreamWriter);
        } else if (value instanceof UnsignedByte) {
            serializeAnyType("unsignedByte", convertToString((UnsignedByte) value), xmlStreamWriter);
        } else if (value instanceof UnsignedLong) {
            serializeAnyType("unsignedLong", convertToString((UnsignedLong) value), xmlStreamWriter);
        } else if (value instanceof UnsignedShort) {
            serializeAnyType("unsignedShort", convertToString((UnsignedShort) value), xmlStreamWriter);
        } else if (value instanceof UnsignedInt) {
            serializeAnyType("unsignedInt", convertToString((UnsignedInt) value), xmlStreamWriter);
        } else if (value instanceof PositiveInteger) {
            serializeAnyType("positiveInteger", convertToString((PositiveInteger) value), xmlStreamWriter);
        } else if (value instanceof NegativeInteger) {
            serializeAnyType("negativeInteger", convertToString((NegativeInteger) value), xmlStreamWriter);
        } else if (value instanceof NonNegativeInteger) {
            serializeAnyType("nonNegativeInteger", convertToString((NonNegativeInteger) value), xmlStreamWriter);
        } else if (value instanceof NonPositiveInteger) {
            serializeAnyType("nonPositiveInteger", convertToString((NonPositiveInteger) value), xmlStreamWriter);
        } else {
            throw new XMLStreamException("Unknow type can not serialize");
        }
    }


    /**
     * this method writes the xsi:type attrubte and the value to the xmlstreamwriter
     * to serialize the anytype object
     * @param type  - xsd type of the attribute
     * @param value - string value of the object
     * @param xmlStreamWriter
     * @throws XMLStreamException
     */
    private static void serializeAnyType(String type,
                                         String value,
                                         XMLStreamWriter xmlStreamWriter)
            throws XMLStreamException {

        addTypeAttribute(xmlStreamWriter, type);
        xmlStreamWriter.writeCharacters(value);
    }

    private static void addTypeAttribute(XMLStreamWriter xmlStreamWriter, String type) throws XMLStreamException {
        String prefix = xmlStreamWriter.getPrefix(Constants.XSI_NAMESPACE);
        if (prefix == null) {
            prefix = BeanUtil.getUniquePrefix();
            xmlStreamWriter.writeNamespace(prefix, Constants.XSI_NAMESPACE);
            xmlStreamWriter.setPrefix(prefix, Constants.XSI_NAMESPACE);
        }

        prefix = xmlStreamWriter.getPrefix(Constants.XSD_NAMESPACE);
        if (prefix == null) {
            prefix = BeanUtil.getUniquePrefix();
            xmlStreamWriter.writeNamespace(prefix, Constants.XSD_NAMESPACE);
            xmlStreamWriter.setPrefix(prefix, Constants.XSD_NAMESPACE);
        }

        String attributeValue = null;
        if (prefix.equals("")) {
            attributeValue = type;
        } else {
            attributeValue = prefix + ":" + type;
        }

        xmlStreamWriter.writeAttribute(Constants.XSI_NAMESPACE, "type", attributeValue);
    }

    public static Object getAnyTypeObject(XMLStreamReader xmlStreamReader,
                                          Class extensionMapperClass) throws XMLStreamException {
        Object returnObject = null;

        // make sure reader is at the first element.
        while(!xmlStreamReader.isStartElement()){
            xmlStreamReader.next();
        }
        // first check whether this element is null or not
        String nillableValue = xmlStreamReader.getAttributeValue(Constants.XSI_NAMESPACE, "nil");
        if ("true".equals(nillableValue) || "1".equals(nillableValue)){
            returnObject = null;
        } else {
            String attributeType = xmlStreamReader.getAttributeValue(Constants.XSI_NAMESPACE, "type");
            if (attributeType != null) {
                String attributeTypePrefix = "";
                if (attributeType.indexOf(":") > -1) {
                    attributeTypePrefix = attributeType.substring(0,attributeType.indexOf(":"));
                    attributeType = attributeType.substring(attributeType.indexOf(":") + 1);
                }
                NamespaceContext namespaceContext = xmlStreamReader.getNamespaceContext();
                String attributeNameSpace = namespaceContext.getNamespaceURI(attributeTypePrefix);

                if (attributeNameSpace.equals(Constants.XSD_NAMESPACE)) {
                    xmlStreamReader.next();
                    if ("base64Binary".equals(attributeType)) {
                        returnObject = getDataHandlerObject(xmlStreamReader);
                    } else {
                        String attribValue = xmlStreamReader.getText();
                        if (attribValue != null) {
                            if (attributeType.equals("string")) {
                                returnObject = attribValue;
                            } else if (attributeType.equals("int")) {
                                returnObject = new Integer(attribValue);
                            } else if (attributeType.equals("QName")) {
                                String namespacePrefix = null;
                                String localPart = null;
                                if (attribValue.indexOf(":") > -1) {
                                    namespacePrefix = attribValue.substring(0, attribValue.indexOf(":"));
                                    localPart = attribValue.substring(attribValue.indexOf(":") + 1);
                                    returnObject = new QName(namespaceContext.getNamespaceURI(namespacePrefix), localPart);
                                }
                            } else if ("boolean".equals(attributeType)) {
                                returnObject = new Boolean(attribValue);
                            } else if ("anyURI".equals(attributeType)) {
                                try {
                                    returnObject = new URI(attribValue);
                                } catch (URI.MalformedURIException e) {
                                    throw new XMLStreamException("Invalid URI");
                                }
                            } else if ("date".equals(attributeType)) {
                                returnObject = ConverterUtil.convertToDate(attribValue);
                            } else if ("dateTime".equals(attributeType)) {
                                returnObject = ConverterUtil.convertToDateTime(attribValue);
                            } else if ("time".equals(attributeType)) {
                                returnObject = ConverterUtil.convertToTime(attribValue);
                            } else if ("byte".equals(attributeType)) {
                                returnObject = new Byte(attribValue);
                            } else if ("short".equals(attributeType)) {
                                returnObject = new Short(attribValue);
                            } else if ("float".equals(attributeType)) {
                                returnObject = new Float(attribValue);
                            } else if ("long".equals(attributeType)) {
                                returnObject = new Long(attribValue);
                            } else if ("double".equals(attributeType)) {
                                returnObject = new Double(attribValue);
                            } else if ("decimal".equals(attributeType)) {
                                returnObject = new BigDecimal(attribValue);
                            } else if ("unsignedLong".equals(attributeType)) {
                                returnObject = new UnsignedLong(attribValue);
                            } else if ("unsignedInt".equals(attributeType)) {
                                returnObject = new UnsignedInt(attribValue);
                            } else if ("unsignedShort".equals(attributeType)) {
                                returnObject = new UnsignedShort(attribValue);
                            } else if ("unsignedByte".equals(attributeType)) {
                                returnObject = new UnsignedByte(attribValue);
                            } else if ("positiveInteger".equals(attributeType)) {
                                returnObject = new PositiveInteger(attribValue);
                            } else if ("negativeInteger".equals(attributeType)) {
                                returnObject = new NegativeInteger(attribValue);
                            } else if ("nonNegativeInteger".equals(attributeType)) {
                                returnObject = new NonNegativeInteger(attribValue);
                            } else if ("nonPositiveInteger".equals(attributeType)) {
                                returnObject = new NonPositiveInteger(attribValue);
                            } else {
                                throw new ADBException("Unknown type ==> " + attributeType);
                            }
                        } else {
                            throw new ADBException("Attribute value is null");
                        }
                    }
                } else {
                    try {
                        Method getObjectMethod = extensionMapperClass.getMethod("getTypeObject",
                                new Class[]{String.class, String.class, XMLStreamReader.class});
                        returnObject = getObjectMethod.invoke(null,
                                new Object[]{attributeNameSpace, attributeType, xmlStreamReader});
                    } catch (NoSuchMethodException e) {
                        throw new ADBException("Can not find the getTypeObject method in the " +
                                "extension mapper class ", e);
                    } catch (IllegalAccessException e) {
                        throw new ADBException("Can not access the getTypeObject method in the " +
                                "extension mapper class ", e);
                    } catch (InvocationTargetException e) {
                        throw new ADBException("Can not invoke the getTypeObject method in the " +
                                "extension mapper class ", e);
                    }

                }

            } else {
                throw new ADBException("Any type element type has not been given");
            }
        }
        return returnObject;
    }

    private static Object getDataHandlerObject(XMLStreamReader reader) throws XMLStreamException {
        Object dataHandler = null;
        if (Boolean.TRUE.equals(reader.getProperty(OMConstants.IS_DATA_HANDLERS_AWARE))
                && Boolean.TRUE.equals(reader.getProperty(OMConstants.IS_BINARY))) {
            dataHandler = reader.getProperty(org.apache.axiom.om.OMConstants.DATA_HANDLER);
        } else {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT &&
                    reader.getName().equals(new QName(MTOMConstants.XOP_NAMESPACE_URI, MTOMConstants.XOP_INCLUDE))) {
                String id = ElementHelper.getContentID(reader, "UTF-8");
                dataHandler = ((MTOMStAXSOAPModelBuilder) ((OMStAXWrapper) reader).getBuilder()).getDataHandler(id);
                reader.next();
            } else if (reader.hasText()) {
                String content = reader.getText();
                dataHandler = ConverterUtil.convertToBase64Binary(content);

            }
        }
        return dataHandler;
    }

    static {
        isCustomClassPresent = (System.getProperty(SYSTEM_PROPERTY_ADB_CONVERTERUTIL) != null);
        if (isCustomClassPresent){
            String className = System.getProperty(SYSTEM_PROPERTY_ADB_CONVERTERUTIL);
            try {
                customClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                log.error("Can not load the converter util class "
                        + className + " using default org.apache.axis2.databinding.utils.ConverterUtil class");
                isCustomClassPresent = false;
            }
        }
    }

}
