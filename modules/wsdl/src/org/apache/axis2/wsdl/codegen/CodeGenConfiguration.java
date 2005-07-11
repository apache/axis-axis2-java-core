/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.wsdl.WSDLDescription;

import java.io.File;
import java.util.Map;

/**
 * @author chathura@opensource.lk
 */
public class CodeGenConfiguration implements CommandLineOptionConstants {

    private WSDLDescription wom;
    private CommandLineOptionParser parser;
    private File outputLocation;
    private int outputLanguage = XSLTConstants.LanguageTypes.JAVA;
    private int databindingType = XSLTConstants.DataBindingTypes.XML_BEANS; //default is XML beans
    private boolean advancedCodeGenEnabled = false;


    private boolean asyncOn = true;
    private boolean syncOn = true;
    private boolean serverSide = false;
    private boolean generateDeployementDescriptor = true;
    private boolean writeTestCase = false;
    private boolean writeMessageReceiver = true;
    private String packageName = XSLTConstants.DEFAULT_PACKAGE_NAME;


    private TypeMapper typeMapper;

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public void setTypeMapper(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public int getDatabindingType() {
        return databindingType;
    }

    public void setDatabindingType(int databindingType) {
        this.databindingType = databindingType;
    }

    /**
     * @param wom
     * @param parser
     */
    public CodeGenConfiguration(WSDLDescription wom,
                                CommandLineOptionParser parser) {
        this(wom, parser.getAllOptions());
        this.parser = parser;
    }

    public CodeGenConfiguration(WSDLDescription wom, Map optionMap) {
        this.wom = wom;

        String outputLocation = ((CommandLineOption) optionMap.get(
                OUTPUT_LOCATION_OPTION)).getOptionValue();
        this.outputLocation = new File(outputLocation);

        serverSide = (optionMap.get(SERVER_SIDE_CODE_OPTION) != null);
        generateDeployementDescriptor = (optionMap.get(
                GENERATE_SERVICE_DESCRIPTION_OPTION) !=
                null);
        writeTestCase = (optionMap.get(GENERATE_TEST_CASE_OPTION) != null);

        boolean asyncFlagPresent = (optionMap.get(CODEGEN_ASYNC_ONLY_OPTION) !=
                null);
        boolean syncFlagPresent = (optionMap.get(CODEGEN_SYNC_ONLY_OPTION) !=
                null);
        if (asyncFlagPresent) {
            this.asyncOn = true;
            this.syncOn = false;
        }
        if (syncFlagPresent) {
            this.asyncOn = false;
            this.syncOn = true;
        }

        CommandLineOption packageOption = (CommandLineOption) optionMap.get(
                PACKAGE_OPTION);
        if (packageOption != null) {
            this.packageName = packageOption.getOptionValue();
        }

        CommandLineOption langOption = (CommandLineOption) optionMap.get(
                STUB_LANGUAGE_OPTION);
        if (langOption != null) {
            loadLanguge(langOption.getOptionValue());
        }


    }


    private void loadLanguge(String langName) {
        if (LanguageNames.JAVA.equalsIgnoreCase(langName)) {
            this.outputLanguage = XSLTConstants.LanguageTypes.JAVA;
        } else if (LanguageNames.C_SHARP.equalsIgnoreCase(langName)) {
            this.outputLanguage = XSLTConstants.LanguageTypes.C_SHARP;
        } else if (LanguageNames.C_PLUS_PLUS.equalsIgnoreCase(langName)) {
            this.outputLanguage = XSLTConstants.LanguageTypes.C_PLUS_PLUS;
        } else if (LanguageNames.VB_DOT_NET.equalsIgnoreCase(langName)) {
            this.outputLanguage = XSLTConstants.LanguageTypes.VB_DOT_NET;
        }
    }

    /**
     * @return Returns the parser.
     */
    public CommandLineOptionParser getParser() {
        return parser;
    }

    /**
     * @return Returns the wom.
     */
    public WSDLDescription getWom() {
        return wom;
    }


    /**
     * @return Returns the outputLocation.
     */
    public File getOutputLocation() {
        return outputLocation;
    }

    public int getOutputLanguage() {
        return outputLanguage;
    }

    public boolean isAdvancedCodeGenEnabled() {
        return advancedCodeGenEnabled;
    }


    /**
     * @return Returns the packageName.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param packageName The packageName to set.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public boolean isAsyncOn() {
        return asyncOn;
    }


    public boolean isSyncOn() {
        return syncOn;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    public boolean isGenerateDeployementDescriptor() {
        return generateDeployementDescriptor;
    }

    public boolean isWriteTestCase() {
        return writeTestCase;
    }


    public boolean isWriteMessageReceiver() {
        return writeMessageReceiver;
    }

    public void setWriteMessageReceiver(boolean writeMessageReceiver) {
        this.writeMessageReceiver = writeMessageReceiver;
    }
}