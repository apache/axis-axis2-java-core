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
package org.apache.axis2.maven.xsd2java;

import java.io.File;

import org.apache.axis2.maven.shared.NamespaceMapping;
import org.apache.axis2.maven.shared.NamespaceMappingUtil;
import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompilationException;
import org.apache.axis2.schema.SchemaCompiler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xml.sax.InputSource;

public abstract class AbstractXSD2JavaMojo extends AbstractMojo {
    /**
     * The maven project.
     * 
     * @parameter property="project"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * The list of XSD files for which to generate the Java code.
     * 
     * @parameter
     * @required
     */
    private File[] xsdFiles;

    /**
     * Mapping of namespaces to target Java packages.
     * 
     * @parameter
     */
    private NamespaceMapping[] namespaceMappings;

    /**
     * The Java package to use for schema items without namespace.
     * 
     * @parameter
     */
    private String noNamespacePackageName;

    /**
     * @parameter
     */
    private String mapperClassPackage;

    /**
     * @parameter
     */
    private boolean helperMode;

    /**
     * @parameter
     */
    private String packageName;

    /**
     * Specifies whether unexpected elements should be ignored (log warning) instead of creating an
     * exception.
     * 
     * @parameter
     */
    private boolean ignoreUnexpected;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputDirectory = getOutputDirectory();
        outputDirectory.mkdirs();
        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setOutputLocation(outputDirectory);
        compilerOptions.setGenerateAll(true);
        NamespaceMappingUtil.addToMap(namespaceMappings, compilerOptions.getNs2PackageMap());
        if (noNamespacePackageName != null) {
            compilerOptions.getNs2PackageMap().put("", noNamespacePackageName);
        }
        compilerOptions.setMapperClassPackage(mapperClassPackage);
        compilerOptions.setHelperMode(helperMode);
        if (packageName != null) {
            compilerOptions.setPackageName(packageName);
        }
        compilerOptions.setIgnoreUnexpected(ignoreUnexpected);
        compilerOptions.setWriteOutput(true);
        try {
            for (File xsdFile : xsdFiles) {
                XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
                SchemaCompiler compiler = new SchemaCompiler(compilerOptions);
                compiler.compile(schemaCollection.read(new InputSource(xsdFile.toURI().toString())));
            }
        } catch (SchemaCompilationException ex) {
            throw new MojoExecutionException("An error occurred during 'xsd2java' processing: " + ex.getMessage(), ex);
        }
        addSourceRoot(project);
    }

    protected abstract File getOutputDirectory();
    protected abstract void addSourceRoot(MavenProject project);
}
