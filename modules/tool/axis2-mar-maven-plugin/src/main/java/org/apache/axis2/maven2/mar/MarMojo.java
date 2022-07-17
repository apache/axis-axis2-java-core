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

package org.apache.axis2.maven2.mar;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.ManifestException;

import java.io.File;
import java.io.IOException;

/**
 * Build a mar.
 */
@Mojo(name = "mar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class MarMojo extends AbstractMarMojo
{
    /**
     * The Maven Session
     */
    @Parameter(required = true, readonly = true, property = "session")
    private MavenSession session;
    
    /**
     * The directory for the generated mar.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private String outputDirectory;

    /**
     * The name of the generated mar.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String marName;

    /**
     * The Jar archiver.
     */
    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver jarArchiver;

    /**
     * The maven archive configuration to use.
     */
    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     */
    @Parameter
    private String classifier;

    /**
     * Whether this is the main artifact being built. Set to <code>false</code> if you don't want to install or deploy
     * it to the local repository instead of the default one in an execution.
     */
    @Parameter(defaultValue = "true")
    private boolean primaryArtifact;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Executes the MarMojo on the current project.
     * 
     * @throws MojoExecutionException
     *             if an error occured while building the webapp
     */
    public void execute() throws MojoExecutionException
    {

        File marFile = new File( outputDirectory, marName + ".mar" );

        try
        {
            performPackaging( marFile );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error assembling mar", e );
        }
    }

    /**
     * Generates the mar.
     * 
     * @param marFile
     *            the target mar file
     * @throws IOException
     * @throws ArchiverException
     * @throws ManifestException
     * @throws DependencyResolutionRequiredException
     */
    private void performPackaging( File marFile )
        throws IOException, ArchiverException, ManifestException, DependencyResolutionRequiredException,
        MojoExecutionException
    {

        buildExplodedMar( );

        // generate mar file
        getLog().info( "Generating mar " + marFile.getAbsolutePath() );
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver( jarArchiver );
        archiver.setOutputFile( marFile );
        jarArchiver.addDirectory( marDirectory );

        // create archive
        archiver.createArchive(session, project, archive);

        if ( classifier != null )
        {
            projectHelper.attachArtifact( project, "mar", classifier, marFile );
        }
        else
        {
            Artifact artifact = project.getArtifact();
            if ( primaryArtifact )
            {
                artifact.setFile( marFile );
            }
            else if ( artifact.getFile() == null || artifact.getFile().isDirectory() )
            {
                artifact.setFile( marFile );
            }
            else
            {
                projectHelper.attachArtifact( project, "mar", marFile );
            }
        }
    }
}
