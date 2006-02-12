package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.builder.Java2WSDLBuilder;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;

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
 */

public class Java2WSDLCodegenEngine implements CommandLineOptionConstants {

    private Java2WSDLBuilder java2WsdlBuilder;
    public static final String WSDL_FILENAME_SUFFIX = ".wsdl";

    public Java2WSDLCodegenEngine(Map optionsMap) throws CodeGenerationException {
        //create a new  Java2WSDLBuilder and populate it
        File outputFolder;

        CommandLineOption option = loadOption(Java2WSDLConstants.OUTPUT_LOCATION_OPTION,
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION_LONG,optionsMap);
        String outputFolderName =  option ==null?System.getProperty("user.dir"):option.getOptionValue();


        outputFolder = new File(outputFolderName);
        if (!outputFolder.exists()){
            outputFolder.mkdir();
        }else  if(!outputFolder.isDirectory()){
            throw new CodeGenerationException(CodegenMessages.getMessage("java2wsdl.notAFolder",outputFolderName));
        }

        option = loadOption(Java2WSDLConstants.CLASSNAME_OPTION,Java2WSDLConstants.CLASSNAME_OPTION_LONG,optionsMap);
        String className =option==null?null:option.getOptionValue();

        if (className==null || className.equals("")){
            throw new CodeGenerationException(CodegenMessages.getMessage("java2wsdl.classIsMust"));
        }

        option = loadOption(Java2WSDLConstants.OUTPUT_FILENAME_OPTION,
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION_LONG,optionsMap);
        String outputFileName = option==null?null:option.getOptionValue();
        //derive a file name from the class name if the filename is not specified
        if (outputFileName==null){
            outputFileName = getSimpleClassName(className)+WSDL_FILENAME_SUFFIX;
        }

        //first create a file in the given location
        File outputFile = new File(outputFolder,outputFileName);
        FileOutputStream out;
        try {
            if (!outputFile.exists()){
                outputFile.createNewFile();
            }
            out = new FileOutputStream(outputFile);
        } catch (IOException e) {
            throw new CodeGenerationException(e);
        }

        //if the class path is present, create a URL class loader with those
        //class path entries present. if not just take the  TCCL
        option = loadOption(Java2WSDLConstants.CLASSPATH_OPTION,
                Java2WSDLConstants.CLASSPATH_OPTION_LONG,optionsMap);

        ClassLoader classLoader;

        if (option != null){
            ArrayList optionValues = option.getOptionValues();
            URL[] urls= new URL[optionValues.size()];
            String[] classPathEntries = (String[])optionValues.toArray(new String[optionValues.size()]);

            try {
                for (int i = 0; i < classPathEntries.length; i++) {
                    String classPathEntry = classPathEntries[i];
                    //this should be a file(or a URL)
                    if (isURL(classPathEntry)) {
                        urls[i] = new URL(classPathEntry);
                    }else{
                        urls[i]= new File(classPathEntry).toURL();
                    }
                }
            } catch (MalformedURLException e) {
                throw new CodeGenerationException(e);
            }

            classLoader = new URLClassLoader(urls,Thread.currentThread().getContextClassLoader());

        }else{
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        //Now we are done with loading the basic values - time to create the builder
       java2WsdlBuilder = new Java2WSDLBuilder(out,
                                               className,
                                               classLoader);


        //set the other parameters to the builder
        option  = loadOption(Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION_LONG,optionsMap);
        java2WsdlBuilder.setSchemaTargetNamespace(option==null?null:option.getOptionValue());

        option  = loadOption(Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG,optionsMap);
        java2WsdlBuilder.setSchemaTargetNamespacePrefix(option==null?null:option.getOptionValue());

        option  = loadOption(Java2WSDLConstants.TARGET_NAMESPACE_OPTION,
                Java2WSDLConstants.TARGET_NAMESPACE_OPTION_LONG,optionsMap);
        java2WsdlBuilder.setTargetNamespace(option==null?null:option.getOptionValue());

        option  = loadOption(Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION,
                Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION_LONG,optionsMap);
        java2WsdlBuilder.setTargetNamespacePrefix(option==null?null:option.getOptionValue());

          option  = loadOption(Java2WSDLConstants.SERVICE_NAME_OPTION,
                Java2WSDLConstants.SERVICE_NAME_OPTION_LONG,optionsMap);
        java2WsdlBuilder.setServiceName(option==null?getSimpleClassName(className):option.getOptionValue());

    }

    public void generate() throws CodeGenerationException {
        try {
            java2WsdlBuilder.generateWSDL();
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * check the entry for a URL. This is a simple check and need to be improved
     * @param entry
     * @return
     */

    private boolean isURL(String entry){
          return entry.startsWith("http://");
    }
    /**
     *
     * @param shortOption
     * @param longOption
     * @param options
     * @return
     */
    private  CommandLineOption loadOption(String shortOption, String longOption,Map options) {
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

    /**
     * A method to strip the fully qualified className to a simple classname
     * @param qualifiedName
     * @return
     */
    private String getSimpleClassName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index > 0) {
            return qualifiedName.substring(index + 1, qualifiedName.length());
        }
        return qualifiedName;
    }
}
