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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Creates an Axis2 repository from the project's dependencies in scope test. This goal is
 * typically used to build an Axis2 repository for use during unit tests. Note that this goal
 * is skipped if the <code>maven.test.skip</code> property is set to <code>true</code>.
 */
@Mojo(name = "create-test-repository", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class CreateTestRepositoryMojo extends AbstractCreateRepositoryMojo {
    /**
     * Input directory with additional files to be copied to the repository.
     */
    @Parameter(defaultValue = "src/test/repository")
    private File inputDirectory;
    
    /**
     * The output directory where the repository will be created.
     */
    @Parameter(defaultValue = "${project.build.directory}/test-repository")
    private File outputDirectory;
    
    @Parameter(property = "maven.test.skip")
    private boolean skip;
    
    @Parameter(property = "project.build.outputDirectory", readonly = true)
    private File buildOutputDirectory;
    
    @Parameter(property = "project.build.testOutputDirectory", readonly = true)
    private File buildTestOutputDirectory;
    
    @Override
    protected String getScope() {
        return Artifact.SCOPE_TEST;
    }

    @Override
    protected File getInputDirectory() {
        return inputDirectory;
    }

    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected File[] getClassDirectories() {
        return new File[] { buildOutputDirectory, buildTestOutputDirectory };
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Not creating test repository");
        } else {
            super.execute();
        }
    }
}
