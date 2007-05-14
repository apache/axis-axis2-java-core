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

public interface CommandLineOptionConstants {

    interface WSDL2JavaConstants {

        //short option constants
        String WSDL_LOCATION_URI_OPTION = "uri";
        String OUTPUT_LOCATION_OPTION = "o";
        String SERVER_SIDE_CODE_OPTION = "ss";
        String GENERATE_SERVICE_DESCRIPTION_OPTION = "sd";
        String CODEGEN_ASYNC_ONLY_OPTION = "a";
        String CODEGEN_SYNC_ONLY_OPTION = "s";
        String PACKAGE_OPTION = "p";
        String STUB_LANGUAGE_OPTION = "l";
        String GENERATE_TEST_CASE_OPTION = "t";
        String DATA_BINDING_TYPE_OPTION = "d";
        String UNPACK_CLASSES_OPTION = "u";
        String GENERATE_ALL_OPTION = "g";
        String PORT_NAME_OPTION = "pn";
        String SERVICE_NAME_OPTION = "sn";
        String REPOSITORY_PATH_OPTION = "r";
        String NAME_SPACE_TO_PACKAGE_OPTION = "ns2p";
        String SERVER_SIDE_INTERFACE_OPTION = "ssi";
        String EXTERNAL_MAPPING_OPTION = "em";
        String WSDL_VERSION_OPTION = "wv";
        String FLATTEN_FILES_OPTION = "f";
        String UNWRAP_PARAMETERS = "uw";
        String BACKWORD_COMPATIBILITY_OPTION = "b";
        String SUPPRESS_PREFIXES_OPTION = "sp";
        String SOURCE_FOLDER_NAME_OPTION = "S";
        String RESOURCE_FOLDER_OPTION = "R";
        String XSDCONFIG_OPTION = "xc";

        //long option constants
        String OUTPUT_LOCATION_OPTION_LONG = "output";
        String SERVER_SIDE_CODE_OPTION_LONG = "server-side";
        String GENERATE_SERVICE_DESCRIPTION_OPTION_LONG = "service-description";
        String CODEGEN_ASYNC_ONLY_OPTION_LONG = "async";
        String CODEGEN_SYNC_ONLY_OPTION_LONG = "sync";
        String PACKAGE_OPTION_LONG = "package";
        String STUB_LANGUAGE_OPTION_LONG = "language";
        String GENERATE_TEST_CASE_OPTION_LONG = "test-case";
        String DATA_BINDING_TYPE_OPTION_LONG = "databinding-method";
        String UNPACK_CLASSES_OPTION_LONG = "unpack-classes";
        String GENERATE_ALL_OPTION_LONG = "generate-all";
        String PORT_NAME_OPTION_LONG = "port-name";
        String SERVICE_NAME_OPTION_LONG = "service-name";
        String INVALID_OPTION = "INVALID_OPTION";
        String EXTRA_OPTIONTYPE_PREFIX = "E";
        String REPOSITORY_PATH_OPTION_LONG = "repository-path";
        String NAME_SPACE_TO_PACKAGE_OPTION_LONG = "namespace2package";
        String SERVER_SIDE_INTERFACE_OPTION_LONG = "serverside-interface";
        String EXTERNAL_MAPPING_OPTION_LONG = "external-mapping";
        String WSDL_VERSION_OPTION_LONG = "wsdl-version";
        String FLATTEN_FILES_OPTION_LONG = "flatten-files";
        String UNWRAP_PARAMETERS_LONG = "unwrap-params";
        String BACKWORD_COMPATIBILITY_OPTION_LONG = "backword-compatible";
        String SUPPRESS_PREFIXES_OPTION_LONG = "suppress-prefixes";
        String SOURCE_FOLDER_NAME_OPTION_LONG = "source-folder";
        String RESOURCE_FOLDER_OPTION_LONG = "resource-folder";
        String XSDCONFIG_OPTION_LONG = "xsdconfig";

        String WSDL_VERSION_2 = "2.0";
        String WSDL_VERSION_2_OPTIONAL = "2";
        String WSDL_VERSION_1 = "1.1";


    }

    interface Java2WSDLConstants {
        String OUTPUT_LOCATION_OPTION = "o";
        String OUTPUT_FILENAME_OPTION = "of";
        String CLASSNAME_OPTION = "cn";
        String CLASSPATH_OPTION = "cp";
        String TARGET_NAMESPACE_OPTION = "tn";
        String TARGET_NAMESPACE_PREFIX_OPTION = "tp";
        String SCHEMA_TARGET_NAMESPACE_OPTION = "stn";
        String SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION = "stp";
        String SERVICE_NAME_OPTION = "sn";

        //long option constants
        String OUTPUT_LOCATION_OPTION_LONG = "output";
        String TARGET_NAMESPACE_OPTION_LONG = "targetNamespace";
        String TARGET_NAMESPACE_PREFIX_OPTION_LONG = "targetNamespacePrefix";
        String SERVICE_NAME_OPTION_LONG = "serviceName";
        String CLASSNAME_OPTION_LONG = "className";
        String CLASSPATH_OPTION_LONG = "classPath";
        String OUTPUT_FILENAME_OPTION_LONG = "outputFilename";
        String SCHEMA_TARGET_NAMESPACE_OPTION_LONG = "schemaTargetnamespace";
        String SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG = "schemaTargetnamespacePrefix";


    }

    public static final String SOLE_INPUT = "SOLE_INPUT";


}
