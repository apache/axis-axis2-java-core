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

package org.apache.axis2.databinding.deserializers;

import org.apache.axis2.databinding.DeserializationTarget;
import org.apache.axis2.databinding.DeserializationContext;
import org.apache.axis2.databinding.Deserializer;
import org.apache.axis2.databinding.metadata.TypeDesc;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

/**
 * SimpleDeserializer
 */
public class SimpleDeserializer implements Deserializer {
    private static SimpleDateFormat zulu =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    //  0123456789 0 123456789

    static {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static final Class[] STRING_STRING_CLASS =
        new Class [] {String.class, String.class};

    public static final Class[] STRING_CLASS =
        new Class [] {String.class};

    Class javaType;
    QName xmlType;
    Constructor constructor;
    XMLStreamReader reader;
    TypeDesc typeDesc;
    DeserializationTarget target;

    public SimpleDeserializer(Class javaType, QName xmlType) {
        this.javaType = javaType;
        this.xmlType = xmlType;
    }

    public void setTypeDesc(TypeDesc typeDesc) {
        this.typeDesc = typeDesc;
    }

    public void setTarget(DeserializationTarget target) {
        this.target = target;
    }

    public void deserialize(XMLStreamReader reader,
                            DeserializationContext context) throws Exception {
        this.reader = reader;
        // We're expecting text and then an end element.
        int eventType;
        String text = new String();
        while ((eventType = reader.next()) != XMLStreamConstants.END_DOCUMENT) {
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                int numAttrs = reader.getAttributeCount();
                eventType = reader.next();
                System.out.println("There are " + numAttrs + " attr(s)");
            }
            if (eventType == XMLStreamConstants.CHARACTERS) {
                text = text.concat(reader.getText());
            }
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                // End of element
                target.setValue(makeValue(text));
                return;
            }
        }
        throw new Exception("End of document reached prematurely!");
    }

    /**
     * Convert the string that has been accumulated into an Object.  Subclasses
     * may override this.  Note that if the javaType is a primitive, the returned
     * object is a wrapper class.
     * @param source the serialized value to be deserialized
     * @throws Exception any exception thrown by this method will be wrapped
     */
    public Object makeValue(String source) throws Exception
    {
        if (javaType == java.lang.String.class) {
            return source;
        }

        // Trim whitespace if non-String
        source = source.trim();

        if (source.length() == 0) {
            return null;
        }

        // if constructor is set skip all basic java type checks
        if (this.constructor == null) {
            Object value = makeBasicValue(source);
            if (value != null) {
                return value;
            }
        }

        Object [] args = null;

        boolean isQNameSubclass = QName.class.isAssignableFrom(javaType);

        if (isQNameSubclass) {
            int colon = source.lastIndexOf(":");
            String namespace = colon < 0 ? "" :
                reader.getNamespaceURI(source.substring(0, colon));
            String localPart = colon < 0 ? source : source.substring(colon + 1);
            args = new Object [] {namespace, localPart};
        }

        if (constructor == null) {
            try {
                if (isQNameSubclass) {
                    constructor =
                        javaType.getDeclaredConstructor(STRING_STRING_CLASS);
                } else {
                    constructor =
                        javaType.getDeclaredConstructor(STRING_CLASS);
                }
            } catch (Exception e) {
                return null;
            }
        }

        if(constructor.getParameterTypes().length==0){
            try {
                Object obj = constructor.newInstance(new Object[]{});
                obj.getClass().getMethod("set_value", new Class[]{String.class})
                        .invoke(obj, new Object[]{source});
                return obj;
            } catch (Exception e){
                //Ignore exception
            }
        }
        if (args == null) {
            args = new Object[]{source};
        }
        return constructor.newInstance(args);
    }

    private Object makeBasicValue(String source) throws Exception {
        // If the javaType is a boolean, except a number of different sources
        if (javaType == boolean.class ||
            javaType == Boolean.class) {
            // This is a pretty lame test, but it is what the previous code did.
            switch (source.charAt(0)) {
                case '0': case 'f': case 'F':
                    return Boolean.FALSE;

                case '1': case 't': case 'T':
                    return Boolean.TRUE;

                default:
                    throw new NumberFormatException("Bad boolean expression");
                }

        }

        // If expecting a Float or a Double, need to accept some special cases.
        if (javaType == float.class ||
            javaType == java.lang.Float.class) {
            if (source.equals("NaN")) {
                return new Float(Float.NaN);
            } else if (source.equals("INF")) {
                return new Float(Float.POSITIVE_INFINITY);
            } else if (source.equals("-INF")) {
                return new Float(Float.NEGATIVE_INFINITY);
            } else {
                return new Float(source);
            }
        }

        if (javaType == double.class ||
            javaType == java.lang.Double.class) {
            if (source.equals("NaN")) {
                return new Double(Double.NaN);
            } else if (source.equals("INF")) {
                return new Double(Double.POSITIVE_INFINITY);
            } else if (source.equals("-INF")) {
                return new Double(Double.NEGATIVE_INFINITY);
            } else {
                return new Double(source);
            }
        }

        if (javaType == int.class ||
            javaType == java.lang.Integer.class) {
            return new Integer(source);
        }

        if (javaType == short.class ||
            javaType == java.lang.Short.class) {
            return new Short(source);
        }

        if (javaType == long.class ||
            javaType == java.lang.Long.class) {
            return new Long(source);
        }

        if (javaType == byte.class ||
            javaType == java.lang.Byte.class) {
            return new Byte(source);
        }

/*
        if (javaType == org.apache.axis.types.URI.class) {
            return new org.apache.axis.types.URI(source);
        }
*/

        if (javaType == Calendar.class) {
            return makeCalendar(source, false);
        }

        return null;
    }

    public static Object makeCalendar(String source, boolean returnDate) {
        Calendar calendar = Calendar.getInstance();
        Date date;
        boolean bc = false;

        // validate fixed portion of format
        if (source == null || source.length() == 0) {
            throw new NumberFormatException(
                    Messages.getMessage("badDateTime00"));
        }
        if (source.charAt(0) == '+') {
            source = source.substring(1);
        }
        if (source.charAt(0) == '-') {
            source = source.substring(1);
            bc = true;
        }
        if (source.length() < 19) {
            throw new NumberFormatException(
                    Messages.getMessage("badDateTime00"));
        }
        if (source.charAt(4) != '-' || source.charAt(7) != '-' ||
                source.charAt(10) != 'T') {
            throw new NumberFormatException(Messages.getMessage("badDate00"));
        }
        if (source.charAt(13) != ':' || source.charAt(16) != ':') {
            throw new NumberFormatException(Messages.getMessage("badTime00"));
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
                throw new NumberFormatException(
                        Messages.getMessage("badTimezone00"));
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
            throw new NumberFormatException(Messages.getMessage("badChars00"));
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
