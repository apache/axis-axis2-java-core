package org.apache.axis2.tools.bean;

import org.apache.axis2.wsdl.codegen.*;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.util.URLProcessor;
import org.apache.wsdl.WSDLDescription;

import javax.wsdl.WSDLException;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 21, 2005
 * Time: 2:41:26 PM
 */
public class CodegenBean {
    private String WSDLFileName = null;
    private String output = "E:/CD";
    private String packageName = URLProcessor.DEFAULT_PACKAGE;
    private String language = CommandLineOptionConstants.LanguageNames.JAVA;

    private boolean asyncOnly = false;
    private boolean syncOnly = false;
    private boolean serverSide = false;
    private boolean testcase = false;
    private boolean generateServerXml = false;
    /**
     *
     */
    public Map fillOptionMap(boolean isAyncOnly,
                             boolean isSyncOnly,
                             boolean isServerSide,
                             boolean isServerXML,
                             boolean isTestCase,
                             String WSDLFileName,
                             String packageName,
                             String selectedLanguage,
                             String outputLocation) {
        Map optionMap = new HashMap();
        //WSDL file name
        optionMap.put(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                        getStringArray(WSDLFileName)));

        //Async only
        if (isAyncOnly) {
            optionMap.put(CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION,
                            new String[0]));
        }
        //sync only
        if (isSyncOnly) {
            optionMap.put(CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION,
                            new String[0]));
        }
        //serverside
        if (isServerSide) {
            optionMap.put(CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                            new String[0]));
            //server xml
            if (isServerXML) {
                optionMap.put(
                        CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                        new CommandLineOption(
                                CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                                new String[0]));
            }
        }
        //test case
        if (isTestCase) {
            optionMap.put(CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
                            new String[0]));
        }
        //package name
        optionMap.put(CommandLineOptionConstants.PACKAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.PACKAGE_OPTION,
                        getStringArray(packageName)));
        //selected language
        optionMap.put(CommandLineOptionConstants.STUB_LANGUAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.STUB_LANGUAGE_OPTION,
                        getStringArray(selectedLanguage)));
        //output location
        optionMap.put(CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                        getStringArray(outputLocation)));

        // System.out.println(page3.getOutputLocation());
       optionMap.put(CommandLineOptionConstants.DATA_BINDING_TYPE_OPTION, new CommandLineOption(
               CommandLineOptionConstants.DATA_BINDING_TYPE_OPTION, getStringArray(0+"")));
        return optionMap;
    }


    private String[] getStringArray(String value) {
        String[] values = new String[1];
        values[0] = value;
        return values;
    }

    public WSDLDescription getWOM(String wsdlLocation) throws WSDLException,
            IOException {
        InputStream in = new FileInputStream(new File(wsdlLocation));
        return WOMBuilderFactory.getBuilder(WSDLConstants.WSDL_1_1).build(in).getDescription();
    }

    public void execute() throws Exception {
        Map optionsMap = fillOptionMap(
                isAsyncOnly(),
                isSyncOnly(),
                isServerSide(),
                isGenerateServerXml(),
                isTestcase(),
                getWSDLFileName(),
                getPackageName(),
                getLanguage(),
                getOutput());
        CommandLineOptionParser parser = new CommandLineOptionParser(optionsMap);
        CodeGenerationEngine codegen = new CodeGenerationEngine(parser);
        codegen.generate();
    }

    public String getWSDLFileName() {
        return WSDLFileName;
    }

    public void setWSDLFileName(String WSDLFileName) {
        this.WSDLFileName = WSDLFileName;
    }

    public boolean isSyncOnly() {
        return syncOnly;
    }

    public void setSyncOnly(boolean syncOnly) {
        this.syncOnly = syncOnly;
    }

    public boolean isAsyncOnly() {
        return asyncOnly;
    }

    public void setAsyncOnly(boolean asyncOnly) {
        this.asyncOnly = asyncOnly;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    public void setServerSide(boolean serverSide) {
        this.serverSide = serverSide;
    }

    public boolean isGenerateServerXml() {
        return generateServerXml;
    }

    public void setGenerateServerXml(boolean generateServerXml) {
        this.generateServerXml = generateServerXml;
    }

    public boolean isTestcase() {
        return testcase;
    }

    public void setTestcase(boolean testcase) {
        this.testcase = testcase;
    }

}
