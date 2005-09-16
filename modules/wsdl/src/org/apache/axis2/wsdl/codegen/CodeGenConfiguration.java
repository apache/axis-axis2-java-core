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

package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.wsdl.WSDLDescription;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CodeGenConfiguration implements CommandLineOptionConstants {

    private WSDLDescription wom;
    private CommandLineOptionParser parser;
    private File outputLocation;
    private String outputLanguage = ConfigPropertyFileLoader.getDefaultLanguage();
    private int databindingType = XSLTConstants.DataBindingTypes.XML_BEANS; //default is XML beans
    private boolean advancedCodeGenEnabled = false;


    private boolean asyncOn = true;
    private boolean syncOn = true;
    private boolean serverSide = false;
    private boolean generateDeployementDescriptor = true;
    private boolean writeTestCase = false;
    private boolean writeMessageReceiver = true;
    private String packageName = XSLTConstants.DEFAULT_PACKAGE_NAME;

    /* Code generation style means whether to use the binding or the interface for code generation.
    * the default is automatic where the code generator looks for the binding and if the binding is
    * absent, switches to the interface. The user however, can switch to the interface or the binding
    * modes explicitly by specifying this parameter
    */
    private  int codeGenerationStyle = XSLTConstants.CodegenStyle.AUTOMATIC;

    /*
    * A hashmap of properties that may be populated on the way. extensions can populate it
    *
    */
    private Map configurationProperties = new HashMap();

    public void put(Object key, Object value){
        configurationProperties.put(key,value);
    }

    public Object get(Object key){
       return configurationProperties.get(key);
    }

    public Map getProperties(){
        return configurationProperties;
    }
    private TypeMapper typeMapper;


    public int getCodeGenerationStyle() {
        return codeGenerationStyle;
    }

    public void setCodeGenerationStyle(int codeGenerationStyle) {
        this.codeGenerationStyle = codeGenerationStyle;
    }

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

    public void setDatabindingType(String databindingType) {
        if (Databinding.XML_BEANS.equalsIgnoreCase(databindingType)) {
            this.databindingType = XSLTConstants.DataBindingTypes.XML_BEANS;
        }else if(Databinding.JAXB.equalsIgnoreCase(databindingType)){
            this.databindingType = XSLTConstants.DataBindingTypes.JAXB;
        }else{
             this.databindingType = XSLTConstants.DataBindingTypes.NONE;
//            throw new UnsupportedOperationException();
        }
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

        CommandLineOption clo =
                ((CommandLineOption)optionMap.get(OUTPUT_LOCATION_OPTION));
        if (clo == null) {
            throw new RuntimeException("Cannot codegenerate! WSDL file is missing!");
        }
        String outputLocation = clo.getOptionValue();
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
        //The language here
        if (langOption != null) {
            outputLanguage = langOption.getOptionValue();
        }

// Unused code commented out by gdaniels...
        CommandLineOption dataBindingOption = (CommandLineOption) optionMap.get(
                DATA_BINDING_TYPE_OPTION);
        if(dataBindingOption != null){
            setDatabindingType(dataBindingOption.getOptionValue());
        }
    }


//    private void loadLanguge(String langName) {
//        if (LanguageNames.JAVA.equalsIgnoreCase(langName)) {
//            this.outputLanguage = XSLTConstants.LanguageTypes.JAVA;
//        } else if (LanguageNames.C_SHARP.equalsIgnoreCase(langName)) {
//            this.outputLanguage = XSLTConstants.LanguageTypes.C_SHARP;
//        } else if (LanguageNames.C_PLUS_PLUS.equalsIgnoreCase(langName)) {
//            this.outputLanguage = XSLTConstants.LanguageTypes.C_PLUS_PLUS;
//        } else if (LanguageNames.VB_DOT_NET.equalsIgnoreCase(langName)) {
//            this.outputLanguage = XSLTConstants.LanguageTypes.VB_DOT_NET;
//        }
//    }

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

    public String getOutputLanguage() {
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