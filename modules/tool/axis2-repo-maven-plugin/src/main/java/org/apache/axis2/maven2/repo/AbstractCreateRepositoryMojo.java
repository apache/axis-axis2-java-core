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

package org.apache.axis2.maven2.repo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractCreateRepositoryMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;
    
    /**
     * The directory (relative to the repository root) where AAR files are copied. This should be
     * set to the same value as the <tt>ServicesDirectory</tt> property in <tt>axis2.xml</tt>.
     * 
     * @parameter default-value="services"
     */
    private String servicesDirectory;
    
    /**
     * The directory (relative to the repository root) where MAR files are copied. This should be
     * set to the same value as the <tt>ModulesDirectory</tt> property in <tt>axis2.xml</tt>.
     * 
     * @parameter default-value="modules"
     */
    private String modulesDirectory;
    
    /**
     * The <tt>axis2.xml</tt> file to be copied into the repository.
     * 
     * @parameter
     */
    private File axis2xml;
    
    /**
     * The directory (relative to the repository root) where the <tt>axis2.xml</tt> file will be
     * copied. If this parameter is not set, then the file will be copied into the repository
     * root.
     * 
     * @parameter
     */
    private String configurationDirectory;
    
    protected abstract String getScope();
    
    protected abstract File getOutputDirectory();

    public void execute() throws MojoExecutionException, MojoFailureException {
        Set artifacts = project.getArtifacts();
        FilterArtifacts filter = new FilterArtifacts();
        filter.addFilter(new ScopeFilter(getScope(), null));
        filter.addFilter(new TypeFilter("aar,mar", null));
        try {
            artifacts = filter.filter(artifacts);
        } catch (ArtifactFilterException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
        File outputDirectory = getOutputDirectory();
        File servicesDirectory = new File(outputDirectory, this.servicesDirectory);
        File modulesDirectory = new File(outputDirectory, this.modulesDirectory);
        for (Iterator it = artifacts.iterator(); it.hasNext(); ) {
            Artifact artifact = (Artifact)it.next();
            String type = artifact.getType();
            String destFileName = artifact.getArtifactId() + "-" + artifact.getVersion() + "." + type;
            File targetDir = type.equals("mar") ? modulesDirectory : servicesDirectory;
            getLog().info("Adding " + destFileName);
            try {
                FileUtils.copyFile(artifact.getFile(), new File(targetDir, destFileName));
            } catch (IOException ex) {
                throw new MojoExecutionException("Error copying " + destFileName + ": " + ex.getMessage(), ex);
            }
        }
        if (axis2xml != null) {
            getLog().info("Copying axis2.xml");
            File targetDirectory = configurationDirectory == null
                    ? outputDirectory : new File(outputDirectory, configurationDirectory);
            try {
                FileUtils.copyFile(axis2xml, new File(targetDirectory, "axis2.xml"));
            } catch (IOException ex) {
                throw new MojoExecutionException("Error copying axis2.xml file: " + ex.getMessage(), ex);
            }
        }
    }
}
