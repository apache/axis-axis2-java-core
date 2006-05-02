package org.apache.axis2.databinding.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.attachments.ByteArrayDataSource;
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
import org.apache.axis2.util.Base64;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
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
 * Converter methods to go from
 * 1. simple type -> String
 * 2. simple type -> Object
 * 3. String -> simpletype
 * 4. Object list -> array
 */
public class ConverterUtil {
    private static final String POSITIVE_INFINITY = "INF";
    private static final String NEGATIVE_INFINITY = "-INF";

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
        return ((int)i)+"";
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

    public static String convertToString(Date value) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer buf = new StringBuffer();
        synchronized (calendar) {
            if (calendar.get(Calendar.ERA) == GregorianCalendar.BC) {
                buf.append("-");
                calendar.setTime((Date)value);
                calendar.set(Calendar.ERA, GregorianCalendar.AD);
                value = calendar.getTime();
            }
            buf.append(zulu.format(value));
        }
        return buf.toString();
    }

    public static String convertToString(Calendar value) {
        SimpleDateFormat zulu =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = value.getTime();

        // Serialize including convert to GMT
        synchronized (zulu) {
            // Sun JDK bug http://developer.java.sun.com/developer/bugParade/bugs/4229798.html
            return zulu.format(date);
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


    public static int convertToint(String s) {
        return Integer.parseInt(s);
    }

    public static double convertTodouble(String s) {
        if (POSITIVE_INFINITY.equals(s)){
            return Double.POSITIVE_INFINITY;
        }else if (NEGATIVE_INFINITY.equals(s)){
            return Double.NEGATIVE_INFINITY;
        }
        return Double.parseDouble(s);
    }

    public static BigDecimal convertTodecimal(String s) {
        return new BigDecimal(s);
    }

    public static float convertTofloat(String s) {
         if (POSITIVE_INFINITY.equals(s)){
            return Float.POSITIVE_INFINITY;
        }else if (NEGATIVE_INFINITY.equals(s)){
            return Float.NEGATIVE_INFINITY;
        }
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
        return Boolean.valueOf(s).booleanValue();
    }

    public static String convertToanySimpleType(String s) {
        return s;
    }

    public static OMElement convertToanyType(String s) {
        try {
            XMLStreamReader r = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(s.getBytes()));
            StAXOMBuilder builder = new StAXOMBuilder(OMAbstractFactory.getOMFactory(),r);
            return builder.getDocumentElement();
        } catch (XMLStreamException e) {
            return null;
        }
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

    public static Duration convertToDuration(String s) {
        return new Duration(s);
    }
    public static HexBinary convertTohexBinary(String s) {
        return new HexBinary(s);
    }

    public static javax.activation.DataHandler convertTobase64Binary(String s)
            throws Exception{
        // reusing the byteArrayDataSource from the Axiom classes
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(
                s.getBytes()
        );
        return new DataHandler(byteArrayDataSource);
    }

    /**
     * Converts a given string into a date.
     * Code from Axis1 DateDeserializer.
     *
     * @param source
     * @return Returns Date.
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
     * @param source
     * @return Returns Calendar.
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
     * @return Returns QName.
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
                Array.setInt(returnArray, i, Integer.parseInt(objectList.get(i).toString()));
            }
        } else if (float.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Array.setFloat(returnArray, i, Float.parseFloat(objectList.get(i).toString()));
            }
        } else if (short.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Array.setShort(returnArray, i, Short.parseShort(objectList.get(i).toString()));
            }
        } else if (long.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Array.setLong(returnArray, i, Long.parseLong(objectList.get(i).toString()));
            }
        } else if (boolean.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Array.setBoolean(returnArray, i, Boolean.getBoolean(objectList.get(i).toString()));
            }
        } else if (char.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Array.setChar(returnArray, i, objectList.get(i).toString().toCharArray()[0]);
            }
        } else if (double.class.equals(baseArrayClass)) {
            for (int i = 0; i < listSize; i++) {
                Array.setDouble(returnArray, i, Double.parseDouble(objectList.get(i).toString()));
            }
        } else {
            objectList.toArray((Object[])returnArray);
        }
        return returnArray;
    }

    /**
     * 
     * @param array
     * @return
     */
    public static List toList(Object[] array){
        if (array==null){
            return new ArrayList();
        }else{
            ArrayList list =  new ArrayList();
            for (int i = 0; i < array.length; i++) {
                list.add(array[i]);
            }
            return list;
        }
    }

    /**
     * Converts the given datahandler to a string
     * @return
     * @throws XMLStreamException
     */
    public static String getStringFromDatahandler(DataHandler dataHandler){
        try {
            InputStream inStream;
            inStream = dataHandler.getDataSource().getInputStream();
            byte[] data;
            StringBuffer text = new StringBuffer();
            do {
                data = new byte[1024];
                int len;
                while ((len = inStream.read(data)) > 0) {
                    byte[] temp = new byte[len];
                    System.arraycopy(data, 0, temp, 0, len);
                    text.append(Base64.encode(temp));
                }

            } while (inStream.available() > 0);

            return text.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
