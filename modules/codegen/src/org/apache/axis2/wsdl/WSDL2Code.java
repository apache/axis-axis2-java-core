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

package org.apache.axis2.wsdl;

import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CommandLineOptionConstants;
import org.apache.axis2.wsdl.codegen.CommandLineOptionParser;
import org.apache.axis2.wsdl.i18n.CodegenMessages;

public class WSDL2Code {


    public static void main(String[] args) throws Exception {
        CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(
                args);
        validateCommandLineOptions(commandLineOptionParser);
        new CodeGenerationEngine(commandLineOptionParser).generate();

    }

    private static void printUsage() {
        
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg1"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg2"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg3"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg4"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg5"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg6"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg7"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg8"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg9"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg10"));

        System.exit(0);
    }


    private static void validateCommandLineOptions(
            CommandLineOptionParser parser) {
        if (parser.getInvalidOptions().size() > 0)
            printUsage();
        if (null ==
                parser.getAllOptions().get(
                        CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION))
            printUsage();
    }


}
