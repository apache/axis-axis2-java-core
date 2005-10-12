package org.apache.axis2.databinding.schema.util;
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

public class ConverterUtil {

    public static Object convertToObject(int i){
        return new Integer(i);
    }

    public static Object convertToObject(float i){
        return new Float(i);
    }

    public static Object convertToObject(long i){
        return new Long(i);
    }

    // fill the other methods


    public static int convertToint(String s){
        return Integer.parseInt(s);
    }

    public static float convertTofloat(String s){
        return Float.parseFloat(s);
    }

      public static String convertTostring(String s){
        return s;
    }

   
    //the pass through method
    public static Object convertToObject(Object o){
        return o;
    }
    //add the others here

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
}
