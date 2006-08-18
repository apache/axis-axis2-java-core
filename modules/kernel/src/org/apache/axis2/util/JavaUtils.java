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
package org.apache.axis2.util;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

/**
 * JavaUtils
 */
public class JavaUtils {
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
     * Checks if the input string is a valid java keyword.
     * @return Returns boolean.
     */
    public static boolean isJavaKeyword(String keyword) {
        return (Arrays.binarySearch(keywords, keyword, englishCollator) >= 0);
    }

    /**
     * Turns a java keyword string into a non-Java keyword string.  (Right now
     * this simply means appending an underscore.)
     */
    public static String makeNonJavaKeyword(String keyword){
        return  keywordPrefix + keyword;
    }

    public static String xmlNameToJava(String name) {
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

        // check for Java keywords
        if (isJavaKeyword(newName))
            newName = makeNonJavaKeyword(newName);

        return newName;
    } // xmlNameToJava

    /**
     * Capitalizes the first character of the name.
     *
     * @param name
     * @return Returns String.
     */
    public static String capitalizeFirstChar(String name) {

        if ((name == null) || name.equals("")) {
            return name;
        }

        char start = name.charAt(0);

        if (Character.isLowerCase(start)) {
            start = Character.toUpperCase(start);

            return start + name.substring(1);
        }

        return name;
    }    // capitalizeFirstChar

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
}
