package org.apache.axis2.maven.xsd2java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.schema.XSD2Java;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Generates Java classes from the specified XSD schema files.
 *
 * @goal xsd2java
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class XSD2JavaMojo extends AbstractMojo {

    /**
     * The maven project.
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * The list of XSD files for which to generate the Java code.
     * @parameter
     * @required true
     */
    protected List<String> xsdFiles;

    /**
     * The output directory for the generated Java code.
     * @parameter
     * @required true
     */
    public File outputFolder;

    /**
     * Mapping of namespaces to target Java packages.
     * @parameter
     */
    protected List<String> namespace2Packages;

    /**
     * Run the 'xsd2java' utility.
     * @throws MojoExecutionException if an error occurs during processing
     * @throws MojoFailureException if an error occurs during processing
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        String[] args = getCommandLineArgumentsForXSD2Java();

        try {
            XSD2Java.main(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("An error occurred during 'xsd2java' processing: " + ex.getMessage(), ex);
        }

    }

    /**
     * Process the maven parameters to xsd2java command-line arguments
     * @return the array of command line arguments
     */
    private String[] getCommandLineArgumentsForXSD2Java() {

        final List<String> commandLineArguments = new ArrayList<String>();

        // add the namespace-to-package mappings
        if (namespace2Packages != null) {
            for (String namespace2Package : namespace2Packages) {
                commandLineArguments.add("-ns2p");
                commandLineArguments.add(namespace2Package);
            }
        }

        // add the XSD files
        for (String xsdFile : xsdFiles) {
            commandLineArguments.add(xsdFile);
        }

        // add the output path
        commandLineArguments.add(outputFolder.getAbsolutePath());

        final String[] args = commandLineArguments.toArray(new String[]{});

        return args;

    }

}
