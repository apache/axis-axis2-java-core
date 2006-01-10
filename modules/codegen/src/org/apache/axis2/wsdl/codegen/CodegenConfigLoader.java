package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
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

class CodegenConfigLoader implements CommandLineOptionConstants {

    public static void loadConfig(CodeGenConfiguration config, Map optionMap){
        String outputLocation = "."; //default output directory is the current working directory
        CommandLineOption outputLocationOption = loadOption(OUTPUT_LOCATION_OPTION,OUTPUT_LOCATION_OPTION_LONG,optionMap);

        if (outputLocationOption != null) {
            outputLocation = outputLocationOption.getOptionValue();
        }
        File outputLocationFile = new File(outputLocation);
        config.setOutputLocation(outputLocationFile);

        //check and create the directories
        if (outputLocationFile.exists()) {
            if (outputLocationFile.isFile()) {
                throw new RuntimeException(CodegenMessages.getMessage("options.notADirectoryException"));
            }
        } else {
            outputLocationFile.mkdirs();
        }

        config.setServerSide(loadOption(SERVER_SIDE_CODE_OPTION,SERVER_SIDE_CODE_OPTION_LONG,optionMap) != null);
        config.setGenerateDeployementDescriptor(loadOption(GENERATE_SERVICE_DESCRIPTION_OPTION,
                GENERATE_SERVICE_DESCRIPTION_OPTION_LONG,optionMap) !=
                null);
        config.setWriteTestCase(loadOption(GENERATE_TEST_CASE_OPTION,GENERATE_TEST_CASE_OPTION,optionMap) != null);

        boolean asyncFlagPresent =
                (loadOption(CODEGEN_ASYNC_ONLY_OPTION,CODEGEN_ASYNC_ONLY_OPTION_LONG,optionMap) !=  null);
        boolean syncFlagPresent =
                (loadOption(CODEGEN_SYNC_ONLY_OPTION,CODEGEN_SYNC_ONLY_OPTION_LONG,optionMap) !=   null);
        if (asyncFlagPresent) {
            config.setAsyncOn(true);
            config.setSyncOn(false);
        }
        if (syncFlagPresent) {
            config.setAsyncOn(false);
            config.setSyncOn(true);
        }

        CommandLineOption packageOption = loadOption(PACKAGE_OPTION,PACKAGE_OPTION_LONG,optionMap);
        if (packageOption != null) {
            config.setPackageName(packageOption.getOptionValue());
        }

        CommandLineOption langOption = loadOption(STUB_LANGUAGE_OPTION,STUB_LANGUAGE_OPTION_LONG,optionMap);
        //The language here
        if (langOption != null) {
            config.setOutputLanguage(langOption.getOptionValue());
        }

        CommandLineOption dataBindingOption = loadOption(DATA_BINDING_TYPE_OPTION,DATA_BINDING_TYPE_OPTION_LONG,optionMap);
        if (dataBindingOption != null) {
            config.setDatabindingType(dataBindingOption.getOptionValue());
        }

        CommandLineOption unwrapClassesOption = loadOption(UNPACK_CLASSES_OPTION,UNPACK_CLASSES_OPTION_LONG,optionMap);
        if (unwrapClassesOption != null) {
            config.setPackClasses(false);
        }

        CommandLineOption portNameOption = loadOption(PORT_NAME_OPTION,PORT_NAME_OPTION_LONG,optionMap);
        config.setPortName(portNameOption!=null?portNameOption.getOptionValue():null);

        CommandLineOption serviceNameOption = loadOption(SERVICE_NAME_OPTION,SERVICE_NAME_OPTION_LONG,optionMap);
        config.setServiceName(serviceNameOption!=null?serviceNameOption.getOptionValue():null);


        CommandLineOption generateAllOption = loadOption(GENERATE_ALL_OPTION,GENERATE_ALL_OPTION_LONG,optionMap);
        if (generateAllOption != null) {
            config.setGenerateAll(true);
        }

        //loop through the map and find parameters having the extra prefix.
        //put them in the property map
        Iterator keyIterator = optionMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            Object key = keyIterator.next();
            CommandLineOption option = (CommandLineOption) optionMap.get(key);
            if (key.toString().startsWith(EXTRA_OPTIONTYPE_PREFIX)) {
                //add this to the property map
                config.getProperties().put(key.toString().replaceFirst(EXTRA_OPTIONTYPE_PREFIX, ""), option.getOptionValue());
            }
        }



    }

    private static CommandLineOption loadOption(String shortOption, String longOption,Map options) {
        //short option gets precedence
        CommandLineOption option = null;
        if (longOption!=null){
           option =(CommandLineOption)options.get(longOption);
           if (option!=null) {
               return option;
           }
        }
        if (shortOption!= null){
            option = (CommandLineOption)options.get(shortOption);
        }

        return option;
    }

}
