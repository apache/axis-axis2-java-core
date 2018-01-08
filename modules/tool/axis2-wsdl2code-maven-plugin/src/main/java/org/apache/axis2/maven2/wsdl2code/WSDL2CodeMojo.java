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

import org.apache.maven.project.MavenProject;

/**
 * Generates source code from a WSDL.
 * 
 * @goal wsdl2code
 * @phase generate-sources
 * @threadSafe
 * @deprecated This goal is identical to axis2-wsdl2code:generate-sources; either switch to that
 *             goal or use the new axis2-wsdl2code:generate-test-sources goal if you need to
 *             generate code for use in unit tests only.
 */
public class WSDL2CodeMojo extends GenerateSourcesMojo {
    /**
     * The output directory, where the generated sources are being created.
     *
     * @parameter property="axis2.wsdl2code.target" default-value="${project.build.directory}/generated-sources/axis2/wsdl2code"
     */
    private File outputDirectory;

    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected void addSourceRoot(MavenProject project, File srcDir) {
        project.addCompileSourceRoot(srcDir.getPath());
    }
}
