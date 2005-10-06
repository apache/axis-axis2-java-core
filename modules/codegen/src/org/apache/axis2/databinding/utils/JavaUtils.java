/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.databinding.utils;


//import org.apache.axis.attachments.AttachmentPart;
//import org.apache.axis.attachments.OctetStream;
//import org.apache.axis.components.image.ImageIO;
//import org.apache.axis.components.image.ImageIOFactory;
//import org.apache.axis.components.logger.LogFactory;
//import org.apache.axis.types.HexBinary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import javax.activation.DataHandler;
//import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Utility class to deal with Java language related issues, such
 * as type conversions.
 *
 * @author Glen Daniels (gdaniels@apache.org)
 */
public class JavaUtils
{
    private JavaUtils() {
    }

    protected static Log log =
        LogFactory.getLog(JavaUtils.class.getName());
    
    public static final char NL = '\n';

    public static final char CR = '\r';

    /**
     * The prefered line separator
     */
    public static final String LS = System.getProperty("line.separator",
                                                       (new Character(NL)).toString());


    public static Class getWrapperClass(Class primitive)
    {
        if (primitive == int.class)
            return java.lang.Integer.class;
        else if (primitive == short.class)
            return java.lang.Short.class;
        else if (primitive == boolean.class)
            return java.lang.Boolean.class;
        else if (primitive == byte.class)
            return java.lang.Byte.class;
        else if (primitive == long.class)
            return java.lang.Long.class;
        else if (primitive == double.class)
            return java.lang.Double.class;
        else if (primitive == float.class)
            return java.lang.Float.class;
        else if (primitive == char.class)
            return java.lang.Character.class;
        
        return null;
    }
    
    public static String getWrapper(String primitive)
    {
        if (primitive.equals("int"))
            return "Integer";
        else if (primitive.equals("short"))
            return "Short";
        else if (primitive.equals("boolean"))
            return "Boolean";
        else if (primitive.equals("byte"))
            return "Byte";
        else if (primitive.equals("long"))
            return "Long";
        else if (primitive.equals("double"))
            return "Double";
        else if (primitive.equals("float"))
            return "Float";
        else if (primitive.equals("char"))
            return "Character";
        
        return null;
    }

    public static Class getPrimitiveClass(Class wrapper)
    {
        if (wrapper == java.lang.Integer.class)
            return int.class;
        else if (wrapper == java.lang.Short.class)
            return short.class;
        else if (wrapper == java.lang.Boolean.class)
            return boolean.class;
        else if (wrapper == java.lang.Byte.class)
            return byte.class;
        else if (wrapper == java.lang.Long.class)
            return long.class;
        else if (wrapper == java.lang.Double.class)
            return double.class;
        else if (wrapper == java.lang.Float.class)
            return float.class;
        else if (wrapper == java.lang.Character.class)
            return char.class;
        
        return null;
    }

    /*
         * Any builtin type that has a constructor that takes a String is a basic
         * type.
         * This is for optimization purposes, so that we don't introspect
         * primitive java types or some basic Axis types.
         */
    public static boolean isBasic(Class javaType) {
        return (javaType.isPrimitive() ||
                javaType == String.class ||
                javaType == Boolean.class ||
                javaType == Float.class ||
                javaType == Double.class ||
                Number.class.isAssignableFrom(javaType));// ||
//                javaType == org.apache.axis.types.Day.class ||
//                javaType == org.apache.axis.types.Duration.class ||
//                javaType == org.apache.axis.types.Entities.class ||
//                javaType == org.apache.axis.types.Entity.class ||
//                javaType == HexBinary.class ||
//                javaType == org.apache.axis.types.Id.class ||
//                javaType == org.apache.axis.types.IDRef.class ||
//                javaType == org.apache.axis.types.IDRefs.class ||
//                javaType == org.apache.axis.types.Language.class ||
//                javaType == org.apache.axis.types.Month.class ||
//                javaType == org.apache.axis.types.MonthDay.class ||
//                javaType == org.apache.axis.types.Name.class ||
//                javaType == org.apache.axis.types.NCName.class ||
//                javaType == org.apache.axis.types.NegativeInteger.class ||
//                javaType == org.apache.axis.types.NMToken.class ||
//                javaType == org.apache.axis.types.NMTokens.class ||
//                javaType == org.apache.axis.types.NonNegativeInteger.class ||
//                javaType == org.apache.axis.types.NonPositiveInteger.class ||
//                javaType == org.apache.axis.types.NormalizedString.class ||
//                javaType == org.apache.axis.types.PositiveInteger.class ||
//                javaType == org.apache.axis.types.Time.class ||
//                javaType == org.apache.axis.types.Token.class ||
//                javaType == org.apache.axis.types.UnsignedByte.class ||
//                javaType == org.apache.axis.types.UnsignedInt.class ||
//                javaType == org.apache.axis.types.UnsignedLong.class ||
//                javaType == org.apache.axis.types.UnsignedShort.class ||
//                javaType == org.apache.axis.types.URI.class ||
//                javaType == org.apache.axis.types.Year.class ||
//                javaType == org.apache.axis.types.YearMonth.class);
    }

    /**
     * It the argument to the convert(...) method implements
     * the ConvertCache interface, the convert(...) method
     * will use the set/get methods to store and retrieve
     * converted values.
     **/
    public interface ConvertCache {
        /**
         * Set/Get converted values of the convert method.
         **/
        public void setConvertedValue(Class cls, Object value);
        public Object getConvertedValue(Class cls);
        /**
         * Get the destination array class described by the xml
         **/
        public Class getDestClass();
    }

//    /** Utility function to convert an Object to some desired Class.
//     *
//     * Right now this works for:
//     *     arrays <-> Lists,
//     *     Holders <-> held values
//     * @param arg the array to convert
//     * @param destClass the actual class we want
//     */
//    public static Object convert(Object arg, Class destClass)
//    {
//        if (destClass == null) {
//            return arg;
//        }
//
//        Class argHeldType = null;
//        if (arg != null) {
//            argHeldType = getHolderValueType(arg.getClass());
//        }
//
//        if (arg != null && argHeldType == null && destClass.isAssignableFrom(arg.getClass())) {
//            return arg;
//        }
//
//        if (log.isDebugEnabled()) {
//            String clsName = "null";
//            if (arg != null) clsName = arg.getClass().getName();
////            log.debug( Messages.getMessage("convert00", clsName, destClass.getName()));
//        }
//
//        // See if a previously converted value is stored in the argument.
//        Object destValue = null;
//        if (arg instanceof ConvertCache) {
//            destValue = (( ConvertCache) arg).getConvertedValue(destClass);
//            if (destValue != null)
//                return destValue;
//        }
//
//        // Get the destination held type or the argument held type if they exist
//        Class destHeldType = getHolderValueType(destClass);
//
//        // Convert between Axis special purpose HexBinary and byte[]
//        if (arg instanceof HexBinary &&
//            destClass == byte[].class) {
//            return ((HexBinary) arg).getBytes();
//        } else if (arg instanceof byte[] &&
//                   destClass == HexBinary.class) {
//            return new HexBinary((byte[]) arg);
//        }
//
//        // Convert between Calendar and Date
//        if (arg instanceof Calendar && destClass == Date.class) {
//            return ((Calendar) arg).getTime();
//        }
//        if (arg instanceof Date && destClass == Calendar.class) {
//        	Calendar calendar = Calendar.getInstance();
//        	calendar.setTime((Date) arg);
//            return calendar;
//        }
//
//        // Convert between Calendar and java.sql.Date
//        if (arg instanceof Calendar && destClass == java.sql.Date.class) {
//            return new java.sql.Date(((Calendar) arg).getTime().getTime());
//        }
//
//        // Convert between HashMap and Hashtable
//        if (arg instanceof HashMap && destClass == Hashtable.class) {
//            return new Hashtable((HashMap)arg);
//        }
//
//        // Convert an AttachmentPart to the given destination class.
//        if (isAttachmentSupported() &&
//                (arg instanceof InputStream || arg instanceof AttachmentPart || arg instanceof DataHandler)) {
//            try {
//                String destName = destClass.getName();
//                if (destClass == String.class
//                        || destClass == OctetStream.class
//                        || destClass == byte[].class
//                        || destClass == Image.class
//                        || destClass == Source.class
//                        || destClass == DataHandler.class
//                        || destName.equals("javax.mail.internet.MimeMultipart")) {
//                    DataHandler handler = null;
//                    if (arg instanceof AttachmentPart) {
//                        handler = ((AttachmentPart) arg).getDataHandler();
//                    }
//                    else if (arg instanceof DataHandler) {
//                        handler = (DataHandler) arg;
//                    }
//                    if (destClass == Image.class) {
//                        // Note:  An ImageIO component is required to process an Image
//                        // attachment, but if the image would be null
//                        // (is.available == 0) then ImageIO component isn't needed
//                        // and we can return null.
//                        InputStream is = (InputStream) handler.getContent();
//                        if (is.available() == 0) {
//                            return null;
//                        }
//                        else {
//                            ImageIO imageIO = ImageIOFactory.getImageIO();
//                            if (imageIO != null) {
//                                return getImageFromStream(is);
//                            }
//                            else {
//                                log.info(Messages.getMessage("needImageIO"));
//                                return arg;
//                            }
//                        }
//                    }
//                    else if (destClass == javax.xml.transform.Source.class) {
//                        // For a reason unknown to me, the handler's
//                        // content is a String.  Convert it to a
//                        // StreamSource.
//                        return new StreamSource(new StringReader(
//                                (String) handler.getContent()));
//                    }
//                    else if (destClass == OctetStream.class || destClass == byte[].class) {
//                        InputStream in = null;
//                        if (arg instanceof InputStream) {
//                            in = (InputStream) arg;
//                        } else {
//                            in = (InputStream)handler.getContent();
//                        }
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        int byte1 = -1;
//                        while((byte1 = in.read())!=-1)
//                            baos.write(byte1);
//                        return new OctetStream(baos.toByteArray());
//                    }
//                    else if (destClass == DataHandler.class) {
//                        return handler;
//                    }
//                    else {
//                        return handler.getContent();
//                    }
//                }
//            }
//            catch (IOException ioe) {
//            }
//            catch (SOAPException se) {
//            }
//        }
//
//        // If the destination is an array and the source
//        // is a suitable component, return an array with
//        // the single item.
//        if (arg != null &&
//            destClass.isArray() &&
//            !destClass.getComponentType().equals(Object.class) &&
//            destClass.getComponentType().isAssignableFrom(arg.getClass())) {
//            Object array =
//                Array.newInstance(destClass.getComponentType(), 1);
//            Array.set(array, 0, arg);
//            return array;
//        }
//
//        // in case destClass is array and arg is ArrayOfT class. (ArrayOfT -> T[])
//        if (arg != null && destClass.isArray()) {
//            Object newArg = ArrayUtil.convertObjectToArray(arg, destClass);
//            if (newArg == null
//                    || (newArg != ArrayUtil.NON_CONVERTABLE && newArg != arg)) {
//                return newArg;
//            }
//        }
//
//        // in case arg is ArrayOfT and destClass is an array. (T[] -> ArrayOfT)
//        if (arg != null && arg.getClass().isArray()) {
//            Object newArg = ArrayUtil.convertArrayToObject(arg, destClass);
//            if (newArg != null)
//                return newArg;
//        }
//
//        // Return if no conversion is available
//        if (!(arg instanceof Collection ||
//              (arg != null && arg.getClass().isArray())) &&
//            ((destHeldType == null && argHeldType == null) ||
//             (destHeldType != null && argHeldType != null))) {
//            return arg;
//        }
//
//        // Take care of Holder conversion
//        if (destHeldType != null) {
//            // Convert arg into Holder holding arg.
//            Object newArg = convert(arg, destHeldType);
//            Object argHolder = null;
//            try {
//                argHolder = destClass.newInstance();
//                setHolderValue(argHolder, newArg);
//                return argHolder;
//            } catch (Exception e) {
//                return arg;
//            }
//        } else if (argHeldType != null) {
//            // Convert arg into the held type
//            try {
//                Object newArg = getHolderValue(arg);
//                return convert(newArg, destClass);
//            } catch (HolderException e) {
//                return arg;
//            }
//        }
//
//        // Flow to here indicates that neither arg or destClass is a Holder
//
//        // Check to see if the argument has a prefered destination class.
//        if (arg instanceof ConvertCache &&
//            (( ConvertCache) arg).getDestClass() != destClass) {
//            Class hintClass = ((ConvertCache) arg).getDestClass();
//            if (hintClass != null &&
//                hintClass.isArray() &&
//                destClass.isArray() &&
//                destClass.isAssignableFrom(hintClass)) {
//                destClass = hintClass;
//                destValue = ((ConvertCache) arg).getConvertedValue(destClass);
//                if (destValue != null)
//                    return destValue;
//            }
//        }
//
//        if (arg == null) {
//            return arg;
//        }
//
//        // The arg may be an array or List
//        int length = 0;
//        if (arg.getClass().isArray()) {
//            length = Array.getLength(arg);
//        } else {
//            length = ((Collection) arg).size();
//        }
//        if (destClass.isArray()) {
//            if (destClass.getComponentType().isPrimitive()) {
//
//                Object array = Array.newInstance(destClass.getComponentType(),
//                                                 length);
//                // Assign array elements
//                if (arg.getClass().isArray()) {
//                    for (int i = 0; i < length; i++) {
//                        Array.set(array, i, Array.get(arg, i));
//                    }
//                } else {
//                    int idx = 0;
//                    for (Iterator i = ((Collection)arg).iterator();
//                            i.hasNext();) {
//                        Array.set(array, idx++, i.next());
//                    }
//                }
//                destValue = array;
//
//            } else {
//                Object [] array;
//                try {
//                    array = (Object [])Array.newInstance(destClass.getComponentType(),
//                                                         length);
//                } catch (Exception e) {
//                    return arg;
//                }
//
//                // Use convert to assign array elements.
//                if (arg.getClass().isArray()) {
//                    for (int i = 0; i < length; i++) {
//                        array[i] = convert(Array.get(arg, i),
//                                           destClass.getComponentType());
//                    }
//                } else {
//                    int idx = 0;
//                    for (Iterator i = ((Collection)arg).iterator();
//                            i.hasNext();) {
//                        array[idx++] = convert(i.next(),
//                                           destClass.getComponentType());
//                    }
//                }
//                destValue = array;
//            }
//        }
//        else if (Collection.class.isAssignableFrom(destClass)) {
//            Collection newList = null;
//            try {
//                // if we are trying to create an interface, build something
//                // that implements the interface
//                if (destClass == Collection.class || destClass == List.class) {
//                    newList = new ArrayList();
//                } else if (destClass == Set.class) {
//                    newList = new HashSet();
//                } else {
//                    newList = (Collection)destClass.newInstance();
//                }
//            } catch (Exception e) {
//                // Couldn't build one for some reason... so forget it.
//                return arg;
//            }
//
//            if (arg.getClass().isArray()) {
//                for (int j = 0; j < length; j++) {
//                    newList.add(Array.get(arg, j));
//                }
//            } else {
//                for (Iterator j = ((Collection)arg).iterator();
//                            j.hasNext();) {
//                    newList.add(j.next());
//                }
//            }
//            destValue = newList;
//        }
//        else {
//            destValue = arg;
//        }
//
//        // Store the converted value in the argument if possible.
//        if (arg instanceof ConvertCache) {
//            (( ConvertCache) arg).setConvertedValue(destClass, destValue);
//        }
//        return destValue;
//    }

//    public static boolean isConvertable(Object obj, Class dest)
//    {
//        return isConvertable(obj, dest, false);
//    }
//
//    public static boolean isConvertable(Object obj, Class dest, boolean isEncoded)
//    {
//        Class src = null;
//
//        if (obj != null) {
//            if (obj instanceof Class) {
//                src = (Class)obj;
//            } else {
//                src = obj.getClass();
//            }
//        } else {
//            if(!dest.isPrimitive())
//                return true;
//        }
//
//        if (dest == null)
//            return false;
//
//        if (src != null) {
//            // If we're directly assignable, we're good.
//            if (dest.isAssignableFrom(src))
//                return true;
//
//            //Allow mapping of Map's to Map's
//            if (java.util.Map.class.isAssignableFrom(dest) &&
//                java.util.Map.class.isAssignableFrom(src)) {
//                  return true;
//            }
//
//            // If it's a wrapping conversion, we're good.
//            if (getWrapperClass(src) == dest)
//                return true;
//            if (getWrapperClass(dest) == src)
//                return true;
//
//            // If it's List -> Array or vice versa, we're good.
//            if ((Collection.class.isAssignableFrom(src) || src.isArray()) &&
//                (Collection.class.isAssignableFrom(dest) || dest.isArray()) &&
//                (src.getComponentType() == Object.class ||
//                 src.getComponentType() == null ||
//                 dest.getComponentType() == Object.class ||
//                 dest.getComponentType() == null ||
//                 isConvertable(src.getComponentType(), dest.getComponentType())))
//                    return true;
//
//            // If destination is an array, and src is a component, we're good
//            // if we're not encoded!
//            if (!isEncoded && dest.isArray() &&
////                !dest.getComponentType().equals(Object.class) &&
//                dest.getComponentType().isAssignableFrom(src))
//                return true;
//
//            if ((src == HexBinary.class && dest == byte[].class) ||
//                (src == byte[].class && dest == HexBinary.class))
//                return true;
//
//            // Allow mapping of Calendar to Date
//            if (Calendar.class.isAssignableFrom(src) && dest == Date.class)
//                return true;
//
//            // Allow mapping of Date to Calendar
//            if (Date.class.isAssignableFrom(src) && dest == Calendar.class)
//                return true;
//
//            // Allow mapping of Calendar to java.sql.Date
//            if (Calendar.class.isAssignableFrom(src) && dest == java.sql.Date.class)
//                return true;
//        }
//
//        Class destHeld = JavaUtils.getHolderValueType(dest);
//        // Can always convert a null to an empty holder
//        if (src == null)
//            return (destHeld != null);
//
//        if (destHeld != null) {
//            if (destHeld.isAssignableFrom(src) || isConvertable(src, destHeld))
//                return true;
//        }
//
//        // If it's holder -> held or held -> holder, we're good
//        Class srcHeld = JavaUtils.getHolderValueType(src);
//        if (srcHeld != null) {
//            if (dest.isAssignableFrom(srcHeld) || isConvertable(srcHeld, dest))
//                return true;
//        }
//
//        // If it's a MIME type mapping and we want a DataHandler,
//        // then we're good.
//        if (dest.getName().equals("javax.activation.DataHandler")) {
//            String name = src.getName();
//            if (src == String.class
//                    || src == java.awt.Image.class
//                    || src == OctetStream.class
//                    || name.equals("javax.mail.internet.MimeMultipart")
//                    || name.equals("javax.xml.transform.Source"))
//                return true;
//        }
//
//        if (src.getName().equals("javax.activation.DataHandler")) {
//            if (dest ==  byte[].class)
//                return true;
//            if (dest.isArray() && dest.getComponentType() == byte[].class)
//                return true;
//        }
//
//        if (dest.getName().equals("javax.activation.DataHandler")) {
//            if (src ==  Object[].class)
//                return true;
//            if (src.isArray() && src.getComponentType() == Object[].class)
//                return true;
//        }
//
//        if (obj instanceof java.io.InputStream) {
//            if (dest ==  OctetStream.class)
//                return true;
//        }
//
//        if (src.isPrimitive()) {
//            return isConvertable(getWrapperClass(src),dest);
//        }
//
//        // ArrayOfT -> T[] ?
//        if (dest.isArray()) {
//        	if (ArrayUtil.isConvertable(src, dest) == true)
//        		return true;
//        }
//
//        // T[] -> ArrayOfT ?
//        if (src.isArray()) {
//        	if (ArrayUtil.isConvertable(src, dest) == true)
//        		return true;
//        }
//
//        return false;
//    }
//
//    public static Image getImageFromStream(InputStream is) {
//        try {
//            return ImageIOFactory.getImageIO().loadImage(is);
//        }
//        catch (Throwable t) {
//            return null;
//        }
//    } // getImageFromStream
    
    /**
     * These are java keywords as specified at the following URL (sorted alphabetically).
     * http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#229308
     * Note that false, true, and null are not strictly keywords; they are literal values,
     * but for the purposes of this array, they can be treated as literals.
     *    ****** PLEASE KEEP THIS LIST SORTED IN ASCENDING ORDER ******
     */
    static final String keywords[] =
    {
        "abstract",  "assert",       "boolean",    "break",      "byte",      "case",
        "catch",     "char",         "class",      "const",     "continue",
        "default",   "do",           "double",     "else",      "extends",
        "false",     "final",        "finally",    "float",     "for",
        "goto",      "if",           "implements", "import",    "instanceof",
        "int",       "interface",    "long",       "native",    "new",
        "null",      "package",      "private",    "protected", "public",
        "return",    "short",        "static",     "strictfp",  "super",
        "switch",    "synchronized", "this",       "throw",     "throws",
        "transient", "true",         "try",        "void",      "volatile",
        "while"
    };

    /** Collator for comparing the strings */
    static final Collator englishCollator = Collator.getInstance(Locale.ENGLISH);

    /** Use this character as suffix */
    static final char keywordPrefix = '_';

    /**
     * isJavaId
     * Returns true if the name is a valid java identifier.
     * @param id to check
     * @return boolean true/false
     **/
    public static boolean isJavaId(String id) {
        if (id == null || id.equals("") || isJavaKeyword(id))
            return false;
        if (!Character.isJavaIdentifierStart(id.charAt(0)))
            return false;
        for (int i=1; i<id.length(); i++)
            if (!Character.isJavaIdentifierPart(id.charAt(i)))
                return false;
        return true;
    }

    /**
     * checks if the input string is a valid java keyword.
     * @return boolean true/false
     */
    public static boolean isJavaKeyword(String keyword) {
      return (Arrays.binarySearch(keywords, keyword, englishCollator) >= 0);
    }

    /**
     * Turn a java keyword string into a non-Java keyword string.  (Right now
     * this simply means appending an underscore.)
     */
    public static String makeNonJavaKeyword(String keyword){
        return  keywordPrefix + keyword;
     }

    /**
     * Converts text of the form
     * Foo[] to the proper class name for loading [LFoo
     */
    public static String getLoadableClassName(String text) {
        if (text == null ||
            text.indexOf("[") < 0 ||
            text.charAt(0) == '[')
            return text;
        String className = text.substring(0,text.indexOf("["));
        if (className.equals("byte"))
            className = "B";
        else if (className.equals("char"))
            className = "C";
        else if (className.equals("double"))
            className = "D";
        else if (className.equals("float"))
            className = "F";
        else if (className.equals("int"))
            className = "I";
        else if (className.equals("long"))
            className = "J";
        else if (className.equals("short"))
            className = "S";
        else if (className.equals("boolean"))
            className = "Z";
        else
            className = "L" + className + ";";
        int i = text.indexOf("]");
        while (i > 0) {
            className = "[" + className;
            i = text.indexOf("]", i+1);
        }
        return className;
    }

    /**
     * Converts text of the form
     * [LFoo to the Foo[]
     */
    public static String getTextClassName(String text) {
        if (text == null ||
            text.indexOf("[") != 0)
            return text;
        String className = "";
        int index = 0;
        while(index < text.length() &&
              text.charAt(index) == '[') {
            index ++;
            className += "[]";
        }
        if (index < text.length()) {
            if (text.charAt(index)== 'B')
                className = "byte" + className;
            else if (text.charAt(index) == 'C')
                className = "char" + className;
            else if (text.charAt(index) == 'D')
                className = "double" + className;
            else if (text.charAt(index) == 'F')
                className = "float" + className;
            else if (text.charAt(index) == 'I')
                className = "int" + className;
            else if (text.charAt(index) == 'J')
                className = "long" + className;
            else if (text.charAt(index) == 'S')
                className = "short" + className;
            else if (text.charAt(index) == 'Z')
                className = "boolean" + className;
            else {
                className = text.substring(index+1, text.indexOf(";")) + className;
            }
        }
        return className;
    }

    /**
     * Map an XML name to a Java identifier per
     * the mapping rules of JSR 101 (in version 1.0 this is
     * "Chapter 20: Appendix: Mapping of XML Names"
     * 
     * @param name is the xml name
     * @return the java name per JSR 101 specification
     */
    public static String xmlNameToJava(String name)
    {
        // protect ourselves from garbage
        if (name == null || name.equals(""))
            return name;

        char[] nameArray = name.toCharArray();
        int nameLen = name.length();
        StringBuffer result = new StringBuffer(nameLen);
        boolean wordStart = false;

        // The mapping indicates to convert first character.
        int i = 0;
        while (i < nameLen
                && (isPunctuation(nameArray[i])
                || !Character.isJavaIdentifierStart(nameArray[i]))) {
            i++;
        }
        if (i < nameLen) {
            // Decapitalization code used to be here, but we use the
            // Introspector function now after we filter out all bad chars.
            
            result.append(nameArray[i]);
            //wordStart = !Character.isLetter(nameArray[i]);
            wordStart = !Character.isLetter(nameArray[i]) && nameArray[i] != "_".charAt(0);
        }
        else {
            // The identifier cannot be mapped strictly according to
            // JSR 101
            if (Character.isJavaIdentifierPart(nameArray[0])) {
                result.append("_" + nameArray[0]);
            }
            else {
                // The XML identifier does not contain any characters
                // we can map to Java.  Using the length of the string
                // will make it somewhat unique.
                result.append("_" + nameArray.length);
            }
        }

        // The mapping indicates to skip over
        // all characters that are not letters or
        // digits.  The first letter/digit
        // following a skipped character is
        // upper-cased.
        for (++i; i < nameLen; ++i) {
            char c = nameArray[i];

            // if this is a bad char, skip it and remember to capitalize next
            // good character we encounter
            if (isPunctuation(c) || !Character.isJavaIdentifierPart(c)) {
                wordStart = true;
                continue;
            }
            if (wordStart && Character.isLowerCase(c)) {
                result.append(Character.toUpperCase(c));
            }
            else {
                result.append(c);
            }
            // If c is not a character, but is a legal Java
            // identifier character, capitalize the next character.
            // For example:  "22hi" becomes "22Hi"
            //wordStart = !Character.isLetter(c);
            wordStart = !Character.isLetter(c) && c != "_".charAt(0);
        }

        // covert back to a String
        String newName = result.toString();
        
        // Follow JavaBean rules, but we need to check if the first 
        // letter is uppercase first
        if (Character.isUpperCase(newName.charAt(0)))
            newName = Introspector.decapitalize(newName);

        // check for Java keywords
        if (isJavaKeyword(newName))
            newName = makeNonJavaKeyword(newName);

        return newName;
    } // xmlNameToJava

    /**
     * Is this an XML punctuation character?
     */
    private static boolean isPunctuation(char c)
    {
        return '-' == c
            || '.' == c
            || ':' == c
            || '\u00B7' == c
            || '\u0387' == c
            || '\u06DD' == c
            || '\u06DE' == c;
    } // isPunctuation


    /**
     * replace:
     * Like String.replace except that the old new items are strings.
     *
     * @param name string
     * @param oldT old text to replace
     * @param newT new text to use
     * @return replacement string
     **/
    public static final String replace (String name,
                                        String oldT, String newT) {

        if (name == null) return "";

        // Create a string buffer that is twice initial length.
        // This is a good starting point.
        StringBuffer sb = new StringBuffer(name.length()* 2);

        int len = oldT.length ();
        try {
            int start = 0;
            int i = name.indexOf (oldT, start);

            while (i >= 0) {
                sb.append(name.substring(start, i));
                sb.append(newT);
                start = i+len;
                i = name.indexOf(oldT, start);
            }
            if (start < name.length())
                sb.append(name.substring(start));
        } catch (NullPointerException e) {
        }

        return new String(sb);
    }

//    /**
//     * Determines if the Class is a Holder class. If so returns Class of held type
//     * else returns null
//     * @param type the suspected Holder Class
//     * @return class of held type or null
//     */
//    public static Class getHolderValueType(Class type) {
//        if (type != null) {
//            Class[] intf = type.getInterfaces();
//            boolean isHolder = false;
//            for (int i=0; i<intf.length && !isHolder; i++) {
//                if (intf[i] == javax.xml.rpc.holders.Holder.class) {
//                    isHolder = true;
//                }
//            }
//            if (isHolder == false) {
//                return null;
//            }
//
//            // Holder is supposed to have a public value field.
//            java.lang.reflect.Field field;
//            try {
//                field = type.getField("value");
//            } catch (Exception e) {
//                field = null;
//            }
//            if (field != null) {
//                return field.getType();
//            }
//        }
//        return null;
//    }

//    /**
//     * Gets the Holder value.
//     * @param holder Holder object
//     * @return value object
//     */
//    public static Object getHolderValue(Object holder) throws HolderException {
//        if (!(holder instanceof javax.xml.rpc.holders.Holder)) {
//            throw new HolderException(Messages.getMessage("badHolder00"));
//        }
//        try {
//            Field valueField = holder.getClass().getField("value");
//            return valueField.get(holder);
//        } catch (Exception e) {
//          throw new HolderException(Messages.getMessage("exception01", e.getMessage()));
//        }
//    }
//
//    /**
//     * Sets the Holder value.
//     * @param holder Holder object
//     * @param value is the object value
//     */
//    public static void setHolderValue(Object holder, Object value) throws HolderException {
//        if (!(holder instanceof javax.xml.rpc.holders.Holder)) {
//            throw new HolderException(Messages.getMessage("badHolder00"));
//        }
//        try {
//            Field valueField = holder.getClass().getField("value");
//            if (valueField.getType().isPrimitive()) {
//                if (value == null)
//                    ;  // Don't need to set anything
//                else
//                    valueField.set(holder, value);  // Automatically unwraps value to primitive
//            } else {
//                valueField.set(holder, value);
//            }
//        } catch (Exception e) {
//          throw new HolderException(Messages.getMessage("exception01", e.getMessage()));
//        }
//    }
    public static class HolderException extends Exception
    {
        public HolderException(String msg) { super(msg); }
    }

    
    /**
     * Used to cache a result from IsEnumClassSub(). 
     * Class->Boolean mapping.
     */
    private static HashMap enumMap = new HashMap();
    
    /**
     * Determine if the class is a JAX-RPC enum class.
     * An enumeration class is recognized by
     * a getValue() method, a toString() method, a fromString(String) method
     * a fromValue(type) method and the lack
     * of a setValue(type) method
     */
    public static boolean isEnumClass(Class cls) {
        Boolean b = (Boolean)enumMap.get(cls);
        if (b == null) {
            b = (isEnumClassSub(cls)) ? Boolean.TRUE : Boolean.FALSE;
            enumMap.put(cls, b);
        }
        return b.booleanValue();
    }

    private static boolean isEnumClassSub(Class cls) {
        try {
            java.lang.reflect.Method[] methods = cls.getMethods();
            java.lang.reflect.Method getValueMethod = null, 
                fromValueMethod = null,
                setValueMethod = null, fromStringMethod = null;
            
            // linear search: in practice, this is faster than
            // sorting/searching a short array of methods.
            for (int i = 0; i < methods.length; i++) {
                String name = methods[i].getName();

                if (name.equals("getValue")
                    && methods[i].getParameterTypes().length == 0) { // getValue()
                    getValueMethod = methods[i];
                } else if (name.equals("fromString")) { // fromString(String s)
                    Object[] params = methods[i].getParameterTypes();
                    if (params.length == 1
                        && params[0] == String.class) {
                        fromStringMethod = methods[i];
                    }
                } else if (name.equals("fromValue")
                           && methods[i].getParameterTypes().length == 1) { // fromValue(Something s)
                    fromValueMethod = methods[i];
                } else if (name.equals("setValue")
                           && methods[i].getParameterTypes().length == 1) { // setValue(Something s)
                    setValueMethod = methods[i];
                }
            }

            // must have getValue and fromString, but not setValue
            // must also have toString(), but every Object subclass has that, so
            // no need to check for it.
            if (null != getValueMethod && null != fromStringMethod) {
                if (null != setValueMethod
                    && setValueMethod.getParameterTypes().length == 1
                    && getValueMethod.getReturnType() == setValueMethod.getParameterTypes()[0]) {
                    // setValue exists: return false
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } catch (java.lang.SecurityException e) {
            return false;
        } // end of catch
    }

    public static String stackToString(Throwable e){
      java.io.StringWriter sw= new java.io.StringWriter(1024); 
      java.io.PrintWriter pw= new java.io.PrintWriter(sw); 
      e.printStackTrace(pw);
      pw.close();
      return sw.toString();
    }

    /**
     * Tests the String 'value':
     *   return 'false' if its 'false', '0', or 'no' - else 'true'
     * 
     * Follow in 'C' tradition of boolean values:
     * false is specific (0), everything else is true;
     */
    public static final boolean isTrue(String value) {
        return !isFalseExplicitly(value);
    }

    /**
     * Tests the String 'value':
     *   return 'true' if its 'true', '1', or 'yes' - else 'false'
     */
    public static final boolean isTrueExplicitly(String value) {
        return value != null  &&
               (value.equalsIgnoreCase("true")  ||
                value.equals("1")  ||
                value.equalsIgnoreCase("yes"));
    }

    /**
     * Tests the Object 'value':
     *   if its null, return default.
     *   if its a Boolean, return booleanValue()
     *   if its an Integer,  return 'false' if its '0' else 'true'
     *   if its a String, return isTrueExplicitly((String)value).
     *   All other types return 'true'
     */
    public static final boolean isTrueExplicitly(Object value, boolean defaultVal) {
        if ( value == null ) return defaultVal;
        if ( value instanceof Boolean ) {
            return ((Boolean)value).booleanValue();
        }
        if ( value instanceof Integer ) {
            return ((Integer)value).intValue() != 0;
        }
        if ( value instanceof String ) {
            return isTrueExplicitly( (String)value );
        }
        return true;
    }
    
    public static final boolean isTrueExplicitly(Object value) {
        return isTrueExplicitly(value, false);
    }

    /**
     * Tests the Object 'value':
     *   if its null, return default.
     *   if its a Boolean, return booleanValue()
     *   if its an Integer,  return 'false' if its '0' else 'true'
     *   if its a String, return 'false' if its 'false', 'no', or '0' - else 'true'
     *   All other types return 'true'
     */
    public static final boolean isTrue(Object value, boolean defaultVal) {
        return !isFalseExplicitly(value, !defaultVal);
    }
    
    public static final boolean isTrue(Object value) {
        return isTrue(value, false);
    }
    
    /**
     * Tests the String 'value':
     *   return 'true' if its 'false', '0', or 'no' - else 'false'
     * 
     * Follow in 'C' tradition of boolean values:
     * false is specific (0), everything else is true;
     */
    public static final boolean isFalse(String value) {
        return isFalseExplicitly(value);
    }

    /**
     * Tests the String 'value':
     *   return 'true' if its null, 'false', '0', or 'no' - else 'false'
     */
    public static final boolean isFalseExplicitly(String value) {
        return value == null  ||
               value.equalsIgnoreCase("false")  ||
               value.equals("0")  ||
               value.equalsIgnoreCase("no");
    }
    
    /**
     * Tests the Object 'value':
     *   if its null, return default.
     *   if its a Boolean, return !booleanValue()
     *   if its an Integer,  return 'true' if its '0' else 'false'
     *   if its a String, return isFalseExplicitly((String)value).
     *   All other types return 'false'
     */
    public static final boolean isFalseExplicitly(Object value, boolean defaultVal) {
        if ( value == null ) return defaultVal;
        if ( value instanceof Boolean ) {
            return !((Boolean)value).booleanValue();
        }
        if ( value instanceof Integer ) {
            return ((Integer)value).intValue() == 0;
        }
        if ( value instanceof String ) {
            return isFalseExplicitly( (String)value );
        }
        return false;
    }
    
    public static final boolean isFalseExplicitly(Object value) {
        return isFalseExplicitly(value, true);
    }

    /**
     * Tests the Object 'value':
     *   if its null, return default.
     *   if its a Boolean, return booleanValue()
     *   if its an Integer,  return 'false' if its '0' else 'true'
     *   if its a String, return 'false' if its 'false', 'no', or '0' - else 'true'
     *   All other types return 'true'
     */
    public static final boolean isFalse(Object value, boolean defaultVal) {
        return isFalseExplicitly(value, defaultVal);
    }
    
    public static final boolean isFalse(Object value) {
        return isFalse(value, true);
    }
    
    /**
     * Given the MIME type string, return the Java mapping.
     */
    public static String mimeToJava(String mime) {
        if ("image/gif".equals(mime) || "image/jpeg".equals(mime)) {
            return "java.awt.Image";
        }
        else if ("text/plain".equals(mime)) {
            return "java.lang.String";
        }
        else if ("text/xml".equals(mime) || "application/xml".equals(mime)) {
            return "javax.xml.transform.Source";
        }
        else if ("application/octet-stream".equals(mime)||
                 "application/octetstream".equals(mime)) {
            return "org.apache.axis.attachments.OctetStream";
        }
        else if (mime != null && mime.startsWith("multipart/")) {
            return "javax.mail.internet.MimeMultipart";
        }
        else {
            return "javax.activation.DataHandler";
        }
    } // mimeToJava

    //avoid testing and possibly failing everytime.
    private static boolean checkForAttachmentSupport = true;
    private static boolean attachmentSupportEnabled = false;

    /**
     * Determine whether attachments are supported by checking if the following
     * classes are available:  javax.activation.DataHandler,
     * javax.mail.internet.MimeMultipart.
     */
//    public static synchronized boolean isAttachmentSupported() {
//
//        if (checkForAttachmentSupport) {
//            //aviod testing and possibly failing everytime.
//            checkForAttachmentSupport = false;
//            try {
//                // Attempt to resolve DataHandler and MimeMultipart and
//                // javax.xml.transform.Source, all necessary for full
//                // attachment support
//                ClassUtils.forName("javax.activation.DataHandler");
//                ClassUtils.forName("javax.mail.internet.MimeMultipart");
//                attachmentSupportEnabled = true;
//            } catch (Throwable t) {
//            }
//            log.debug(Messages.getMessage("attachEnabled") + "  " +
//                    attachmentSupportEnabled);
//            if(!attachmentSupportEnabled) {
//                log.warn(Messages.getMessage("attachDisabled"));
//            }
//        }
//
//        return attachmentSupportEnabled;
//    } // isAttachmentSupported

    /**
     * Makes the value passed in <code>initValue</code> unique among the
     * {@link String} values contained in <code>values</code> by suffixing
     * it with a decimal digit suffix.
     */
    public static String  getUniqueValue(Collection values, String initValue) {

        if (!values.contains(initValue))  {
            return  initValue;
        }
        else  {

            StringBuffer   unqVal = new StringBuffer(initValue);
            int   beg = unqVal.length(),  cur,  end;
            while (Character.isDigit(unqVal.charAt(beg - 1)))  {
                beg--;
            }
            if (beg == unqVal.length())  {
                unqVal.append('1');
            }
            cur = end = unqVal.length() - 1;

            while (values.contains(unqVal.toString()))  {

                if (unqVal.charAt(cur) < '9')  {
                    unqVal.setCharAt(cur, (char) (unqVal.charAt(cur) + 1));
                }

                else  {

                    while (cur-- > beg)  {
                        if (unqVal.charAt(cur) < '9')  {
                            unqVal.setCharAt(cur,
                                (char) (unqVal.charAt(cur) + 1));
                            break;
                        }
                    }

                    // See if there's a need to insert a new digit.
                    if (cur < beg)  {
                        unqVal.insert(++cur, '1');     end++;
                    }
                    while (cur < end)  {
                        unqVal.setCharAt(++cur, '0');
                    }

                }

            }

            return  unqVal.toString();

        }  /*  For  else  clause  of  selection-statement   If(!values ...   */

    }  /*  For  class  method   JavaUtils.getUniqueValue   */
}
