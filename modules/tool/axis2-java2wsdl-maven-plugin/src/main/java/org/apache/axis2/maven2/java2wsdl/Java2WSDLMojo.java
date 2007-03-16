package org.apache.axis2.maven2.java2wsdl;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.ws.java2wsdl.Java2WSDLCodegenEngine;
import org.apache.ws.java2wsdl.Java2WSDLConstants;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;


/**
 * Takes a Java class as input and converts it into an equivalent
 * WSDL file.
 * 
 * @goal java2wsdl
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class Java2WSDLMojo extends AbstractMojo {
    /**
     * The maven project.
     * @parameter expression="${project}"
     * @read-only
     * @required
     */
    private MavenProject project;

    /**
     * Fully qualified name of the class, which is being inspected.
     * @parameter expression="${axis2.java2wsdl.className}"
     * @required
     */
    private String className;

    /**
     * Target namespace of the generated WSDL.
     * @parameter expression="${axis2.java2wsdl.targetNamespace}"
     */
    private String targetNamespace;

    /**
     * The namespace prefix, which is being used for the WSDL's
     * target namespace.
     * @parameter expression="${axis2.java2wsdl.targetNamespacePrefix}"
     */
    private String targetNamespacePrefix;

    /**
     * The generated schemas target namespace.
     * @parameter expression="${axis2.java2wsdl.schemaTargetNamespace}"
     */
    private String schemaTargetNamespace;

    /**
     * The generated schemas target namespace prefix.
     * @parameter expression="${axis2.java2wsdl.schemaTargetNamespacePrefix}"
     */
    private String schemaTargetNamespacePrefix;

    /**
     * Name of the generated service.
     * @parameter expression="${axis2.java2wsdl.serviceName}"
     */
    private String serviceName;

    /**
     * Name of the service file, which is being generated.
     * @parameter expression="${axis2.java2wsdl.outputFileName}" default-value="${project.build.outputDirectory}/generated-resources/java2wsdl/service.xml"
     */
    private String outputFileName;

    private void addToOptionMap(Map map, String option, String value) {
        addToOptionMap(map, option, new String[]{value});
    }

    private void addToOptionMap(Map map, String option, String[] value) {
        if (value != null) {
            map.put(option,
                    new Java2WSDLCommandLineOption(option, value));
        }
    }

    /**
     * Fills the option map. This map is passed onto
     * the code generation API to generate the code.
     */
    private Map fillOptionMap() throws MojoFailureException {
        Map optionMap = new HashMap();

        if (className == null) {
            throw new MojoFailureException("You must specify a classname");
        }
        addToOptionMap( optionMap,
                        Java2WSDLConstants.CLASSNAME_OPTION,
                        className);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.TARGET_NAMESPACE_OPTION,
                        targetNamespace);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.TARGET_NAMESPACE_PREFIX_OPTION,
                        targetNamespacePrefix);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_OPTION,
                        schemaTargetNamespace);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                        schemaTargetNamespacePrefix);
        addToOptionMap( optionMap,
                        Java2WSDLConstants.SERVICE_NAME_OPTION,
                        serviceName);
        File f = new File(project.getBasedir(), outputFileName);
        File dir = f.getParentFile();
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        addToOptionMap( optionMap,
                        Java2WSDLConstants.OUTPUT_LOCATION_OPTION,
                        dir.getPath() );
        addToOptionMap( optionMap,
                        Java2WSDLConstants.OUTPUT_FILENAME_OPTION,
                        f.getName() );

        Set artifacts = project.getArtifacts();
        String[] artifactFileNames = new String[artifacts.size() + 1];
        int j = 0;
        for(Iterator i = artifacts.iterator(); i.hasNext(); j++) {
            artifactFileNames[j] = ((Artifact) i.next()).getFile().getAbsolutePath();
        }
        artifactFileNames[j] = project.getArtifact().getFile().getAbsolutePath();

        addToOptionMap( optionMap,
                        Java2WSDLConstants.CLASSPATH_OPTION,
                        artifactFileNames);

        return optionMap;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        Map commandLineOptions = fillOptionMap();
        try {
            new Java2WSDLCodegenEngine(commandLineOptions).generate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
