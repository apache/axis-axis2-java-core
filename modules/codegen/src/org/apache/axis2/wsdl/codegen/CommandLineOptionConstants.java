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

package org.apache.axis2.wsdl.codegen;

public interface CommandLineOptionConstants {

    public static final String SOLE_INPUT = "SOLE_INPUT";

    public static final String WSDL_LOCATION_URI_OPTION = "uri";
    public static final String OUTPUT_LOCATION_OPTION = "o";
    public static final String SERVER_SIDE_CODE_OPTION = "ss";
    public static final String GENERATE_SERVICE_DESCRIPTION_OPTION = "sd";
    public static final String CODEGEN_ASYNC_ONLY_OPTION = "a";
    public static final String CODEGEN_SYNC_ONLY_OPTION = "s";
    public static final String PACKAGE_OPTION = "p";
    public static final String STUB_LANGUAGE_OPTION = "l";
    public static final String GENERATE_TEST_CASE_OPTION = "t";
    public static final String DATA_BINDING_TYPE_OPTION = "d";
    public static final String UNWRAP_CLASSES_OPTION = "u";
    public static final String GENERATE_ALL_OPTION = "g";

    public static final String INVALID_OPTION = "INVALID_OPTION";


    String EXTRA_OPTIONTYPE_PREFIX = "E";
}
