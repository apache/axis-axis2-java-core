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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
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
     * @component
     */
    private ArtifactFactory factory;
    
    /**
     * @component
     */
    private ArtifactResolver resolver;
    
    /**
     * @parameter expression="${project.artifacts}"
     * @readonly
     * @required
     */
    private Set<Artifact> projectArtifacts;
    
    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List remoteRepositories;
    
    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository localRepository;
    
    /**
     * @parameter expression="${project.collectedProjects}"
     * @required
     * @readonly
     */
    private List<MavenProject> collectedProjects;
    
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
    
    /**
     * Specifies whether the plugin should scan the project dependencies for AAR and MAR artifacts.
     * 
     * @parameter default-value="true"
     */
    private boolean useDependencies;
    
    /**
     * Specifies whether the plugin should scan Maven modules for AAR and MAR artifacts. This
     * parameter only has an effect for multimodule projects.
     * 
     * @parameter default-value="true"
     */
    private boolean useModules;
    
    protected abstract String getScope();
    
    protected abstract File getOutputDirectory();

    public void execute() throws MojoExecutionException, MojoFailureException {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        if (useDependencies) {
            artifacts.addAll(projectArtifacts);
        }
        if (useModules) {
            for (MavenProject project : collectedProjects) {
                artifacts.add(project.getArtifact());
                artifacts.addAll(project.getAttachedArtifacts());
            }
        }
        FilterArtifacts filter = new FilterArtifacts();
        filter.addFilter(new ScopeFilter(getScope(), null));
        filter.addFilter(new TypeFilter("aar,mar", null));
        try {
            artifacts = filter.filter(artifacts);
        } catch (ArtifactFilterException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
        artifacts = replaceIncompleteArtifacts(artifacts);
        File outputDirectory = getOutputDirectory();
        File servicesDirectory = new File(outputDirectory, this.servicesDirectory);
        File modulesDirectory = new File(outputDirectory, this.modulesDirectory);
        for (Artifact artifact : artifacts) {
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

    /**
     * Replace artifacts that have not been packaged yet. This occurs if the artifact is
     * part of the reactor build and the compile phase has been executed, but not the
     * the package phase. These artifacts will be replaced by new artifact objects
     * resolved from the repository.
     * 
     * @param artifacts the original sets of {@link Artifact} objects
     * @return a set of {@link Artifact} objects built as described above
     * @throws MojoExecutionException
     */
    private Set<Artifact> replaceIncompleteArtifacts(Set<Artifact> artifacts) throws MojoExecutionException {
        Set<Artifact> result = new HashSet<Artifact>();
        for (Artifact artifact : artifacts) {
            File file = artifact.getFile();
            if (file != null && file.isDirectory()) {
                artifact = factory.createDependencyArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                        artifact.getVersionRange(), artifact.getType(), artifact.getClassifier(), artifact.getScope());
                try {
                    resolver.resolve(artifact, remoteRepositories, localRepository);
                } catch (AbstractArtifactResolutionException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                }
            }
            result.add(artifact);
        }
        return result;
    }
}
