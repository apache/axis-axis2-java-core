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
import java.util.Iterator;

public class CodeGenConfiguration implements CommandLineOptionConstants {

    private WSDLDescription wom;
    private CommandLineOptionParser parser;
    private File outputLocation;

    //get the defaults for these from the property file
    private String outputLanguage = ConfigPropertyFileLoader.getDefaultLanguage();
    private String databindingType = ConfigPropertyFileLoader.getDefaultDBFrameworkName();
    private boolean advancedCodeGenEnabled = false;


    private boolean asyncOn = true;
    private boolean syncOn = true;
    private boolean serverSide = false;
    private boolean generateDeployementDescriptor = true;
    private boolean writeTestCase = false;
    private boolean writeMessageReceiver = true;
    private String packageName = XSLTConstants.DEFAULT_PACKAGE_NAME;

    // Default wrap classes is true, which means the classes generated
    // by default are wrapped. The effect of this setting will be controlled
    // to some extent, by the other settings as well.

    private boolean wrapClasses = true;

    private boolean generateAll = false;


    /**
     * A hashmap to hang the property objects
     */
    private Map policyMap = new HashMap();

    /*
    * A hashmap of properties that may be populated on the way. extensions can populate it
    * This can be used to keep non specific information
    */
    private Map configurationProperties = new HashMap();


    public boolean isGenerateAll() {
        return generateAll;
    }

    public void setGenerateAll(boolean generateAll) {
        this.generateAll = generateAll;
    }

    /**
     * get the wrap classes flag
     *
     * @return
     */
    public boolean isWrapClasses() {
        return wrapClasses;
    }

    /**
     * set the wrap classes flag
     *
     * @param wrapClasses
     */
    public void setWrapClasses(boolean wrapClasses) {
        this.wrapClasses = wrapClasses;
    }

    /**
     * gets the policy map
     *
     * @return
     */
    public Map getPolicyMap() {
        return policyMap;
    }

    /**
     * sets the policy map
     *
     * @param policyMap
     */
    public void setPolicyMap(Map policyMap) {
        this.policyMap = policyMap;
    }

    /* Code generation style means whether to use the binding or the interface for code generation.
    * the default is automatic where the code generator looks for the binding and if the binding is
    * absent, switches to the interface. The user however, can switch to the interface or the binding
    * modes explicitly by specifying this parameter
    */
    private int codeGenerationStyle = XSLTConstants.CodegenStyle.AUTOMATIC;


    /**
     * put a property into the configuration
     *
     * @param key
     * @param value
     */
    public void putProperty(Object key, Object value) {
        configurationProperties.put(key, value);
    }

    /**
     * get the property from the configuration
     *
     * @param key
     * @return
     */
    public Object getProperty(Object key) {
        return configurationProperties.get(key);
    }

    /**
     * Get the whole property object
     *
     * @return
     */
    public Map getProperties() {
        return configurationProperties;
    }

    private TypeMapper typeMapper;

    /**
     * @return
     */
    public int getCodeGenerationStyle() {
        return codeGenerationStyle;
    }

    /**
     * @param codeGenerationStyle
     */
    public void setCodeGenerationStyle(int codeGenerationStyle) {
        this.codeGenerationStyle = codeGenerationStyle;
    }

    /**
     * @return
     */
    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    /**
     * @param typeMapper
     */
    public void setTypeMapper(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    /**
     * @return
     */
    public String getDatabindingType() {
        return databindingType;
    }

    /**
     * @param databindingType
     */
    public void setDatabindingType(String databindingType) {
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

    /**
     * Constructor for the configuration. populate the values using the options map
     *
     * @param wom
     * @param optionMap
     */
    public CodeGenConfiguration(WSDLDescription wom, Map optionMap) {
        this.wom = wom;

        String outputLocation = "."; //default output directory is the current working directory
        CommandLineOption clo =
                ((CommandLineOption) optionMap.get(OUTPUT_LOCATION_OPTION));
        if (clo != null) {
            outputLocation = clo.getOptionValue();
        }
        this.outputLocation = new File(outputLocation);

        //check and create the directories
        if (this.outputLocation.exists()) {
            if (this.outputLocation.isFile()) {
                throw new RuntimeException("The specified output location is not a directory!");
            }
        } else {
            this.outputLocation.mkdirs();
        }

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

        CommandLineOption dataBindingOption = (CommandLineOption) optionMap.get(
                DATA_BINDING_TYPE_OPTION);
        if (dataBindingOption != null) {
            setDatabindingType(dataBindingOption.getOptionValue());
        }

        CommandLineOption unwrapClassesOption = (CommandLineOption) optionMap.get(
                UNWRAP_CLASSES_OPTION);
        if (unwrapClassesOption != null) {
            wrapClasses = false;
        }

        CommandLineOption generateAllOption = (CommandLineOption) optionMap.get(
                GENERATE_ALL_OPTION);
        if (generateAllOption != null) {
            generateAll = true;
        }

        //loop through the map and find parameters having the extra prefix.
        //put them in the property map
        Iterator keyIterator = optionMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            Object key = keyIterator.next();
            CommandLineOption option =  (CommandLineOption)optionMap.get(key);
            if (key.toString().startsWith(EXTRA_OPTIONTYPE_PREFIX)){
                //add this to the property map
                configurationProperties.put(key.toString().replaceFirst("E",""),option.getOptionValue());
            }
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