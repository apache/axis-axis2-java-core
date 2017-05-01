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
    private File[] xsdFiles;

    /**
     * The output directory for the generated Java code.
     * @parameter
     * @required true
     */
    private File outputDirectory;

    /**
     * Mapping of namespaces to target Java packages.
     * @parameter
     */
    private NamespaceMapping[] namespaceMappings;

    public void execute() throws MojoExecutionException, MojoFailureException {
        outputDirectory.mkdirs();
        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setOutputLocation(outputDirectory);
        compilerOptions.setGenerateAll(true);
        NamespaceMappingUtil.addToMap(namespaceMappings, compilerOptions.getNs2PackageMap());
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
    }
}
