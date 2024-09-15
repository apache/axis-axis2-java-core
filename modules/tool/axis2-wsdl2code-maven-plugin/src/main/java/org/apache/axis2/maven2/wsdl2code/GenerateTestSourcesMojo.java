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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates source code from a WSDL, for use in unit tests. This goal bind by default to the
 * generate-test-sources phase and adds the sources to the test sources of the project; it is
 * otherwise identical to the axis2-wsdl2code:generate-sources goal.
 */
@Mojo(name = "generate-test-sources", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, threadSafe = true)
public class GenerateTestSourcesMojo extends AbstractWSDL2CodeMojo {
    /**
     * The output directory, where the generated sources are being created.
     */
    @Parameter(property = "axis2.wsdl2code.target", defaultValue = "${project.build.directory}/generated-test-sources/wsdl2code")
    private File outputDirectory;
    
    @Parameter(property = "maven.test.skip")
    private boolean skip;
    
    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected void addSourceRoot(MavenProject project, File srcDir) {
        project.addTestCompileSourceRoot(srcDir.getPath());
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Not generating test sources");
        } else {
            super.execute();
        }
    }
}
