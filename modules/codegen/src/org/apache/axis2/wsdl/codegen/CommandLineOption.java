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

import java.util.ArrayList;

public class CommandLineOption implements CommandLineOptionConstants {

    private String type;
    private ArrayList optionValues;
    private boolean invalid = false;

    public CommandLineOption(String type, String[] values) {
        updateType(type);
        ArrayList arrayList = new ArrayList(values.length);
        for (int i = 0; i < values.length; i++) {
            arrayList.add(values[i]);
        }
        this.optionValues = arrayList;
    }

    private void updateType(String type) {
        if (type.startsWith("-")) type = type.replaceFirst("-", "");

        //for options that start with the extra prefix, don't do any change for the
        //case
        if (!type.startsWith(EXTRA_OPTIONTYPE_PREFIX)){
            type = type.toLowerCase();
        }
        this.type = type;
    }

    /**
     * @param type
     */
    public CommandLineOption(String type, ArrayList values) {
        updateType(type);
        this.validate(this.type);

        if (null != values) {
            this.optionValues = values;
        }
    }


    /**
     * @return Returns the type.
     * @see <code>CommandLineOptionConstans</code>
     */
    public String getType() {
        return type;
    }


    /**
     * @return Returns the optionValues.
     */
    public String getOptionValue() {
        if (optionValues != null)
            return (String) optionValues.get(0);
        else
            return null;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isInvalid() {
        return invalid;
    }


    /**
     * @return Returns the optionValues.
     */
    public ArrayList getOptionValues() {
        return optionValues;
    }

    private void validate(String optionType) {

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
                    (GENERATE_ALL_OPTION).equalsIgnoreCase(optionType))
                    ;
        }

    }
}
