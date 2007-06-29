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

import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.WSDL2JavaOptionsValidator;

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
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg11"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg12"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg13"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg14"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg15"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg16"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg17"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg18"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg19"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg20"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg21"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg22"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg23"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg24"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg24"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg24"));

        System.exit(0);
    }


    private static void validateCommandLineOptions(
            CommandLineOptionParser parser) {
        if (parser.getInvalidOptions(new WSDL2JavaOptionsValidator()).size() > 0)
            printUsage();
        if (null ==
                parser.getAllOptions().get(
                        CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION))
            printUsage();
    }


}
