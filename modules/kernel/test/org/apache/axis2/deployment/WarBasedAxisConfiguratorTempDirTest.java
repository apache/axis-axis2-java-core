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

package org.apache.axis2.deployment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.axis2.Constants;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WarBasedAxisConfiguratorTempDirTest extends TestCase {

    public void testArtifactsDirIsUnderContainerTempDir() throws Exception {
        File containerTempDir = Files.createTempDirectory("axis2-ctx-tmp").toFile();
        File stray = new File("_axis2");
        boolean strayExisted = stray.exists();

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getAttribute(ServletContext.TEMPDIR)).thenReturn(containerTempDir);
        when(servletContext.getResourceAsStream("/WEB-INF/conf/axis2.xml")).thenReturn(
                new ByteArrayInputStream("<axisconfig name=\"AxisJava2.0\"/>"
                        .getBytes(StandardCharsets.UTF_8)));
        ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        try {
            WarBasedAxisConfigurator configurator = new WarBasedAxisConfigurator(servletConfig);
            File artifactsDir = (File) configurator.axisConfig.getParameterValue(
                    Constants.Configuration.ARTIFACTS_TEMP_DIR);
            assertEquals(new File(containerTempDir, "_axis2"), artifactsDir);
        } finally {
            if (!strayExisted) {
                stray.delete();
            }
            new File(containerTempDir, "_axis2").delete();
            containerTempDir.delete();
        }
    }
}
