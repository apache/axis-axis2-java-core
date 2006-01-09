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

import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.axis2.wsdl.util.XSLTConstants;
import org.apache.wsdl.WSDLDescription;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CodeGenConfiguration implements CommandLineOptionConstants {

    private WSDLDescription wom;

    public void setWom(WSDLDescription wom) {
        this.wom = wom;
    }

    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
    }

    public void setAdvancedCodeGenEnabled(boolean advancedCodeGenEnabled) {
        this.advancedCodeGenEnabled = advancedCodeGenEnabled;
    }

    public void setAsyncOn(boolean asyncOn) {
        this.asyncOn = asyncOn;
    }

    public void setSyncOn(boolean syncOn) {
        this.syncOn = syncOn;
    }

    public void setServerSide(boolean serverSide) {
        this.serverSide = serverSide;
    }

    public void setGenerateDeployementDescriptor(boolean generateDeployementDescriptor) {
        this.generateDeployementDescriptor = generateDeployementDescriptor;
    }

    public void setWriteTestCase(boolean writeTestCase) {
        this.writeTestCase = writeTestCase;
    }

    public void setOutputLocation(File outputLocation) {
        this.outputLocation = outputLocation;
    }

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
    private String packageName = URLProcessor.DEFAULT_PACKAGE;

    // Default packClasses is true, which means the classes generated
    // by default are wrapped. The effect of this setting will be controlled
    // to some extent, by the other settings as well.
    private boolean packClasses = true;

    private boolean generateAll = false;

    //user selected portname
    private String portName;
    //user selected servicename
    private String serviceName;


    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

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
     * Gets the wrap classes flag.
     *
     * @return Returns true if it is a wrap class, else returns false.
     */
    public boolean isPackClasses() {
        return packClasses;
    }

    /**
     * Sets the wrap classes flag.
     *
     * @param packClasses
     */
    public void setPackClasses(boolean packClasses) {
        this.packClasses = packClasses;
    }

    /**
     * Gets the policy map.
     *
     * @return Returns Map.
     */
    public Map getPolicyMap() {
        return policyMap;
    }

    /**
     * Sets the policy map.
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
     * Puts a property into the configuration.
     *
     * @param key
     * @param value
     */
    public void putProperty(Object key, Object value) {
        configurationProperties.put(key, value);
    }

    /**
     * Gets the property from the configuration.
     *
     * @param key
     * @return Returns the property as Object.
     */
    public Object getProperty(Object key) {
        return configurationProperties.get(key);
    }

    /**
     * Gets all property objects.
     *
     * @return Returns Map of all properties.
     */
    public Map getProperties() {
        return configurationProperties;
    }

    private TypeMapper typeMapper;

    /**
     * @return Returns int.
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
     * @return Returns TypeMapper.
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
     * @return Returns String.
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
     * Constructor for the configuration. It populates the values using the options map.
     *
     * @param wom
     * @param optionMap
     */
    public CodeGenConfiguration(WSDLDescription wom, Map optionMap) {
        this.wom = wom;
        CodegenConfigLoader.loadConfig(this,optionMap);
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
