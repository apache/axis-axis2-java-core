package org.apache.ws.java2wsdl;

import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
*
*/

public class Java2WSDLCodegenEngine implements Java2WSDLConstants {
    private Java2WSDLBuilder java2WsdlBuilder;
    public static final String WSDL_FILENAME_SUFFIX = ".wsdl";

    public Java2WSDLCodegenEngine(Map optionsMap) throws Exception {
        //create a new  Java2WSDLBuilder and populate it
        File outputFolder;

        Java2WSDLCommandLineOption option = loadOption(Java2WSDLConstants.OUTPUT_LOCATION_OPTION,
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION_LONG, optionsMap);
        String outputFolderName = option == null ? System.getProperty("user.dir") : option.getOptionValue();


        outputFolder = new File(outputFolderName);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        } else if (!outputFolder.isDirectory()) {
            throw new Exception("The specivied location " + outputFolderName + "is not a folder");
        }

        option = loadOption(Java2WSDLConstants.CLASSNAME_OPTION, Java2WSDLConstants.CLASSNAME_OPTION_LONG, optionsMap);
        String className = option == null ? null : option.getOptionValue();

        if (className == null || className.equals("")) {
            throw new Exception("class name must be present!");
        }

        option = loadOption(Java2WSDLConstants.OUTPUT_FILENAME_OPTION,
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION_LONG, optionsMap);
        String outputFileName = option == null ? null : option.getOptionValue();
        //derive a file name from the class name if the filename is not specified
        if (outputFileName == null) {
            outputFileName = Java2WSDLUtils.getSimpleClassName(className) + WSDL_FILENAME_SUFFIX;
        }

        //first create a file in the given location
        File outputFile = new File(outputFolder, outputFileName);
        FileOutputStream out;
        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            out = new FileOutputStream(outputFile);
        } catch (IOException e) {
            throw new Exception(e);
        }

        //if the class path is present, create a URL class loader with those
        //class path entries present. if not just take the  TCCL
        option = loadOption(Java2WSDLConstants.CLASSPATH_OPTION,
                Java2WSDLConstants.CLASSPATH_OPTION_LONG, optionsMap);

        ClassLoader classLoader;

        if (option != null) {
            ArrayList optionValues = option.getOptionValues();
            URL[] urls = new URL[optionValues.size()];
            String[] classPathEntries = (String[]) optionValues.toArray(new String[optionValues.size()]);

            try {
                for (int i = 0; i < classPathEntries.length; i++) {
                    String classPathEntry = classPathEntries[i];
                    //this should be a file(or a URL)
                    if (Java2WSDLUtils.isURL(classPathEntry)) {
                        urls[i] = new URL(classPathEntry);
                    } else {
                        urls[i] = new File(classPathEntry).toURL();
                    }
                }
            } catch (MalformedURLException e) {
                throw new Exception(e);
            }

            classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

        } else {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        //Now we are done with loading the basic values - time to create the builder
        java2WsdlBuilder = new Java2WSDLBuilder(out,
                className,
                classLoader);

        //set the other parameters to the builder
        option = loadOption(Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setSchemaTargetNamespace(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setSchemaTargetNamespacePrefix(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.TARGET_NAMESPACE_OPTION,
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setTargetNamespace(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION,
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setTargetNamespacePrefix(option == null ? null : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.SERVICE_NAME_OPTION,
                Java2WSDLConstants.SERVICE_NAME_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setServiceName(option == null ? Java2WSDLUtils.getSimpleClassName(className) : option.getOptionValue());

        option = loadOption(Java2WSDLConstants.STYLE_OPTION,
                Java2WSDLConstants.STYLE_OPTION, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setStyle(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.LOCATION_OPTION,
                Java2WSDLConstants.LOCATION_OPTION, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setLocationUri(option.getOptionValue());
        }

        option = loadOption(Java2WSDLConstants.USE_OPTION,
                Java2WSDLConstants.USE_OPTION, optionsMap);
        if (option != null) {
            java2WsdlBuilder.setUse(option.getOptionValue());
        }
        
        option = loadOption(Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION,
                Java2WSDLConstants.ATTR_FORM_DEFAULT_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setAttrFormDefault(option == null ? null : option.getOptionValue());
        
        option = loadOption(Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION,
                Java2WSDLConstants.ELEMENT_FORM_DEFAULT_OPTION_LONG, optionsMap);
        java2WsdlBuilder.setElementFormDefault(option == null ? null : option.getOptionValue());
        


    }

    public void generate() throws Exception {
        try {
            java2WsdlBuilder.generateWSDL();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

     /**
     * @param shortOption
     * @param longOption
     * @param options
     */
    private Java2WSDLCommandLineOption loadOption(String shortOption, String longOption, Map options) {
        //short option gets precedence
        Java2WSDLCommandLineOption option = null;
        if (longOption != null) {
            option = (Java2WSDLCommandLineOption) options.get(longOption);
            if (option != null) {
                return option;
            }
        }
        if (shortOption != null) {
            option = (Java2WSDLCommandLineOption) options.get(shortOption);
        }

        return option;
    }
}
