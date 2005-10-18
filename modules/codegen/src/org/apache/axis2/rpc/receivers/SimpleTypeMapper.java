package org.apache.axis2.rpc.receivers;

import org.apache.axis2.om.OMElement;

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
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 12, 2005
 * Time: 10:50:22 AM
 */
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
    private static final String INT = "int";
    private static final String BOOLEAN = "boolean";
    private static final String BYTE = "byte";
    private static final String DOUBLE = "double";
    private static final String SHORT = "short";
    private static final String LONG = "long";
    private static final String FLOAT = "float";
    private static final String CHAR = "char";

    public static Object getSimpleTypeObject(Class paramter, OMElement value) {
        if (paramter.getName().equals(STRING)) {
            return value.getText();
        } else if (paramter.getName().equals(INT)) {
            return new Integer(value.getText());
        } else if (paramter.getName().equals(BOOLEAN)) {
            return Boolean.valueOf(value.getText());
        } else if (paramter.getName().equals(BYTE)) {
            return new Byte(value.getText());
        } else if (paramter.getName().equals(DOUBLE)) {
            return new Double(value.getText());
        } else if (paramter.getName().equals(SHORT)) {
            return new Short(value.getText());
        } else if (paramter.getName().equals(LONG)) {
            return new Long(value.getText());
        } else if (paramter.getName().equals(FLOAT)) {
            return new Float(value.getText());
        } else if (paramter.getName().equals(CHAR)) {
            return new Character(value.getText().toCharArray()[0]);
        } else {
            return null;
        }
    }

    public static boolean isSimpleType(Object obj) {
        String objClassName = obj.getClass().getName();
        return isSimpleType(objClassName);
    }


    public static boolean isSimpleType(Class obj) {
        String objClassName = obj.getName();
        return isSimpleType(objClassName);
    }

    private static boolean isSimpleType(String objClassName) {
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
        } else return objClassName.equals(W_CHAR);
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
        }
        return obj.toString();
    }
}
