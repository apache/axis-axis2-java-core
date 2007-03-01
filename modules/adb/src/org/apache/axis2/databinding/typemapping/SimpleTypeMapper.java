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
*
*/

package org.apache.axis2.databinding.typemapping;

import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleTypeMapper {

    private static final String STRING = "java.lang.String";
    private static final String W_INT = "java.lang.Integer";
    private static final String W_DOUBLE = "java.lang.Double";
    private static final String W_LONG = "java.lang.Long";
    private static final String W_BYTE = "java.lang.Byte";
    private static final String W_SHORT = "java.lang.Short";
    private static final String W_BOOLEAN = "java.lang.Boolean";
    private static final String W_CHAR = "java.lang.Character";
    private static final String W_FLOAT = "java.lang.Float";
    private static final String W_CALENDAR = "java.util.Calendar";
    private static final String W_DATE = "java.util.Date";
    private static final String W_DATA_HANDLER = "javax.activation.DataHandler";
    private static final String INT = "int";
    private static final String BOOLEAN = "boolean";
    private static final String BYTE = "byte";
    private static final String DOUBLE = "double";
    private static final String SHORT = "short";
    private static final String LONG = "long";
    private static final String FLOAT = "float";
    private static final String CHAR = "char";

    public static Object getSimpleTypeObject(Class parameter, OMElement value) {
        String name = parameter.getName();
        String text = value.getText();

        if (text == null || text.length() == 0) {
            return null;
        } else if (name.equals(STRING)) {
            return text;
        } else if (name.equals(INT)) {
            return new Integer(text);
        } else if (name.equals(BOOLEAN)) {
            return Boolean.valueOf(text);
        } else if (name.equals(BYTE)) {
            return new Byte(text);
        } else if (name.equals(DOUBLE)) {
            return new Double(text);
        } else if (name.equals(SHORT)) {
            return new Short(text);
        } else if (name.equals(LONG)) {
            return new Long(text);
        } else if (name.equals(FLOAT)) {
            return new Float(text);
        } else if (name.equals(CHAR)) {
            return new Character(text.toCharArray()[0]);
        } else if (name.equals(W_INT)) {
            return new Integer(text);
        } else if (name.equals(W_BOOLEAN)) {
            return Boolean.valueOf(text);
        } else if (name.equals(W_BYTE)) {
            return new Byte(text);
        } else if (name.equals(W_DOUBLE)) {
            return new Double(text);
        } else if (name.equals(W_SHORT)) {
            return new Short(text);
        } else if (name.equals(W_LONG)) {
            return new Long(text);
        } else if (name.equals(W_FLOAT)) {
            return new Float(text);
        } else if (name.equals(W_CHAR)) {
            return new Character(text.toCharArray()[0]);
        } else if (name.equals(W_CALENDAR)) {
            return makeCalendar(text, false);
        } else if (name.equals(W_DATE)) {
            return makeCalendar(text, true);
        } else {
            return null;
        }
    }

    public static ArrayList getArrayList(OMElement element, String localName) {
        Iterator childitr = element.getChildrenWithName(new QName(localName));
        ArrayList list = new ArrayList();
        while (childitr.hasNext()) {
            Object o = childitr.next();
            list.add(o);
        }
        return list;
    }

    public static ArrayList getArrayList(OMElement element) {
        Iterator childitr = element.getChildren();
        ArrayList list = new ArrayList();
        while (childitr.hasNext()) {
            Object o = childitr.next();
            list.add(o);
        }
        return list;
    }

    public static boolean isSimpleType(Object obj) {
        String objClassName = obj.getClass().getName();
        if (obj instanceof Calendar) {
            return true;
        } else if (obj instanceof Date) {
            return true;
        } else {
            return isSimpleType(objClassName);
        }
    }

    public static boolean isSimpleType(Class obj) {
        String objClassName = obj.getName();
        return isSimpleType(objClassName);
    }

    public static boolean isCollection(Class obj) {
        return java.util.Collection.class.isAssignableFrom(obj);
    }
    public static boolean isSimpleType(String objClassName) {
        if (objClassName.equals(STRING)) {
            return true;
        } else if (objClassName.equals(INT)) {
            return true;
        } else if (objClassName.equals(BOOLEAN)) {
            return true;
        } else if (objClassName.equals(BYTE)) {
            return true;
        } else if (objClassName.equals(DOUBLE)) {
            return true;
        } else if (objClassName.equals(SHORT)) {
            return true;
        } else if (objClassName.equals(LONG)) {
            return true;
        } else if (objClassName.equals(FLOAT)) {
            return true;
        } else if (objClassName.equals(CHAR)) {
            return true;
        } else if (objClassName.equals(W_INT)) {
            return true;
        } else if (objClassName.equals(W_BOOLEAN)) {
            return true;
        } else if (objClassName.equals(W_BYTE)) {
            return true;
        } else if (objClassName.equals(W_DOUBLE)) {
            return true;
        } else if (objClassName.equals(W_SHORT)) {
            return true;
        } else if (objClassName.equals(W_LONG)) {
            return true;
        } else if (objClassName.equals(W_FLOAT)) {
            return true;
        } else if (objClassName.equals(W_CALENDAR)) {
            return true;
        } else if (objClassName.equals(W_DATE)) {
            return true;
        } else {
            return objClassName.equals(W_CHAR);
        }
    }

    public static String getStringValue(Object obj) {
        if (obj instanceof Float ||
                obj instanceof Double) {
            double data;
            if (obj instanceof Float) {
                data = ((Float) obj).doubleValue();
            } else {
                data = ((Double) obj).doubleValue();
            }
            if (Double.isNaN(data)) {
                return "NaN";
            } else if (data == Double.POSITIVE_INFINITY) {
                return "INF";
            } else if (data == Double.NEGATIVE_INFINITY) {
                return "-INF";
            } else {
                return obj.toString();
            }
        } else if (obj instanceof Calendar) {
            SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
            return zulu.format(((Calendar) obj).getTime());
        } else if (obj instanceof Date) {
            SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
            return zulu.format(obj);
        }
        return obj.toString();
    }

    public static Object makeCalendar(String source, boolean returnDate) {
        Calendar calendar = Calendar.getInstance();
        Date date;
        boolean bc = false;

        // validate fixed portion of format
        if (source == null || source.length() == 0) {
            throw new NumberFormatException("Calendar cannot be null");
        }
        if (source.charAt(0) == '+') {
            source = source.substring(1);
        }
        if (source.charAt(0) == '-') {
            source = source.substring(1);
            bc = true;
        }
        if (source.length() < 19) {
            throw new NumberFormatException("Calendar string too short");
        }
        if (source.charAt(4) != '-' || source.charAt(7) != '-' ||
                source.charAt(10) != 'T') {
            throw new NumberFormatException("Invalid date format");
        }
        if (source.charAt(13) != ':' || source.charAt(16) != ':') {
            throw new NumberFormatException("Invalid time format");
        }
        // convert what we have validated so far
        try {
                SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
                date = zulu.parse(source.substring(0, 19) + ".000Z");
        } catch (Exception e) {
            throw new NumberFormatException(e.toString());
        }
        int pos = 19;

        // parse optional milliseconds
        if (pos < source.length() && source.charAt(pos) == '.') {
            int milliseconds;
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
                throw new NumberFormatException("Invalid timezone");
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
            throw new NumberFormatException("Unknown chars after calendar string");
        }
        calendar.setTime(date);

        // support dates before the Christian era
        if (bc) {
            calendar.set(Calendar.ERA, GregorianCalendar.BC);
        }

        if (returnDate) {
            return date;
        } else {
            return calendar;
        }
    }
}
