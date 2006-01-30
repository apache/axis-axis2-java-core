package org.apache.axis2.wsdl;

import org.apache.axis2.wsdl.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.util.Java2WSDLOptionsValidator;
import org.apache.axis2.wsdl.util.WSDL2JavaOptionsValidator;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.codegen.Java2WSDLCodegenEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
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

public class Java2WSDL {

    public static void main(String[] args) {
        CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(
                args);
        //  validate the arguments
        validateCommandLineOptions(commandLineOptionParser);
        try {
            new Java2WSDLCodegenEngine(commandLineOptionParser.getAllOptions()).generate();
        } catch (CodeGenerationException e) {
            e.printStackTrace();
            System.out.println("An error occured during codegen");//todo i18n this
        }
    }

    public static void printUsage(){
        //todo fill this
    }


    private static void validateCommandLineOptions(
               CommandLineOptionParser parser) {
           if (parser.getInvalidOptions(new Java2WSDLOptionsValidator()).size() > 0)
               printUsage();

       }

}
