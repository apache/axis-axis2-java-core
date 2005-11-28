package org.apache.axis2.databinding.schema.util;

import org.apache.axis2.databinding.schema.SchemaConstants;
import org.apache.axis2.databinding.schema.types.*;

import java.util.List;
import java.util.Calendar;
import java.lang.reflect.Array;
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
    public static String convertToString(int i){
        return i+"";
    }

    public static String convertToString(float i){
        return i+"";
    }

    public static String convertToString(long i){
        return i+"";
    }

    public static String convertToString(double i){
        return i+"";
    }

    public static String convertToString(byte i){
        return new String(new byte[]{i});
    }

    public static String convertToString(char i){
        return new String(new char[]{i});
    }

    public static String convertToString(short i){
        return i+"";
    }

    public static String convertToString(boolean i){
        return i+"";
    }

    public static String convertToString(Object o){
        return o.toString();
    }



    /* String to java type conversions
       These methods have a special signature structure
       <code>convertTo</code> followed by the schema type name
       Say for int, convertToint(String) is the converter method

       Not very elegant but it seems to be the only way!

    */


    public static int convertToint(String s){
        return Integer.parseInt(s);
    }

    public static float convertTofloat(String s){
        return Float.parseFloat(s);
    }

    public static String convertTostring(String s){
        return s;
    }

    public static long convertTolong(String s){
        return Long.parseLong(s);
    }

    public static short convertToshort(String s){
        return Short.parseShort(s);
    }

    public static boolean convertToboolean(String s){
        return Boolean.getBoolean(s);
    }

    public static boolean convertToanyType(String s){
        return Boolean.getBoolean(s);
    }

    public static YearMonth convertTogYearMonth(String s){
        return new YearMonth(s);
    }

    public static MonthDay convertTogMonthDay(String s){
        return new MonthDay(s);
    }

    public static Year convertTogYear(String s){
        return new Year(s);
    }

    public static Month convertTogMonth(String s){
        return new Month(s);
    }

    public static Day convertTogDay(String s){
        return new Day(s);
    }

    public static Duration convertToduration(String s){
        return new Duration(s);
    }


    public static Token convertTotoken(String s){
        return new Token(s);
    }
    public static NormalizedString convertTonormalizedString(String s){
        return new NormalizedString(s);
    }
    public static UnsignedLong convertTounsignedLong(String s){
        return new UnsignedLong(s);
    }
    public static UnsignedInt convertTounsignedInt(String s){
        return new UnsignedInt(s);
    }
    public static UnsignedShort convertTounsignedShort(String s){
        return new UnsignedShort(s);
    }
    public static UnsignedByte convertTounsignedByte(String s){
        return new UnsignedByte(s);
    }
    public static NonNegativeInteger convertTononNegativeInteger(String s){
        return new NonNegativeInteger(s);
    }
    public static NegativeInteger convertTonegativeInteger(String s){
        return new NegativeInteger(s);
    }
    public static PositiveInteger convertTopositiveInteger(String s){
        return new PositiveInteger(s);
    }
    public static NonPositiveInteger convertTononPositiveInteger(String s){
        return new NonPositiveInteger(s);
    }

    public static Name convertToName(String s){
        return new Name(s);
    }
    public static NCName convertToNCName(String s){
        return new NCName(s);
    }
    public static Id convertToID(String s){
        return new Id(s);
    }
    public static Language convertTolanguage(String s){
        return new Language(s);
    }
    public static NMToken convertToNMTOKEN(String s){
        return new NMToken(s);
    }

    public static NMTokens convertToNMTOKENS(String s){
        return new NMTokens(s);
    }

    public static Notation convertToNOTATION(String s){
        return null; //todo Need to fix this
        // return new Notation(s);
    }

    public static Entity convertToENTITY(String s){
        return new Entity(s);
    }

    public static Entities convertToENTITIES(String s){
        return new Entities(s);
    }

    public static IDRef convertToIDREF(String s){
        return new IDRef(s);
    }
    public static IDRefs convertToIDREFS(String s){
        return new IDRefs(s);
    }

    public static URI convertToanyURI(String s) throws Exception{
        return new URI(s);
    }

     public static int convertTointeger(String s) throws Exception{
        return Integer.parseInt(s);
    }

     public static Calendar convertTodateTime(String s) throws Exception{ //need to fix this
        return null;
    }


    /* ################################################################# */

    /* java Primitive types to Object conversion methods */
    public static Object convertToObject(String i){
        return i;
    }

    public static Object convertToObject(boolean i){
        return Boolean.valueOf(i);
    }

    public static Object convertToObject(double i){
        return new Double(i);
    }

    public static Object convertToObject(byte i){
        return new Byte(i);
    }

    public static Object convertToObject(char i){
        return new Character(i);
    }

    public static Object convertToObject(short i){
        return new Short(i);
    }


    /* list to array conversion methods */

    /**
     *
     * @param baseArrayClass
     * @param objectList -> for primitive type array conversion we assume the content to be
     * strings!
     * @return
     */
    public static Object convertToArray(Class baseArrayClass, List objectList){
        int listSize = objectList.size();
        Object returnArray =  Array.newInstance(baseArrayClass,listSize);
        for (int i = 0; i < listSize; i++) {
            if (int.class.equals(baseArrayClass)){
                Array.setInt(returnArray,i,Integer.parseInt(objectList.get(i).toString()));
            }else if (float.class.equals(baseArrayClass)){
                Array.setFloat(returnArray,i,Float.parseFloat(objectList.get(i).toString()));
            }else if (short.class.equals(baseArrayClass)){
                Array.setShort(returnArray,i,Short.parseShort(objectList.get(i).toString()));
            }else if (long.class.equals(baseArrayClass)){
                Array.setLong(returnArray,i,Long.parseLong(objectList.get(i).toString()));
            }else if (boolean.class.equals(baseArrayClass)){
                Array.setBoolean(returnArray,i,Boolean.getBoolean(objectList.get(i).toString()));
            }else if (char.class.equals(baseArrayClass)){
                Array.setChar(returnArray,i,objectList.get(i).toString().toCharArray()[0]);
            }else if (double.class.equals(baseArrayClass)){
                Array.setDouble(returnArray,i,Double.parseDouble(objectList.get(i).toString()));
            }else{
                Array.set(returnArray,i,objectList.get(i));
            }
        }
        return returnArray;
    }

}
