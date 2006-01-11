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

package org.apache.axis2.wsdl.util;

public interface CommandLineOptionConstants {

    public static final String SOLE_INPUT = "SOLE_INPUT";

    public static final String WSDL_LOCATION_URI_OPTION = "uri";

     //short option constants
    public static final String OUTPUT_LOCATION_OPTION = "o";
    public static final String SERVER_SIDE_CODE_OPTION = "ss";
    public static final String GENERATE_SERVICE_DESCRIPTION_OPTION = "sd";
    public static final String CODEGEN_ASYNC_ONLY_OPTION = "a";
    public static final String CODEGEN_SYNC_ONLY_OPTION = "s";
    public static final String PACKAGE_OPTION = "p";
    public static final String STUB_LANGUAGE_OPTION = "l";
    public static final String GENERATE_TEST_CASE_OPTION = "t";
    public static final String DATA_BINDING_TYPE_OPTION = "d";
    public static final String UNPACK_CLASSES_OPTION = "u";
    public static final String GENERATE_ALL_OPTION = "g";
    public static final String PORT_NAME_OPTION = "pn";
    public static final String SERVICE_NAME_OPTION = "sn";

     //long option constants
    public static final String OUTPUT_LOCATION_OPTION_LONG = "output";
    public static final String SERVER_SIDE_CODE_OPTION_LONG = "server-side";
    public static final String GENERATE_SERVICE_DESCRIPTION_OPTION_LONG = "service-description";
    public static final String CODEGEN_ASYNC_ONLY_OPTION_LONG = "async";
    public static final String CODEGEN_SYNC_ONLY_OPTION_LONG = "sync";
    public static final String PACKAGE_OPTION_LONG = "package";
    public static final String STUB_LANGUAGE_OPTION_LONG = "language";
    public static final String GENERATE_TEST_CASE_OPTION_LONG = "test-case";
    public static final String DATA_BINDING_TYPE_OPTION_LONG = "databinding-method";
    public static final String UNPACK_CLASSES_OPTION_LONG = "unpack-classes";
    public static final String GENERATE_ALL_OPTION_LONG = "generate-all";
    public static final String PORT_NAME_OPTION_LONG = "port-name";
    public static final String SERVICE_NAME_OPTION_LONG = "service-name";


    public static final String INVALID_OPTION = "INVALID_OPTION";


     public static String EXTRA_OPTIONTYPE_PREFIX = "E";
}
