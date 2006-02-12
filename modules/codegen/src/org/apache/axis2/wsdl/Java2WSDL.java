package org.apache.axis2.wsdl;

import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.Java2WSDLCodegenEngine;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.util.Java2WSDLOptionsValidator;
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
            System.out.println(CodegenMessages.getMessage("java2wsdl.generalError") + e.getMessage());
        }
    }

    public static void printUsage(){
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg1"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg2"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg3"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg4"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg5"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg6"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg7"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg8"));
        System.out.println(CodegenMessages.getMessage("java2wsdl.arg9"));
      

        System.exit(0);
    }


    private static void validateCommandLineOptions(
            CommandLineOptionParser parser) {
        if (parser.getInvalidOptions(new Java2WSDLOptionsValidator()).size() > 0){
            printUsage();
        }

    }

}
