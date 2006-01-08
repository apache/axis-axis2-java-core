package org.apache.axis2.wsdl.util;
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


class OptionValidator implements CommandLineOptionConstants{

    public static boolean isInvalid(CommandLineOption option) {

        boolean invalid;
        String optionType = option.getOptionType();

        if (optionType.startsWith(EXTRA_OPTIONTYPE_PREFIX)){
            invalid = false;
        } else{

            invalid = !((WSDL_LOCATION_URI_OPTION).equalsIgnoreCase(optionType) ||
                    (OUTPUT_LOCATION_OPTION).equalsIgnoreCase(optionType) ||
                    (SERVER_SIDE_CODE_OPTION).equalsIgnoreCase(optionType) ||
                    (CODEGEN_ASYNC_ONLY_OPTION).equalsIgnoreCase(optionType) ||
                    (CODEGEN_SYNC_ONLY_OPTION).equalsIgnoreCase(optionType) ||
                    (PACKAGE_OPTION).equalsIgnoreCase(optionType) ||
                    (GENERATE_SERVICE_DESCRIPTION_OPTION).equalsIgnoreCase(optionType) ||
                    (GENERATE_TEST_CASE_OPTION).equalsIgnoreCase(optionType) ||
                    (STUB_LANGUAGE_OPTION).equalsIgnoreCase(optionType) ||
                    (DATA_BINDING_TYPE_OPTION).equalsIgnoreCase(optionType) ||
                    (UNPACK_CLASSES_OPTION).equalsIgnoreCase(optionType) ||
                    (GENERATE_ALL_OPTION).equalsIgnoreCase(optionType) ||

                    (OUTPUT_LOCATION_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (SERVER_SIDE_CODE_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (CODEGEN_ASYNC_ONLY_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (CODEGEN_SYNC_ONLY_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (PACKAGE_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (GENERATE_SERVICE_DESCRIPTION_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (GENERATE_TEST_CASE_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (STUB_LANGUAGE_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (DATA_BINDING_TYPE_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (UNPACK_CLASSES_OPTION_LONG).equalsIgnoreCase(optionType) ||
                    (GENERATE_ALL_OPTION_LONG).equalsIgnoreCase(optionType)
            );

        }

        return invalid;
    }


}
