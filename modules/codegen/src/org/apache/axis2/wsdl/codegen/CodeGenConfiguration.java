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

package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL20ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL20ToAxisServiceBuilder;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.ws.commons.schema.XmlSchema;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenConfiguration implements CommandLineOptionConstants {

    /** Axis Services reference */
    private List<AxisService> axisServices;

    /** Axis service to use */
    private AxisService axisService;

    /** Base URI */
    private String baseURI;
    /** path to the repository - used for evaluating policy */
    private String repositoryPath;
    /** Mapping file including the qname to type map */
    private File typeMappingFile;

    /** keeps the WSDL version  - default is 1.1 */
    private String WSDLVersion = WSDL2JavaConstants.WSDL_VERSION_1;

    public String getWSDLVersion() {
        return WSDLVersion;
    }

    public void setWSDLVersion(String WSDLVersion) {
        this.WSDLVersion = WSDLVersion;
    }

    public File getTypeMappingFile() {
        return typeMappingFile;
    }

    public void setTypeMappingFile(File typeMappingFile) {
        this.typeMappingFile = typeMappingFile;
    }

    /** A map to keep the custom namespace and package name mappings */
    private Map<String,String> uri2PackageNameMap;


    public Map<String,String> getUri2PackageNameMap() {
        return uri2PackageNameMap;
    }

    public void setUri2PackageNameMap(Map<String,String> uri2PackageNameMap) {
        if (this.uri2PackageNameMap == null) {
            this.uri2PackageNameMap = uri2PackageNameMap;
        } else {
            this.uri2PackageNameMap.putAll(uri2PackageNameMap);
        }
    }

    /** Full path and name of XMLBeans xsdconfig file to use */
    private String xsdConfigFile = null;

    /**
     * Returns the xsdconfig file to use for XMLBeans data binding.
     *
     * @return Full path and name of XMLBeans xsdconfig file to use
     * @deprecated
     */
    public String getXsdConfigFile() {
        return xsdConfigFile;
    }

    /**
     * Sets the xsdconfig file to use for XMLBeans data binding.
     *
     * @param xsdConfigFile Full path and name of XMLBeans xsdconfig file to use
     * @deprecated
     */
    public void setXsdConfigFile(String xsdConfigFile) {
        this.xsdConfigFile = xsdConfigFile;
    }

    /** Says whether to flatten the files or put them in specific folders */
    private boolean flattenFiles = false;

    public boolean isFlattenFiles() {
        return flattenFiles;
    }

    public void setFlattenFiles(boolean flattenFiles) {
        this.flattenFiles = flattenFiles;
    }

    /** Folder name for the resource files */
    private String resourceLocation = ConfigPropertyFileLoader.getResourceFolderName();

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    /** Folder name for the source files */
    private String sourceLocation = ConfigPropertyFileLoader.getSrcFolderName();

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    /** Determines whether the parameters are wrapped or unwrapped true by default */
    private boolean parametersWrapped = true;


    public boolean isParametersWrapped() {
        return parametersWrapped;
    }

    public void setParametersWrapped(boolean parametersWrapped) {
        this.parametersWrapped = parametersWrapped;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public Map<Object,Object> getConfigurationProperties() {
        return configurationProperties;
    }

    public void setConfigurationProperties(Map<Object,Object> configurationProperties) {
        this.configurationProperties = configurationProperties;
    }


    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
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
    private String outputEncoding = System.getProperty("file.encoding");
    private String databindingType = ConfigPropertyFileLoader.getDefaultDBFrameworkName();
    private boolean advancedCodeGenEnabled = false;


    private boolean asyncOn = true;
    private boolean syncOn = true;
    private boolean serverSide = false;
    private boolean generateDeployementDescriptor = true;
    private boolean writeTestCase = false;
    private boolean skipMessageReceiver = false;
    private boolean skipWriteWSDLs = false;
    private boolean skipBuildXML = false;
    private boolean setoutputSourceLocation = false;
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

    //option to generate server side interface or not
    private boolean serverSideInterface = false;


    public boolean isServerSideInterface() {
        return serverSideInterface;
    }

    public void setServerSideInterface(boolean serverSideInterface) {
        this.serverSideInterface = serverSideInterface;
    }
    public boolean isSetoutputSourceLocation(){
      return  setoutputSourceLocation;
    }

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

    /** A hashmap to hang the property objects */
    private Map policyMap = new HashMap();

    /*
    * A hashmap of properties that may be populated on the way. extensions can populate it
    * This can be used to keep non specific information
    */
    private Map<Object,Object> configurationProperties = new HashMap<Object,Object>();


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
    public Map<Object,Object> getProperties() {
        return configurationProperties;
    }

    private TypeMapper typeMapper;


    /** @return Returns TypeMapper. */
    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    /** @param typeMapper  */
    public void setTypeMapper(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    /** @return Returns String. */
    public String getDatabindingType() {
        return databindingType;
    }

    /** @param databindingType  */
    public void setDatabindingType(String databindingType) {
        this.databindingType = databindingType;
    }


    /**
     * Constructor for the configuration. It populates the values using the options map.
     *
     * @param optionMap
     */
    public CodeGenConfiguration() {
        this.axisServices = new ArrayList<AxisService>();
        this.outputFileNamesList = new ArrayList<String>();
    }


    /** @return Returns the outputLocation. */
    public File getOutputLocation() {
        return outputLocation;
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public boolean isAdvancedCodeGenEnabled() {
        return advancedCodeGenEnabled;
    }


    /** @return Returns the packageName. */
    public String getPackageName() {
        return packageName;
    }

    /** @param packageName The packageName to set. */
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


    public boolean isSkipBuildXML() {
        return skipBuildXML;
    }

    public void setSkipBuildXML(boolean skipBuildXML) {
        this.skipBuildXML = skipBuildXML;
    }

    public boolean isWriteTestCase() {
        return writeTestCase;
    }

    public boolean isSkipWriteWSDLs() {
        return skipWriteWSDLs;
    }

    public void setSkipWriteWSDLs(boolean writeWriteWSDLs) {
        this.skipWriteWSDLs = writeWriteWSDLs;
    }

    public boolean isSkipMessageReceiver() {
        return skipMessageReceiver;
    }

    public void setSkipMessageReceiver(boolean skipMessageReceiver) {
        this.skipMessageReceiver = skipMessageReceiver;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public List<AxisService> getAxisServices() {
        return axisServices;
    }

    public void setAxisServices(List<AxisService> axisServices) {
        this.axisServices = axisServices;
    }

    public void addAxisService(AxisService axisService) {
        this.axisServices.add(axisService);
    }

    public AxisService getAxisService() {
        // return the first element of the axis services
        if (axisService != null) {
            return axisService;
        } else if ((axisServices != null) && (axisServices.size() > 0)) {
            return axisServices.get(0);
        } else {
            return null;
        }
    }

    public void setAxisService(AxisService axisService) {
        this.axisService = axisService;
    }

    /**
     * This flag determines whether the generated classes are expected to be backword compatible
     * with Axis 1.x
     */
    private boolean backwordCompatibilityMode = false;

    public boolean isBackwordCompatibilityMode() {
        return backwordCompatibilityMode;
    }

    public void setBackwordCompatibilityMode(boolean backwordCompatibilityMode) {
        this.backwordCompatibilityMode = backwordCompatibilityMode;
    }

    /** Should we suppress namespace prefixes */
    private boolean suppressPrefixesMode = false;

    public boolean isSuppressPrefixesMode() {
        return suppressPrefixesMode;
    }

    public void setSuppressPrefixesMode(boolean suppressPrefixesMode) {
        this.suppressPrefixesMode = suppressPrefixesMode;
    }

    public String getTargetNamespace() {
        String targetNamespace = null;
        if ((this.axisServices != null) && (this.axisServices.size() > 0)) {
            targetNamespace = this.axisServices.get(0).getTargetNamespace();
        }
        return targetNamespace;
    }

    public List<XmlSchema> getSchemaListForAllServices() {
        List<XmlSchema> schemas = new ArrayList<XmlSchema>();
        for (AxisService axisService : axisServices) {
            schemas.addAll(axisService.getSchema());
        }
        return schemas;
    }

    /** keep this wsdl definition to write the wsdl file later */
    private Definition wsdlDefinition;

    public Definition getWsdlDefinition() {
        return wsdlDefinition;
    }

    public void setWsdlDefinition(Definition wsdlDefinition) {
        this.wsdlDefinition = wsdlDefinition;
    }

    private boolean isAllPorts;
    private boolean isOverride;
    

    public boolean isAllPorts() {
        return isAllPorts;
    }

    public void setAllPorts(boolean allPorts) {
        isAllPorts = allPorts;
    }

    public boolean isOverride() {
        return isOverride;
    }

    public void setOverride(boolean override) {
        isOverride = override;
    }

    // this is used to keep the generated xml file list to pretty print.
    private List<String> outputFileNamesList;

    public List<String> getOutputFileNamesList() {
        return outputFileNamesList;
    }

    public void setOutputFileNamesList(List<String> outputXmlFileNamesList) {
        this.outputFileNamesList = outputXmlFileNamesList;
    }

    public void addOutputFileName(String fileName){
       this.outputFileNamesList.add(fileName);
    }
    public void setoutputSourceLocation(boolean setoutputsourcelocation)
    {
        setoutputSourceLocation = true;
        
    }

    private String excludeProperties;

    public String getExcludeProperties() {
        return excludeProperties;
    }

    public void setExcludeProperties(String excludeProperties) {
        this.excludeProperties = excludeProperties;
    }

    private String skeltonInterfaceName;
    private String skeltonClassName;

    public String getSkeltonInterfaceName() {
        return skeltonInterfaceName;
    }

    public void setSkeltonInterfaceName(String skeltonInterfaceName) {
        this.skeltonInterfaceName = skeltonInterfaceName;
    }

    public String getSkeltonClassName() {
        return skeltonClassName;
    }

    public void setSkeltonClassName(String skeltonClassName) {
        this.skeltonClassName = skeltonClassName;
    }

    private boolean overrideAbsoluteAddress;

    public boolean isOverrideAbsoluteAddress() {
        return overrideAbsoluteAddress;
    }

    public void setOverrideAbsoluteAddress(boolean overrideAbsoluteAddress) {
        this.overrideAbsoluteAddress = overrideAbsoluteAddress;
    }

    private String exceptionBaseClassName;

    public String getExceptionBaseClassName() {
        return exceptionBaseClassName;
    }

    public void setExceptionBaseClassName(String exceptionBaseClassName) {
        this.exceptionBaseClassName = exceptionBaseClassName;
    }

    private boolean isUseOperationName;

    public boolean isUseOperationName() {
        return isUseOperationName;
    }

    public void setUseOperationName(boolean useOperationName) {
        isUseOperationName = useOperationName;
    }

    public void loadWsdl(String wsdlUri) throws CodeGenerationException {
        try {
            // the redirected urls gives problems in code generation some times with jaxbri
            // eg. https://www.paypal.com/wsdl/PayPalSvc.wsdl
            // if there is a redirect url better to find it and use.
            if (wsdlUri.startsWith("http")) {
                URL url = new URL(wsdlUri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.getResponseCode();
                String newLocation = connection.getHeaderField("Location");
                if (newLocation != null){
                    wsdlUri = newLocation;
                }
            }

            if (CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_2.
                    equals(getWSDLVersion())) {

                WSDL20ToAxisServiceBuilder builder;

                // jibx currently does not support multiservice
                if ((getServiceName() != null) || (getDatabindingType().equals("jibx"))) {
                    builder = new WSDL20ToAxisServiceBuilder(
                            wsdlUri,
                            getServiceName(),
                            getPortName(),
                            isAllPorts());
                    builder.setCodegen(true);
                    addAxisService(builder.populateService());
                } else {
                    builder = new WSDL20ToAllAxisServicesBuilder(wsdlUri, getPortName());
                    builder.setCodegen(true);
                    builder.setAllPorts(isAllPorts());
                    setAxisServices(
                            ((WSDL20ToAllAxisServicesBuilder)builder).populateAllServices());
                }

            } else {
                //It'll be WSDL 1.1
                Definition wsdl4jDef = readInTheWSDLFile(wsdlUri);

                // we save the original wsdl definition to write it to the resource folder later
                // this is required only if it has imports
                Map imports = wsdl4jDef.getImports();
                if ((imports != null) && (imports.size() > 0)) {
                    setWsdlDefinition(readInTheWSDLFile(wsdlUri));
                } else {
                    setWsdlDefinition(wsdl4jDef);
                }

                // we generate the code for one service and one port if the
                // user has specified them.
                // otherwise generate the code for every service.
                // TODO: find out a permanant solution for this.
                QName serviceQname = null;

                if (getServiceName() != null) {
                    serviceQname = new QName(wsdl4jDef.getTargetNamespace(),
                                             getServiceName());
                }

                WSDL11ToAxisServiceBuilder builder;
                // jibx currently does not support multiservice
                if ((serviceQname != null) || (getDatabindingType().equals("jibx"))) {
                    builder = new WSDL11ToAxisServiceBuilder(
                            wsdl4jDef,
                            serviceQname,
                            getPortName(),
                            isAllPorts());
                    builder.setCodegen(true);
                    addAxisService(builder.populateService());
                } else {
                    builder = new WSDL11ToAllAxisServicesBuilder(wsdl4jDef, getPortName());
                    builder.setCodegen(true);
                    builder.setAllPorts(isAllPorts());
                    setAxisServices(
                            ((WSDL11ToAllAxisServicesBuilder)builder).populateAllServices());
                }
            }
            setBaseURI(getBaseURI(wsdlUri));
        } catch (AxisFault axisFault) {
            throw new CodeGenerationException(
                    CodegenMessages.getMessage("engine.wsdlParsingException"), axisFault);
        } catch (WSDLException e) {
            throw new CodeGenerationException(
                    CodegenMessages.getMessage("engine.wsdlParsingException"), e);
        } catch (Exception e) {
            throw new CodeGenerationException(                            
                    CodegenMessages.getMessage("engine.wsdlParsingException"), e);
        }
    }

    /**
     * calculates the base URI Needs improvement but works fine for now ;)
     *
     * @param currentURI
     */
    private String getBaseURI(String currentURI) throws URISyntaxException, IOException {
        File file = new File(currentURI);
        if (file.exists()) {
            return file.getCanonicalFile().getParentFile().toURI().toString();
        }
        String uriFragment = currentURI.substring(0, currentURI.lastIndexOf("/"));
        return uriFragment + (uriFragment.endsWith("/") ? "" : "/");
    }

    /**
     * Read the WSDL file
     *
     * @param uri
     * @throws WSDLException
     */
    private Definition readInTheWSDLFile(final String uri) throws WSDLException {

        WSDLReader reader = WSDLUtil.newWSDLReaderWithPopulatedExtensionRegistry();
        reader.setFeature("javax.wsdl.importDocuments", true);

        return reader.readWSDL(uri);
        
    }
}
