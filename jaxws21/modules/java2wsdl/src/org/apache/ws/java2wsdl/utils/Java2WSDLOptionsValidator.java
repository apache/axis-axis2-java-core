package org.apache.ws.java2wsdl.utils;

import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
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
*/

public class Java2WSDLOptionsValidator implements Java2WSDLConstants {
    public boolean isInvalid(Java2WSDLCommandLineOption option) {
        boolean invalid;
        String optionType = option.getOptionType();

        invalid = !((Java2WSDLConstants.CLASSNAME_OPTION).equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.CLASSPATH_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SERVICE_NAME_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.STYLE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.USE_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.LOCATION_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.EXTRA_CLASSES_DEFAULT_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.NAMESPACE_GENERATOR_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_GENERATOR_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_GENERATOR_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.JAVA_PKG_2_NSMAP_OPTION_LONG.equalsIgnoreCase(optionType) ||

                Java2WSDLConstants.CLASSNAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.CLASSNAME_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.CLASSPATH_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG.equalsIgnoreCase(optionType) ||
                Java2WSDLConstants.SERVICE_NAME_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.STYLE_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.USE_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.WSDL_VERSION_OPTION.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.WSDL_VERSION_OPTION_LONG.equalsIgnoreCase(optionType)||
                Java2WSDLConstants.LOCATION_OPTION_LONG.equalsIgnoreCase(optionType));


        return invalid;
    }
}
