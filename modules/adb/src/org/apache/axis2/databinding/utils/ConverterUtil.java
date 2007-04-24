package org.apache.axis2.databinding.utils;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.i18n.ADBMessages;
import org.apache.axis2.databinding.types.*;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
 * Converter methods to go from 1. simple type -> String 2. simple type -> Object 3. String ->
 * simpletype 4. Object list -> array
 */
public class ConverterUtil {
    private static final String POSITIVE_INFINITY = "INF";
    private static final String NEGATIVE_INFINITY = "-INF";

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
        // lexical form of the date is '-'? yyyy '-' mm '-' dd zzzzzz?
        // we have to serialize it with the GMT timezone
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(value);
    }

    public static String convertToString(Calendar value) {
        // lexical form of the calendar is '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
        SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Sun JDK bug http://developer.java.sun.com/developer/bugParade/bugs/4229798.html
        return zulu.format(value.getTime());
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
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return Integer.parseInt(s);
    }

    public static BigDecimal convertToBigDecimal(String s) {
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new BigDecimal(s);
    }

    public static double convertToDouble(String s) {
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
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return new BigDecimal(s);
    }

    public static float convertToFloat(String s) {
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
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return Long.parseLong(s);
    }

    public static short convertToShort(String s) {
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
        return new YearMonth(s);
    }

    public static MonthDay convertToGMonthDay(String s) {
        return new MonthDay(s);
    }

    public static Year convertToGYear(String s) {
        return new Year(s);
    }

    public static Month convertToGMonth(String s) {
        return new Month(s);
    }

    public static Day convertToGDay(String s) {
        return new Day(s);
    }

    public static Duration convertToDuration(String s) {
        return new Duration(s);
    }


    public static HexBinary convertToHexBinary(String s) {
        return new HexBinary(s);
    }

    public static javax.activation.DataHandler convertToBase64Binary(String s) {
        // reusing the byteArrayDataSource from the Axiom classes
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
        return new Time(s);
    }

    public static Token convertToToken(String s) {
        return new Token(s);
    }


    public static NormalizedString convertToNormalizedString(String s) {
        return new NormalizedString(s);
    }

    public static UnsignedLong convertToUnsignedLong(String s) {
        return new UnsignedLong(s);
    }

    public static UnsignedInt convertToUnsignedInt(String s) {
        return new UnsignedInt(s);
    }

    public static UnsignedShort convertToUnsignedShort(String s) {
        return new UnsignedShort(s);
    }

    public static UnsignedByte convertToUnsignedByte(String s) {
        return new UnsignedByte(s);
    }

    public static NonNegativeInteger convertToNonNegativeInteger(String s) {
        return new NonNegativeInteger(s);
    }

    public static NegativeInteger convertToNegativeInteger(String s) {
        return new NegativeInteger(s);
    }

    public static PositiveInteger convertToPositiveInteger(String s) {
        return new PositiveInteger(s);
    }

    public static NonPositiveInteger convertToNonPositiveInteger(String s) {
        return new NonPositiveInteger(s);
    }

    public static Name convertToName(String s) {
        return new Name(s);
    }

    public static NCName convertToNCName(String s) {
        return new NCName(s);
    }

    public static Id convertToID(String s) {
        return new Id(s);
    }

    public static Id convertToId(String s) {
        return convertToID(s);
    }

    public static Language convertToLanguage(String s) {
        return new Language(s);
    }

    public static NMToken convertToNMTOKEN(String s) {
        return new NMToken(s);
    }

    public static NMTokens convertToNMTOKENS(String s) {
        return new NMTokens(s);
    }

    public static Notation convertToNOTATION(String s) {
        return null; //todo Need to fix this
        // return new Notation(s);
    }

    public static Entity convertToENTITY(String s) {
        return new Entity(s);
    }

    public static Entities convertToENTITIES(String s) {
        return new Entities(s);
    }

    public static IDRef convertToIDREF(String s) {
        return new IDRef(s);
    }

    public static IDRefs convertToIDREFS(String s) {
        return new IDRefs(s);
    }

    public static URI convertToAnyURI(String s) {
        try {
            return new URI(s);
        } catch (URI.MalformedURIException e) {
            throw new ObjectConversionException(
                    ADBMessages.getMessage("converter.cannotParse", s), e);
        }
    }

    public static BigInteger convertToInteger(String s) {
        if (s.startsWith("+")) {
            s = s.substring(1);
        }

        return new BigInteger(s);
    }

    public static BigInteger convertToBigInteger(String s) {
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return convertToInteger(s);
    }

    public static byte convertToByte(String s) {
        return Byte.parseByte(s);
    }

    /**
     * Code from Axis1 code base Note - We only follow the convention in the latest schema spec
     *
     * @param source
     * @return Returns Calendar.
     */
    public static Calendar convertToDateTime(String source) {

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
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        } else if ((rest.lastIndexOf("+") > 0) || (rest.lastIndexOf("-") > 0)) {
                            // this is given in a general time zione
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
                            if (rest.lastIndexOf("+") > 0) {
                                source = source.substring(0, source.lastIndexOf("+")) + "GMT" +
                                        rest.substring(rest.lastIndexOf("+"));
                            } else if (rest.lastIndexOf("-") > 0) {
                                source = source.substring(0, source.lastIndexOf("-")) + "GMT" +
                                        rest.substring(rest.lastIndexOf("-"));
                            }

                        } else {
                            // i.e it does not have time zone
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        }

                    } else {
                        if (rest.startsWith("Z")) {
                            // this is in gmt time zone
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        } else if (rest.startsWith("+") || rest.startsWith("-")) {
                            // this is given in a general time zione
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
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
        Object returnArray = Array.newInstance(baseArrayClass, listSize);
        if (int.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setInt(returnArray, i, Integer.parseInt(o.toString()));
                }
            }
        } else if (float.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setFloat(returnArray, i, Float.parseFloat(o.toString()));
                }
            }
        } else if (short.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setShort(returnArray, i, Short.parseShort(o.toString()));
                }
            }
        } else if (byte.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setByte(returnArray, i, Byte.parseByte(o.toString()));
                }
            }
        } else if (long.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setLong(returnArray, i, Long.parseLong(o.toString()));
                }
            }
        } else if (boolean.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setBoolean(returnArray, i, Boolean.getBoolean(o.toString()));
                }
            }
        } else if (char.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setChar(returnArray, i, o.toString().toCharArray()[0]);
                }
            }
        } else if (double.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.setDouble(returnArray, i, Double.parseDouble(o.toString()));
                }
            }
        } else if (Calendar.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Object o = objectList.get(i);
                if (o != null) {
                    Array.set(returnArray, i, convertToDateTime(o.toString()));
                }
            }
        } else {
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
                        Array.set(returnArray, i, getObjectForClass(
                                baseArrayClass,
                                null));
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
            objectList.toArray((Object[]) returnArray);
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
        boolean continueFlag = false;
        try {
            Constructor stringConstructor = clazz.getConstructor(new Class[]{String.class});
            return stringConstructor.newInstance(new Object[]{value});
        } catch (NoSuchMethodException e) {
            //oops - no such constructors - continue with the
            //parse method
            continueFlag = true;
        } catch (Exception e) {
            throw new ObjectConversionException(
                    ADBMessages.getMessage("converter.cannotGenerate",
                            clazz.getName()),
                    e);
        }

        if (!continueFlag) {
            throw new ObjectConversionException(
                    ADBMessages.getMessage("converter.cannotConvert",
                            clazz.getName()));
        }

        try {
            Method parseMethod = clazz.getMethod("parse", new Class[]{String.class});
            Object instance = clazz.newInstance();
            return parseMethod.invoke(instance, new Object[]{value});
        } catch (NoSuchMethodException e) {
            throw new ObjectConversionException(e);
        } catch (Exception e) {
            throw new ObjectConversionException(
                    ADBMessages.getMessage("converter.cannotGenerate",
                            clazz.getName()),
                    e);
        }

    }

    /**
     * A simple exception that is thrown when the conversion fails
     */
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
}
