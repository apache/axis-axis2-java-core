package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.builder.Java2WSDLBuilder;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.CommandLineOption;

import java.util.Map;
import java.io.File;
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

    public Java2WSDLCodegenEngine(Map optionsMap) throws CodeGenerationException {


        //create a new  Java2WSDLBuilder and populate it
        File outputFolder = null;

        CommandLineOption option = loadOption(Java2WSDLConstants.OUTPUT_LOCATION_OPTION,
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION_LONG,optionsMap);
        String outputFolderName =  option ==null?System.getProperty("user.dir"):option.getOptionValue();


        outputFolder = new File(outputFolderName);
        if(!outputFolder.isDirectory()){
            throw new CodeGenerationException(""); //todo put the messages here - not a folder
        }

        option = loadOption(Java2WSDLConstants.CLASSNAME_OPTION,Java2WSDLConstants.CLASSNAME_OPTION_LONG,optionsMap);
        String className =option==null?null:option.getOptionValue();
        if (className==null || className.equals("")){
             throw new CodeGenerationException(""); //todo put the messages here - a class is a must
        }

        option = loadOption(Java2WSDLConstants.OUTPUT_FILENAME_OPTION,
                Java2WSDLConstants.OUTPUT_FILENAME_OPTION_LONG,optionsMap);
        String outputFileName = option==null?null:option.getOptionValue();
        //derive a file name from the class name
        if (outputFileName==null){
         outputFileName = getSimpleClassName(className);
        }

        //first create a file in the given location
        File outputFile = new File(outputFolder,outputFileName);

        //if the class path is present, create a URL class loader with those
        //class path entries present. if not just take the


    }

    public void generate() throws CodeGenerationException {

    }


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
