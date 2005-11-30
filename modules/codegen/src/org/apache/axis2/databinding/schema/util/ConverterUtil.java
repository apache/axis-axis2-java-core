package org.apache.axis2.databinding.schema.util;

import org.apache.axis2.databinding.schema.SchemaConstants;
import org.apache.axis2.databinding.schema.types.*;

import java.util.*;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.math.BigInteger;

import sun.misc.BASE64Decoder;

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
 * Converter methods to go from
 * 1. simple type -> String
 * 2. simple type -> Object
 * 3. String -> simpletype
 * 4. Object list -> array
 */
public class ConverterUtil {

    /* String conversion methods */
    public static String convertToString(int i) {
        return i + "";
    }

    public static String convertToString(float i) {
        return i + "";
    }

    public static String convertToString(long i) {
        return i + "";
    }

    public static String convertToString(double i) {
        return i + "";
    }

    public static String convertToString(byte i) {
        return new String(new byte[]{i});
    }

    public static String convertToString(char i) {
        return new String(new char[]{i});
    }

    public static String convertToString(short i) {
        return i + "";
    }

    public static String convertToString(boolean i) {
        return i + "";
    }

    public static String convertToString(Object o) {
        return o.toString();
    }

    /* String to java type conversions
       These methods have a special signature structure
       <code>convertTo</code> followed by the schema type name
       Say for int, convertToint(String) is the converter method

       Not very elegant but it seems to be the only way!

    */


    public static int convertToint(String s) {
        return Integer.parseInt(s);
    }

    public static double convertTodouble(String s) {
        return Double.parseDouble(s);
    }

    public static BigDecimal convertTodecimal(String s) {
        return new BigDecimal(s);
    }

    public static float convertTofloat(String s) {
        return Float.parseFloat(s);
    }

    public static String convertTostring(String s) {
        return s;
    }

    public static long convertTolong(String s) {
        return Long.parseLong(s);
    }

    public static short convertToshort(String s) {
        return Short.parseShort(s);
    }

    public static boolean convertToboolean(String s) {
        return Boolean.getBoolean(s);
    }

    public static Object convertToanyType(String s) {
        return s; //todo -> What to do here?
    }

    public static YearMonth convertTogYearMonth(String s) {
        return new YearMonth(s);
    }

    public static MonthDay convertTogMonthDay(String s) {
        return new MonthDay(s);
    }

    public static Year convertTogYear(String s) {
        return new Year(s);
    }

    public static Month convertTogMonth(String s) {
        return new Month(s);
    }

    public static Day convertTogDay(String s) {
        return new Day(s);
    }

    public static Duration convertToduration(String s) {
        return new Duration(s);
    }

    public static HexBinary convertTohexBinary(String s) {
        return new HexBinary(s);
    }

    public static byte[] convertTobase64Binary(String s) throws Exception{
        //using the Sun's base64 decoder that should come with the JRE
        return new BASE64Decoder().decodeBuffer(s);
    }

    /**
     * Convert a given string into a date
     * Code from Axis1 DateDeserializer
     *
     * @param s
     * @return
     */
    public static Date convertTodate(String source) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd");
        //  0123456789 0 123456789
        Date result;
        boolean bc = false;

        // validate fixed portion of format
        if (source != null) {
            if (source.charAt(0) == '+')
                source = source.substring(1);

            if (source.charAt(0) == '-') {
                source = source.substring(1);
                bc = true;
            }

            if (source.length() < 10)
                throw new NumberFormatException("bad date format");


            if (source.charAt(4) != '-' || source.charAt(7) != '-')
                throw new NumberFormatException("bad Date format");

        }

        synchronized (calendar) {
            // convert what we have validated so far
            try {
                result = zulu.parse(source == null ? null :
                        (source.substring(0, 10)));
            } catch (Exception e) {
                throw new NumberFormatException(e.toString());
            }

            // support dates before the Christian era
            if (bc) {
                calendar.setTime(result);
                calendar.set(Calendar.ERA, GregorianCalendar.BC);
                result = calendar.getTime();
            }
        }
        return result;
    }

    public static Time convertTotime(String s) {
        return new Time(s);
    }

    public static Token convertTotoken(String s) {
        return new Token(s);
    }

    public static NormalizedString convertTonormalizedString(String s) {
        return new NormalizedString(s);
    }

    public static UnsignedLong convertTounsignedLong(String s) {
        return new UnsignedLong(s);
    }

    public static UnsignedInt convertTounsignedInt(String s) {
        return new UnsignedInt(s);
    }

    public static UnsignedShort convertTounsignedShort(String s) {
        return new UnsignedShort(s);
    }

    public static UnsignedByte convertTounsignedByte(String s) {
        return new UnsignedByte(s);
    }

    public static NonNegativeInteger convertTononNegativeInteger(String s) {
        return new NonNegativeInteger(s);
    }

    public static NegativeInteger convertTonegativeInteger(String s) {
        return new NegativeInteger(s);
    }

    public static PositiveInteger convertTopositiveInteger(String s) {
        return new PositiveInteger(s);
    }

    public static NonPositiveInteger convertTononPositiveInteger(String s) {
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

    public static Language convertTolanguage(String s) {
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

    public static URI convertToanyURI(String s) throws Exception {
        return new URI(s);
    }

    public static BigInteger convertTointeger(String s) throws Exception {
        return new BigInteger(s);
    }

    public static byte convertTobyte(String s) throws Exception {
        return Byte.parseByte(s);
    }

    /**
     * Code from Axis1 code base
     * Note - We only follow the convention in the latest schema spec
     * @param s
     * @return
     * @throws Exception
     */
    public static Calendar convertTodateTime(String source) throws Exception {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat zulu =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date;
        boolean bc = false;

        // validate fixed portion of format
        if (source == null || source.length() == 0) {
            throw new NumberFormatException();
//                    Messages.getMessage("badDateTime00"));
        }
        if (source.charAt(0) == '+') {
            source = source.substring(1);
        }
        if (source.charAt(0) == '-') {
            source = source.substring(1);
            bc = true;
        }
        if (source.length() < 19) {
            throw new NumberFormatException();
//                    Messages.getMessage("badDateTime00"));
        }
        if (source.charAt(4) != '-' || source.charAt(7) != '-' ||
                source.charAt(10) != 'T') {
            throw new NumberFormatException();//Messages.getMessage("badDate00"));
        }
        if (source.charAt(13) != ':' || source.charAt(16) != ':') {
            throw new NumberFormatException();//Messages.getMessage("badTime00"));
        }
        // convert what we have validated so far
        try {
            synchronized (zulu) {
                date = zulu.parse(source.substring(0, 19) + ".000Z");
            }
        } catch (Exception e) {
            throw new NumberFormatException(e.toString());
        }
        int pos = 19;

        // parse optional milliseconds
        if (pos < source.length() && source.charAt(pos) == '.') {
            int milliseconds = 0;
            int start = ++pos;
            while (pos < source.length() &&
                    Character.isDigit(source.charAt(pos))) {
                pos++;
            }
            String decimal = source.substring(start, pos);
            if (decimal.length() == 3) {
                milliseconds = Integer.parseInt(decimal);
            } else if (decimal.length() < 3) {
                milliseconds = Integer.parseInt((decimal + "000")
                        .substring(0, 3));
            } else {
                milliseconds = Integer.parseInt(decimal.substring(0, 3));
                if (decimal.charAt(3) >= '5') {
                    ++milliseconds;
                }
            }

            // add milliseconds to the current date
            date.setTime(date.getTime() + milliseconds);
        }

        // parse optional timezone
        if (pos + 5 < source.length() &&
                (source.charAt(pos) == '+' || (source.charAt(pos) == '-'))) {
            if (!Character.isDigit(source.charAt(pos + 1)) ||
                    !Character.isDigit(source.charAt(pos + 2)) ||
                    source.charAt(pos + 3) != ':' ||
                    !Character.isDigit(source.charAt(pos + 4)) ||
                    !Character.isDigit(source.charAt(pos + 5))) {
                throw new NumberFormatException();
                // Messages.getMessage("badTimezone00"));
            }
            int hours = (source.charAt(pos + 1) - '0') * 10
                    + source.charAt(pos + 2) - '0';
            int mins = (source.charAt(pos + 4) - '0') * 10
                    + source.charAt(pos + 5) - '0';
            int milliseconds = (hours * 60 + mins) * 60 * 1000;

            // subtract milliseconds from current date to obtain GMT
            if (source.charAt(pos) == '+') {
                milliseconds = -milliseconds;
            }
            date.setTime(date.getTime() + milliseconds);
            pos += 6;
        }
        if (pos < source.length() && source.charAt(pos) == 'Z') {
            pos++;
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        if (pos < source.length()) {
            throw new NumberFormatException();//Messages.getMessage("badChars00"));
        }
        calendar.setTime(date);

        // support dates before the Christian era
        if (bc) {
            calendar.set(Calendar.ERA, GregorianCalendar.BC);
        }
        return calendar;

    }

    /**
     * Code from Axis1 code base
     * @param source
     * @return
     */
    public static QName convertToQName(String source) {
        source = source.trim();
        int colon = source.lastIndexOf(":");
        String namespace = colon < 0 ? "" : "" ;// todo Fix this. Need to take a namespace with this
        //context.getNamespaceURI(source.substring(0, colon));
        String localPart = colon < 0 ? source : source.substring(colon + 1);
        return new QName(namespace, localPart);
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

    /**
     * @param baseArrayClass
     * @param objectList     -> for primitive type array conversion we assume the content to be
     *                       strings!
     * @return
     */
    public static Object convertToArray(Class baseArrayClass, List objectList) {
        int listSize = objectList.size();
        Object returnArray = Array.newInstance(baseArrayClass, listSize);
        for (int i = 0; i < listSize; i++) {
            if (int.class.equals(baseArrayClass)) {
                Array.setInt(returnArray, i, Integer.parseInt(objectList.get(i).toString()));
            } else if (float.class.equals(baseArrayClass)) {
                Array.setFloat(returnArray, i, Float.parseFloat(objectList.get(i).toString()));
            } else if (short.class.equals(baseArrayClass)) {
                Array.setShort(returnArray, i, Short.parseShort(objectList.get(i).toString()));
            } else if (long.class.equals(baseArrayClass)) {
                Array.setLong(returnArray, i, Long.parseLong(objectList.get(i).toString()));
            } else if (boolean.class.equals(baseArrayClass)) {
                Array.setBoolean(returnArray, i, Boolean.getBoolean(objectList.get(i).toString()));
            } else if (char.class.equals(baseArrayClass)) {
                Array.setChar(returnArray, i, objectList.get(i).toString().toCharArray()[0]);
            } else if (double.class.equals(baseArrayClass)) {
                Array.setDouble(returnArray, i, Double.parseDouble(objectList.get(i).toString()));
            } else {
                Array.set(returnArray, i, objectList.get(i));
            }
        }
        return returnArray;
    }

}
