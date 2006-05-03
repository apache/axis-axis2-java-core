package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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

    public static void loadConfig(CodeGenConfiguration config, Map optionMap) {
        String outputLocation = "."; //default output directory is the current working directory
        CommandLineOption outputLocationOption = loadOption(WSDL2JavaConstants.OUTPUT_LOCATION_OPTION, WSDL2JavaConstants.OUTPUT_LOCATION_OPTION_LONG, optionMap);

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

        config.setServerSide(loadOption(WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION, WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION_LONG, optionMap) != null);
        config.setGenerateDeployementDescriptor(loadOption(WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION_LONG, optionMap) !=
                null);
        config.setWriteTestCase(loadOption(WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION, WSDL2JavaConstants.GENERATE_TEST_CASE_OPTION, optionMap) != null);

        boolean asyncFlagPresent =
                (loadOption(WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION, WSDL2JavaConstants.CODEGEN_ASYNC_ONLY_OPTION_LONG, optionMap) != null);
        boolean syncFlagPresent =
                (loadOption(WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION, WSDL2JavaConstants.CODEGEN_SYNC_ONLY_OPTION_LONG, optionMap) != null);
        if (asyncFlagPresent) {
            config.setAsyncOn(true);
            config.setSyncOn(false);
        }
        if (syncFlagPresent) {
            config.setAsyncOn(false);
            config.setSyncOn(true);
        }

        CommandLineOption packageOption = loadOption(WSDL2JavaConstants.PACKAGE_OPTION, WSDL2JavaConstants.PACKAGE_OPTION_LONG, optionMap);
        if (packageOption != null) {
            config.setPackageName(packageOption.getOptionValue());
        }

        CommandLineOption langOption = loadOption(WSDL2JavaConstants.STUB_LANGUAGE_OPTION, WSDL2JavaConstants.STUB_LANGUAGE_OPTION_LONG, optionMap);
        //The language here
        if (langOption != null) {
            config.setOutputLanguage(langOption.getOptionValue());
        }

        CommandLineOption dataBindingOption = loadOption(WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION, WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION_LONG, optionMap);
        if (dataBindingOption != null) {
            config.setDatabindingType(dataBindingOption.getOptionValue());
        }

        CommandLineOption unwrapClassesOption = loadOption(WSDL2JavaConstants.UNPACK_CLASSES_OPTION, WSDL2JavaConstants.UNPACK_CLASSES_OPTION_LONG, optionMap);
        if (unwrapClassesOption != null) {
            config.setPackClasses(false);
        }

        CommandLineOption portNameOption = loadOption(WSDL2JavaConstants.PORT_NAME_OPTION, WSDL2JavaConstants.PORT_NAME_OPTION_LONG, optionMap);
        config.setPortName(portNameOption != null ? portNameOption.getOptionValue() : null);

        CommandLineOption serviceNameOption = loadOption(WSDL2JavaConstants.SERVICE_NAME_OPTION, WSDL2JavaConstants.SERVICE_NAME_OPTION_LONG, optionMap);
        config.setServiceName(serviceNameOption != null ? serviceNameOption.getOptionValue() : null);

        CommandLineOption repositoryPathOption = loadOption(WSDL2JavaConstants.REPOSITORY_PATH_OPTION, WSDL2JavaConstants.REPOSITORY_PATH_OPTION_LONG, optionMap);
        config.setRepositoryPath(repositoryPathOption != null ? repositoryPathOption.getOptionValue() : null);

        CommandLineOption serverSideInterfaceOption = loadOption(WSDL2JavaConstants.REPOSITORY_PATH_OPTION, WSDL2JavaConstants.REPOSITORY_PATH_OPTION_LONG, optionMap);
        if (serverSideInterfaceOption != null){
            config.setServerSideInterface(true);
        }

        CommandLineOption generateAllOption = loadOption(WSDL2JavaConstants.GENERATE_ALL_OPTION, WSDL2JavaConstants.GENERATE_ALL_OPTION_LONG, optionMap);
        if (generateAllOption != null) {
            config.setGenerateAll(true);
        }

        CommandLineOption ns2packageOption = loadOption(
                WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION,
                WSDL2JavaConstants.NAME_SPACE_TO_PACKAGE_OPTION_LONG,
                optionMap);
        if (ns2packageOption != null) {
            //the syntax for the value of the namespaces and packages is
            //to be a comma seperated list with uri=packagename,uri=packagename...
            String value = ns2packageOption.getOptionValue();
            if (value != null) {
                // Try treating the values as a name=value pair separated by comma's
                if (value.indexOf('=') != -1) {
                    String valuepairs[] = value.split(",");
                    if (valuepairs.length > 0) {
                        //put them in the hash map
                        HashMap map = new HashMap(valuepairs.length);
                        for (int i = 0; i < valuepairs.length; i++) {
                            String values[] = valuepairs[i].split("=");
                            if (values.length == 2) {
                                map.put(values[0], values[1]);
                            }
                        }
                        config.setUri2PackageNameMap(map);
                    }
                } else {
                    // Try loading the properties from the file specified
                    try {
                        Properties p = new Properties();
                        p.load(new FileInputStream(value));
                        config.setUri2PackageNameMap(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to load file :" + value, e);
                    }
                }
            }
        }

        //loop through the map and find parameters having the extra prefix.
        //put them in the property map
        Iterator keyIterator = optionMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            Object key = keyIterator.next();
            CommandLineOption option = (CommandLineOption) optionMap.get(key);
            if (key.toString().startsWith(WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX)) {
                //add this to the property map
                config.getProperties().put(key.toString().replaceFirst(WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX, ""), option.getOptionValue());
            }
        }


    }

    private static CommandLineOption loadOption(String shortOption, String longOption, Map options) {
        //short option gets precedence
        CommandLineOption option = null;
        if (longOption != null) {
            option = (CommandLineOption) options.get(longOption);
            if (option != null) {
                return option;
            }
        }
        if (shortOption != null) {
            option = (CommandLineOption) options.get(shortOption);
        }

        return option;
    }

}
