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
package org.apache.axis2.tool.codegen;

import org.apache.axis2.tool.codegen.eclipse.util.UIConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.axis2.wsdl.codegen.CommandLineOption;
import org.apache.axis2.wsdl.codegen.CommandLineOptionConstants;
import org.apache.wsdl.WSDLDescription;

import javax.wsdl.WSDLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class WSDL2JavaGenerator {
    
    /**
     * Maps a string containing the name of a language to a constant defined in CommandLineOptionConstants.LanguageNames
     * 
     * @param UILangValue a string containg a language, e.g. "java", "cs", "cpp" or "vb"
     * @return a normalized string constant
     */
    private String mapLanguagesWithCombo(String UILangValue)
    {
       if (UIConstants.JAVA.equals(UILangValue))
       {
          return CommandLineOptionConstants.LanguageNames.JAVA;
       }
       else if (UIConstants.C_SHARP.equals(UILangValue))
       {
          return CommandLineOptionConstants.LanguageNames.C_SHARP;
       }
       else if (UIConstants.C_PLUS_PLUS.equals(UILangValue))
       {
          return CommandLineOptionConstants.LanguageNames.C_PLUS_PLUS;
       }
       else
       {
          return null;
       }
    }
    /**
     * Creates a list of parameters for the code generator based on the decisions made by the user on the OptionsPage
     * (page2). For each setting, there is a Command-Line option for the Axis2 code generator.
     * 
     * @return a Map with keys from CommandLineOptionConstants with the values entered by the user on the Options Page.
     */
    public Map fillOptionMap(boolean isAyncOnly,
            		  boolean isSyncOnly,
            		  boolean isServerSide,
            		  boolean isServerXML,
            		  boolean isTestCase,
            		  String WSDLURI,
            		  String packageName,
            		  String selectedLanguage,
            		  String outputLocation
            		  )
    {
       Map optionMap = new HashMap();
       //WSDL file name
       optionMap.put(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION, new CommandLineOption(
          CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION, getStringArray(WSDLURI)));
       
       //Async only
       if (isAyncOnly)
       {
          optionMap.put(CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION, new CommandLineOption(
             CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION, new String[0]));
       }
       //sync only
       if (isSyncOnly)
       {
          optionMap.put(CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION, new CommandLineOption(
             CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION, new String[0]));
       }
       //serverside
       if (isServerSide)
       {
          optionMap.put(CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION, new CommandLineOption(
             CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION, new String[0]));
          //server xml
          if (isServerXML)
          {
             optionMap.put(CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION, new CommandLineOption(
                CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION, new String[0]));
          }
       }
       //test case
       if (isTestCase)
       {
          optionMap.put(CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION, new CommandLineOption(
             CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION, new String[0]));
       }
       //package name
       optionMap.put(CommandLineOptionConstants.PACKAGE_OPTION, new CommandLineOption(
          CommandLineOptionConstants.PACKAGE_OPTION, getStringArray(packageName)));
       //selected language
       optionMap.put(CommandLineOptionConstants.STUB_LANGUAGE_OPTION, new CommandLineOption(
          CommandLineOptionConstants.STUB_LANGUAGE_OPTION, getStringArray(mapLanguagesWithCombo(selectedLanguage))));
       //output location
       optionMap.put(CommandLineOptionConstants.OUTPUT_LOCATION_OPTION, new CommandLineOption(
          CommandLineOptionConstants.OUTPUT_LOCATION_OPTION, getStringArray(outputLocation)));
       
       //data binding constant
       // #########################################################################################
       // This is set to the default NONE option since eclipse cannot load the XBeans thingy
       //###########################################################################################
       optionMap.put(CommandLineOptionConstants.DATA_BINDING_TYPE_OPTION, new CommandLineOption(
               CommandLineOptionConstants.DATA_BINDING_TYPE_OPTION, getStringArray("none")));
       return optionMap;
    }
    /**
     * Reads the WSDL Object Model from the given location.
     * 
     * @param wsdlURI the filesystem location (full path) of the WSDL file to read in.
     * @return the WSDLDescription object containing the WSDL Object Model of the given WSDL file
     * @throws WSDLException when WSDL File is invalid
     * @throws IOException on errors reading the WSDL file
     */
    public WSDLDescription getWOM(String wsdlURI) throws WSDLException, IOException
    {
       try {
           InputStream in = null;
           if (wsdlURI.startsWith("http")){
              in = new URL(wsdlURI).openStream();
           }else{
               //treat the uri as a file name
               in = new FileInputStream(new File(wsdlURI)); 
           }
         
        return WOMBuilderFactory.getBuilder(WSDLConstants.WSDL_1_1).build(in).getDescription();
    } catch (FileNotFoundException e) {
       throw e;
    } catch (WSDLException e) {
       throw e;
    } catch (Exception e){
        e.printStackTrace();
        throw new RuntimeException(e);
    }
    
    }

    /**
     * Converts a single String into a String Array
     * 
     * @param value a single string
     * @return an array containing only one element
     */
    private String[] getStringArray(String value)
    {
       String[] values = new String[1];
       values[0] = value;
       return values;
    }
}
