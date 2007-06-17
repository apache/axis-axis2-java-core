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

package test.interop.whitemesa;

public interface WhiteMesaConstants {

    //text values of the generated SOAP envelopes.
    String ECHO_STRING = "String Argument";
    String ECHO_STRING_ARR_1 = "String Value1";
    String ECHO_STRING_ARR_2 = "String Value2";
    String ECHO_STRING_ARR_3 = "String Value3";
    String ECHO_INTEGER = "42";
    String ECHO_INTEGER_ARR_1 = "451";
    String ECHO_INTEGER_ARR_2 = "425";
    String ECHO_INTEGER_ARR_3 = "2523";
    String ECHO_FLOAT = "50.25";
    String ECHO_FLOAT_ARR_1 = "45.76";
    String ECHO_FLOAT_ARR_2 = "43.45";
    String ECHO_FLOAT_ARR_3 = "2523.54";
    String ECHO_STRUCT_INT = "42";
    String ECHO_STRUCT_FLOAT = "12.45";
    String ECHO_STRUCT_STRING = "hello world";
    String ECHO_STRUCT_ARRAY_STR_1 = "String Value1";
    String ECHO_STRUCT_ARRAY_STR_2 = "String Value2";
    String ECHO_STRUCT_ARRAY_STR_3 = "String Value3";
    String ECHO_STRUCT_ARRAY_INT_1 = "25";
    String ECHO_STRUCT_ARRAY_INT_2 = "26";
    String ECHO_STRUCT_ARRAY_INT_3 = "27";
    String ECHO_STRUCT_ARRAY_FLOAT_1 = "25.23";
    String ECHO_STRUCT_ARRAY_FLOAT_2 = "25.25";
    String ECHO_STRUCT_ARRAY_FLOAT_3 = "25.25";
    String ECHO_BASE_64 = "SGVsbG8gV29ybGQ=";
    String ECHO_HEX_BINARY = "AAABBAAE";
    String ECHO_DATE = "2006-10-18T22:20:00-07:00";
    String ECHO_DECIMAL = "455646152";
    String ECHO_BOOLEAN = "true";


    String seperator = "/";
    String nsValue = "http://soapinterop.org/";
    String nsPrefix = "ns1";
    String echoIntegerResponse = "echoIntegerResponse";
    String ret = "return";
    String echoStringResponse = "echoStringResponse";
    String echoStringArrayResponse = "echoStringArrayResponse";
    String item = "item";
    String echoIntegerArrayResponse = "echoIntegerArrayResponse";
    String echoFloatResponse = "echoFloatResponse";
    String echoFloatArrayResponse = "echoFloatArrayResponse";
    String echoStructResponse = "echoStructResponse";
    String varInt = "varInt";
    String varFloat = "varFloat";
    String varString = "varString";
    String echoVoidResponse = "echoVoidResponse";
    String echoBase64Response = "echoBase64Response";
    String echoHexBinaryResponse = "echoHexBinaryResponse";
    String echoDateResponse = "echoDateResponse";
    String echoDecimalResponse = "echoDecimalResponse";
    String echoBooleanResponse = "echoBooleanResponse";
    String echoStructArrayResponse = "echoStructArrayResponse";
    String textNodeSelector = "text()";
    String colon = ":";

}
