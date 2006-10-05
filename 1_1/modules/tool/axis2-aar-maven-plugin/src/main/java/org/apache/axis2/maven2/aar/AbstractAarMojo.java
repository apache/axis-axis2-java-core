package org.apache.axis2.maven2.aar;

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
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;



/**
 * Abstract base class of all the mojos in the axis2-aar-maven-plugin.
 */
public abstract class AbstractAarMojo
    extends AbstractMojo
{

    /**
     * The projects base directory.
     *
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    protected File baseDir;
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * The directory where the aar is built.
     *
     * @parameter expression="${project.build.directory}/aar"
     * @required
     */
    protected File aarDirectory;

    /**
     * The location of the services.xml file.  If it is present in the META-INF
     * directory in src/main/resources with that name then it will automatically be 
     * included. Otherwise this parameter must be set.
     *
     * @parameter 
     */
    private File servicesXmlFile;

    /**
     * The location of the WSDL file, if any. By default, no WSDL file is added
     * and it is assumed, that Axis 2 will automatically generate a WSDL file.
     *
     * @parameter 
     */
    private File wsdlFile;

    /**
     * Name, to which the wsdl file shall be mapped. By default, the name will
     * be computed from the files path by removing the directory.
     * 
     * @parameter default-value="service.wsdl"
     */
    private String wsdlFileName;

    /**
     * Additional file sets, which are being added to the archive.
     *
     * @parameter
     */
    private FileSet[] fileSets;

    /**
     * Builds the exploded AAR file.
     * @throws MojoExecutionException
     */
    protected void buildExplodedAar( )
        throws MojoExecutionException
    {
        getLog().debug( "Exploding aar..." );

        aarDirectory.mkdirs();
        getLog().debug( "Assembling aar " + project.getArtifactId() + " in " + aarDirectory );

        try
        {
            final File metaInfDir = new File( aarDirectory, "META-INF" );
            final File servicesFileTarget = new File( metaInfDir, "services.xml" );
            boolean existsBeforeCopyingClasses = servicesFileTarget.exists();

            String wsdlName = wsdlFileName;
            if ( wsdlName == null  &&  wsdlFile != null )
            {
                wsdlName = wsdlFile.getName();
            }
            File wsdlFileTarget = null;
            if ( wsdlFile != null )
            {
                wsdlFileTarget = new File( metaInfDir, wsdlFileName );
            }
            boolean wsdlExistsBeforeCopyingClasses = wsdlFileTarget == null ? false : wsdlFileTarget.exists();

            if ( classesDirectory.exists() && ( !classesDirectory.equals( aarDirectory ) ) )
            {
                FileUtils.copyDirectoryStructure( classesDirectory, aarDirectory );
            }

            if ( fileSets != null )
            {
                for ( int i = 0;  i < fileSets.length;   i++ )
                {
                    FileSet fileSet = fileSets[i];
                    copyFileSet( fileSet, aarDirectory );
                }
            }
            
            copyMetaInfFile( servicesXmlFile, servicesFileTarget, existsBeforeCopyingClasses, "services.xml file" );
            copyMetaInfFile( wsdlFile, wsdlFileTarget, wsdlExistsBeforeCopyingClasses, "WSDL file" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not explode aar...", e );
        }
    }


    private void copyFileSet( FileSet fileSet, File targetDirectory )
        throws IOException
    {
        File dir = fileSet.getDirectory();
        if ( dir == null )
        {
            dir = baseDir;
        }
        File targetDir = targetDirectory;
        if ( fileSet.getOutputDirectory() != null )
        {
            targetDir = new File( targetDir, fileSet.getOutputDirectory() );
        }
        if ( targetDir.equals( dir ) )
        {
            return;
        }

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( dir );
        if ( !fileSet.isSuppressDefaultExcludes() )
        {
            ds.addDefaultExcludes();
        }
        final String[] excludes = fileSet.getExcludes();
        if ( excludes != null )
        {
            ds.setExcludes( excludes );
        }
        final String[] includes = fileSet.getIncludes();
        if ( includes != null )
        {
            ds.setIncludes( includes );
        }
        ds.scan();
        String[] files = ds.getIncludedFiles();
        for ( int i = 0;  i < files.length;  i++ )
        {
            File sourceFile = new File( dir, files[i] );
            File targetFile = new File( targetDir, files[i] );
            FileUtils.copyFile( sourceFile, targetFile );
        }
    }


    private void copyMetaInfFile( final File pSource, final File pTarget,
                                  final boolean pExistsBeforeCopying,
                                  final String pDescription )
        throws MojoExecutionException, IOException
    {
        if ( pSource != null  &&  pTarget != null )
        {
            if ( !pSource.exists() )
            {
                throw new MojoExecutionException( "The configured " + pDescription + " could not be found at "
                                                  + pSource );
            }

            if ( !pExistsBeforeCopying  &&  pTarget.exists() )
            {
                getLog().warn( "The configured " + pDescription + " overwrites another file from the classpath." );
            }

            FileUtils.copyFile( pSource, pTarget );
        }
    }
}
