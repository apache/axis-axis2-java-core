package org.apache.axis2.tool.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CommandLineOption;
import org.apache.axis2.wsdl.codegen.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.URLProcessor;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.wsdl.WSDLDescription;

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
public class AntCodegenTask extends Task{

    private String WSDLFileName=null;
    private String output=".";
    private String packageName=URLProcessor.DEFAULT_PACKAGE;
    private String language=CommandLineOptionConstants.LanguageNames.JAVA;

    private boolean asyncOnly=false;
    private boolean syncOnly=false;
    private boolean serverSide=false;
    private boolean testcase=false;
    private boolean generateServerXml=false;

    /**
     *
     */
    private Map fillOptionMap() {
        Map optionMap = new HashMap();

        optionMap.put(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                        getStringArray(WSDLFileName)));

        if (asyncOnly) {
            optionMap
                    .put(
                            CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION,
                            new CommandLineOption(
                                    CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION,
                                    new String[0]));
        }
        if (syncOnly) {
            optionMap
                    .put(
                            CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION,
                            new CommandLineOption(
                                    CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION,
                                    new String[0]));
        }
        optionMap.put(CommandLineOptionConstants.PACKAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.PACKAGE_OPTION,
                        getStringArray(packageName)));
        optionMap.put(CommandLineOptionConstants.STUB_LANGUAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.STUB_LANGUAGE_OPTION,
                        getStringArray(language)));
        optionMap.put(CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                        getStringArray(output)));
        if (serverSide) {
            optionMap.put(CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                            new String[0]));

            if (generateServerXml) {
                optionMap.put(
                        CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                        new CommandLineOption(
                                CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                                new String[0]));
            }
        }
        if (testcase){
            optionMap
                    .put(
                            CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
                            new CommandLineOption(
                                    CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
                                    new String[0]));
        }
        //System.out.println(page3.getOutputLocation());
        return optionMap;
    }

    private WSDLDescription getWOM(String wsdlLocation) throws WSDLException ,
            IOException {
        InputStream in = new FileInputStream(new File(wsdlLocation));
        WSDLVersionWrapper wsdlvWrap =  WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(in);
        return wsdlvWrap.getDescription();
    }

    private String[] getStringArray(String value) {
        String[] values = new String[1];
        values[0] = value;
        return values;
    }


    public void execute() throws BuildException {
        try {
            WSDLDescription wom = this.getWOM(WSDLFileName);
            Map optionsMap = fillOptionMap();
            CodeGenConfiguration codegenConfig = new CodeGenConfiguration(wom,
                    optionsMap);
            new CodeGenerationEngine(codegenConfig).generate();
        } catch (Exception e) {
            throw new BuildException(e);
        }

    }

    public void setWSDLFileName(String WSDLFileName) {
        this.WSDLFileName = WSDLFileName;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAsyncOnly(boolean asyncOnly) {
        this.asyncOnly = asyncOnly;
    }

    public void setSyncOnly(boolean syncOnly) {
        this.syncOnly = syncOnly;
    }

    public void setServerSide(boolean serverSide) {
        this.serverSide = serverSide;
    }

    public void setTestcase(boolean testcase) {
        this.testcase = testcase;
    }

    public void setGenerateServerXml(boolean generateServerXml) {
        this.generateServerXml = generateServerXml;
    }


}
