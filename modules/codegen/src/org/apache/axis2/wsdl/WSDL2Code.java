/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.wsdl;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Map;

import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.jaxws.JAXWSCodeGenerationEngine;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.WSDL2JavaOptionsValidator;

public class WSDL2Code {


    public static void main(String[] args) throws Exception {
        CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(
                args);
       checkAuthentication(commandLineOptionParser);  
       setSystemProperties(commandLineOptionParser);
      //If it is a JAX-WS code generation request call WSimportTool.
      if (isJwsOptionEnabled(commandLineOptionParser)){
         new JAXWSCodeGenerationEngine(commandLineOptionParser, args).generate();
         return;
      }
        if (isOptionsValid(commandLineOptionParser)){
            new CodeGenerationEngine(commandLineOptionParser).generate();
        } else {
            printUsage();
        }
    }

    private static void printUsage() {

        System.out.println(CodegenMessages.getMessage("wsdl2code.arg"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg1"));
        for (int i = 2; i <= 53; i++) {
            System.out.println("  " + CodegenMessages.getMessage("wsdl2code.arg" + i));
        }
    }


    private static boolean isOptionsValid(CommandLineOptionParser parser) {
        boolean isValid = true;
        if (parser.getInvalidOptions(new WSDL2JavaOptionsValidator()).size() > 0){
            isValid = false;
        }
        if (null == parser.getAllOptions().get(
                        CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION)){
            isValid = false;
        }
        return isValid;
    }
  
    private static boolean isJwsOptionEnabled(CommandLineOptionParser parser) {
        Map allOptions = parser.getAllOptions();       
        CommandLineOption option = (CommandLineOption) allOptions
                .get(CommandLineOptionConstants.WSDL2JavaConstants.JAX_WS_SERVICE_OPTION);
        if( option == null){
            return false;
        }
        return true;
    }
    
    private static void checkAuthentication(CommandLineOptionParser commandLineOptionParser) {
        
        String userName = null;
        String password = null;
        
        Map allOptions = commandLineOptionParser.getAllOptions();        
        CommandLineOption userOption = (CommandLineOption) allOptions
                .get(CommandLineOptionConstants.WSDL2JavaConstants.HTTP_PROXY_USER_OPTION_LONG);
        CommandLineOption passwordOption = (CommandLineOption) allOptions
                .get(CommandLineOptionConstants.WSDL2JavaConstants.HTTP_PROXY_PASSWORD_OPTION_LONG);
        CommandLineOption urlOption = (CommandLineOption) allOptions
                .get(CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION);
        
        if(urlOption == null){
            return;
        }        
        if (userOption != null) {
            userName = userOption.getOptionValue();
        }
        if (passwordOption != null) {
            password = passwordOption.getOptionValue();

        }
        if (userName == null) {
            // Try to collect user name and password from UserInfo part of the URL. 
            URL url = null;
            try {
                url = new URL(urlOption.getOptionValue());
            } catch (MalformedURLException e) {
                return;
            }

            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                int i = userInfo.indexOf(':');

                if (i >= 0) {
                    userName = userInfo.substring(0, i);
                    password = userInfo.substring(i + 1);
                } else {
                    userName = userInfo;
                }
            }

        }
        
        if (userName != null) {
            final String user = userName;
            final String pass = password == null ? "" : password;
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });
        }
    }
    
    private static void setSystemProperties(CommandLineOptionParser commandLineOptionParser) {
        Map<String, CommandLineOption> allOptions = commandLineOptionParser.getAllOptions();
        // System properties follow "-Dproperty=value" format, only key is required.
        if (allOptions != null) {
            for (String key : allOptions.keySet()) {
                if (key != null
                        && key.length() > 0
                        && key.startsWith(CommandLineOptionConstants.WSDL2JavaConstants.SYSTEM_PROPERTY_PREFIX)
                        && key.contains("=")) {
                    int splitIndex = key.indexOf("=");
                    String pKey = key.substring(1, splitIndex);
                    String pValue = key.substring(splitIndex + 1);
                    if (pKey != null && pValue != null) {
                        System.setProperty(pKey, pValue);
                    }
                }
            }
        }
    }
   
}
