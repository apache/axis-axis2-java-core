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

package org.apache.axis2.maven2.wsdl2code;

import org.apache.axis2.maven.shared.NamespaceMapping;
import org.apache.axis2.maven.shared.NamespaceMappingUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.extension.JiBXExtension;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractWSDL2CodeMojo extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter property="project"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * The WSDL file, which is being read.
     *
     * @parameter property="axis2.wsdl2code.wsdlFile" default-value="src/main/resources/service.wsdl"
     */
    private String wsdlFile;

    /**
     * Package name of the generated sources; will be used to create a package structure below the
     * output directory.
     *
     * @parameter property="axis2.wsdl2code.package"
     */
    private String packageName;

    /**
     * The programming language of the generated sources.
     *
     * @parameter property="axis2.wsdl2code.language" default-value="java"
     */
    private String language;

    /**
     * The databinding framework, which is being used by the generated sources.
     *
     * @parameter property="axis2.wsdl2code.databindingName" default-value="adb"
     */
    private String databindingName;

    /**
     * The binding file for JiBX databinding.
     *
     * @parameter property="axis2.wsdl2code.jibxBindingFile"
     */
    private String jibxBindingFile;

    /**
     * Port name, for which to generate sources. By default, sources will be generated for a
     * randomly picked port.
     *
     * @parameter property="axis2.wsdl2code.portName"
     */
    private String portName;

    /**
     * Service name, for which to generate sources. By default, sources will be generated for all
     * services.
     *
     * @parameter property="axis2.wsdl2code.serviceName"
     */
    private String serviceName;

    /**
     * Mode, for which sources are being generated; either of "sync", "async" or "both".
     *
     * @parameter property="axis2.wsdl2code.syncMode" default-value="both"
     */
    private String syncMode;

    /**
     * Whether server side sources are being generated.
     *
     * @parameter property="axis2.wsdl2code.generateServerSide" default-value="false"
     */
    private boolean generateServerSide;

    /**
     * Whether a test case is being generated.
     *
     * @parameter property="axis2.wsdl2code.generateTestCase" default-value="false"
     */
    private boolean generateTestcase;

    /**
     * Whether a "services.xml" file is being generated.
     *
     * @parameter property="axis2.wsdl2code.generateServicesXml" default-value="false"
     */
    private boolean generateServicesXml;

    /**
     * Whether to generate simply all classes. This is only valid in conjunction with
     * "generateServerSide".
     *
     * @parameter property="axis2.wsdl2code.generateAllClasses" default-value="false"
     */
    private boolean generateAllClasses;

    /**
     * Whether to unpack classes.
     *
     * @parameter property="axis2.wsdl2code.unpackClasses" default-value="false"
     */
    private boolean unpackClasses;

    /**
     * Whether to generate the server side interface.
     *
     * @parameter property="axis2.wsdl2code.generateServerSideInterface" default-value="false"
     */
    private boolean generateServerSideInterface = false;

    /**
     * @parameter property="axis2.wsdl2code.repositoryPath"
     */
    private String repositoryPath = null;

    /**
     * @parameter property="axis2.wsdl2code.externalMapping"
     */
    private File externalMapping = null;

    /**
     * @parameter property="axis2.wsdl2code.wsdlVersion" default-value="1.1"
     */
    private String wsdlVersion;

    /**
     * @parameter property="axis2.wsdl2code.targetSourceFolderLocation" default-value="src"
     */
    private String targetSourceFolderLocation;

    /**
     * @parameter property="axis2.wsdl2code.targetResourcesFolderLocation"
     */
    private String targetResourcesFolderLocation = null;

    /**
     * This will select between wrapped and unwrapped during code generation. Maps to the -uw option
     * of the command line tool.
     * 
     * @parameter property="axis2.wsdl2code.unwrap" default-value="false"
     */
    private boolean unwrap = false;

    /**
     * Set this to true to generate code for all ports.
     * 
     * @parameter property="axis2.wsdl2code.allPorts" default-value="false"
     */
    private boolean allPorts = false;

    /**
     * @parameter property="axis2.wsdl2code.backwardCompatible" default-value="false" *
     */
    private boolean backwardCompatible = false;

    /**
     * @parameter property="axis2.wsdl2code.flattenFiles" default-value="false" *
     */
    private boolean flattenFiles = false;

    /**
     * @parameter property="axis2.wsdl2code.skipMessageReceiver" default-value="false" *
     */
    private boolean skipMessageReceiver = false;

    /**
     * @parameter property="axis2.wsdl2code.skipBuildXML" default-value="false" *
     */
    private boolean skipBuildXML = false;

    /**
     * @parameter property="axis2.wsdl2code.skipWSDL" default-value="false" *
     */
    private boolean skipWSDL = false;

    /**
     * @parameter property="axis2.wsdl2code.overWrite" default-value="false" *
     */
    private boolean overWrite = false;

    /**
     * @parameter property="axis2.wsdl2code.suppressPrefixes" default-value="false" *
     */
    private boolean suppressPrefixes = false;

    /**
     * Specify databinding specific extra options
     *
     * @parameter property="axis2.java2wsdl.options"
     */
    private Properties options;

    /**
     * Map of namespace URI to packages in the format {@code uri1=package1,uri2=package2,...}. Using
     * this parameter is discouraged. In general, you should use the {@code namespaceUris}
     * parameter. However, the latter cannot be set on the command line.
     * 
     * @parameter property="axis2.wsdl2code.namespaceToPackages"
     */
    private String namespaceToPackages = null;

    /**
     * Map of namespace URI to packages. Example:
     * <pre>
     * &lt;namespaceMappings>
     *   &lt;namespaceMapping>
     *     &lt;uri>uri1&lt;/uri>
     *     &lt;packageName>package1&lt;/packageName>
     *   &lt;/namespaceMapping>
     *   ...
     * &lt;/namespaceMapping></pre>
     * 
     * @parameter
     */
    private NamespaceMapping[] namespaceMappings;
    
    /**
     * @parameter
     * @deprecated Use {@code namespaceMappings} instead.
     */
    private NamespaceMapping[] namespaceURIs = null;
    
    /**
     * The charset encoding to use for generated source files.
     * 
     * @parameter default-value="${project.build.sourceEncoding}"
     */
    private String encoding;
    
    private CodeGenConfiguration buildConfiguration() throws CodeGenerationException, MojoFailureException {
        CodeGenConfiguration config = new CodeGenConfiguration();
        
        ////////////////////////////////////////////////////////////////
        //WSDL file name
        // here we need to set the project base uri to relative paths.
        if (wsdlFile.indexOf(":") == -1){
           //i.e this is not a uri
           File file = new File(wsdlFile);
           if (!file.isAbsolute()){
               wsdlFile = project.getBasedir() + File.separator + wsdlFile; 
           }
        }

        config.setOutputLocation(getOutputDirectory());
        config.setDatabindingType(databindingName);

        if ("jibx".equals(databindingName)) {
            config.getProperties().put(JiBXExtension.BINDING_PATH_OPTION, jibxBindingFile);
        }

        if ("async".equals(syncMode)) {
            config.setSyncOn(false);
            config.setAsyncOn(true);
        } else if ("sync".equals(syncMode)) {
            config.setSyncOn(true);
            config.setAsyncOn(false);
        } else if ("both".equals(syncMode)) {
            config.setSyncOn(true);
            config.setAsyncOn(true);
        } else {
            throw new MojoFailureException("Invalid syncMode: " + syncMode +
                    ", expected either of 'sync', 'async' or 'both'.");
        }

        config.setPackageName(packageName);
        config.setOutputLanguage(language);
        config.setServerSide(generateServerSide);
        config.setGenerateDeployementDescriptor(generateServicesXml);
        config.setGenerateAll(generateAllClasses);
        config.setWriteTestCase(generateTestcase);
        config.setPackClasses(!unpackClasses);
        config.setServerSideInterface(generateServerSideInterface);
        config.setParametersWrapped(!unwrap);
        config.setAllPorts(allPorts);
        config.setBackwordCompatibilityMode(backwardCompatible);
        config.setFlattenFiles(flattenFiles);
        config.setSkipMessageReceiver(skipMessageReceiver);
        config.setSkipBuildXML(skipBuildXML);
        config.setSkipWriteWSDLs(skipWSDL);
        config.setOverride(overWrite);
        config.setSuppressPrefixesMode(suppressPrefixes);
        config.setRepositoryPath(repositoryPath);
        config.setTypeMappingFile(externalMapping);
        config.setWSDLVersion(wsdlVersion);
        config.setSourceLocation(targetSourceFolderLocation);
        if (targetResourcesFolderLocation != null) {
            config.setResourceLocation(targetResourcesFolderLocation);
        }

        if(options != null) {
            config.getProperties().putAll(options);
        }

        config.setServiceName(serviceName);
        config.setPortName(portName);
        config.setUri2PackageNameMap(getNamespaceToPackagesMap());
        config.setOutputEncoding(encoding);
        
        config.loadWsdl(wsdlFile);
        
        return config;
    }

    private Map<String,String> getNamespaceToPackagesMap() throws MojoFailureException {
        Map<String,String> map = new HashMap<String,String>();
        if (namespaceToPackages != null) {
            for (String pair : namespaceToPackages.trim().split(",")) {
                String values[] = pair.split("=");
                map.put(values[0].trim(), values[1].trim());
            }
        }
        NamespaceMappingUtil.addToMap(namespaceURIs, map);
        NamespaceMappingUtil.addToMap(namespaceMappings, map);
        return map;
    }

    public void execute() throws MojoFailureException, MojoExecutionException {
        File outputDirectory = getOutputDirectory();
        outputDirectory.mkdirs();
        addSourceRoot(project, new File(outputDirectory, targetSourceFolderLocation));

        try {
            new CodeGenerationEngine(buildConfiguration()).generate();
        } catch (CodeGenerationException e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            t.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract File getOutputDirectory();
    protected abstract void addSourceRoot(MavenProject project, File srcDir);
}
