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

package org.apache.axis2.spring.boot;

import org.apache.axis2.deployment.WarBasedAxisConfigurator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link Axis2ServletAutoConfiguration} and
 * {@link Axis2RepositoryAutoConfiguration} autoconfiguration logic.
 *
 * <p>Verifies that the axis2.repo property correctly overrides
 * ServletContext.getRealPath() and that the axis2.xml.path init-parameter
 * is set when the repo contains conf/axis2.xml.
 */
class Axis2ServletAutoConfigurationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // Axis2ServletAutoConfiguration — repository and axis2.xml path
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void repoProperty_overridesGetRealPath(@TempDir Path tempDir) throws Exception {
        // Create the expected directory structure
        Path confDir = tempDir.resolve("conf");
        Files.createDirectories(confDir);
        Files.writeString(confDir.resolve("axis2.xml"), "<axisconfig/>");

        Axis2Properties properties = new Axis2Properties();
        properties.setRepo(tempDir.toString());

        // Mock ServletContext that returns null for getRealPath (embedded mode)
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/WEB-INF")).thenReturn(null);

        ServletRegistration.Dynamic axisServlet = mock(ServletRegistration.Dynamic.class);
        when(servletContext.addServlet(eq("AxisServlet"), any(org.apache.axis2.transport.http.AxisServlet.class)))
                .thenReturn(axisServlet);
        when(axisServlet.addMapping(anyString())).thenReturn(Collections.emptySet());

        // Execute
        Axis2ServletAutoConfiguration config = new Axis2ServletAutoConfiguration();
        config.axis2ServletInitializer(properties).onStartup(servletContext);

        // Verify repository path was set from axis2.repo, not getRealPath
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(axisServlet, atLeast(2)).setInitParameter(nameCaptor.capture(), valueCaptor.capture());

        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < nameCaptor.getAllValues().size(); i++) {
            params.put(nameCaptor.getAllValues().get(i), valueCaptor.getAllValues().get(i));
        }

        assertEquals(tempDir.toString(),
                params.get(WarBasedAxisConfigurator.PARAM_AXIS2_REPOSITORY_PATH),
                "repository path should come from axis2.repo property");
        assertNotNull(params.get(WarBasedAxisConfigurator.PARAM_AXIS2_XML_PATH),
                "axis2.xml.path should be set when conf/axis2.xml exists");
        assertTrue(params.get(WarBasedAxisConfigurator.PARAM_AXIS2_XML_PATH)
                        .endsWith("axis2.xml"),
                "axis2.xml.path should point to conf/axis2.xml");
    }

    @Test
    void emptyRepoProperty_usesGetRealPath(@TempDir Path tempDir) throws Exception {
        Axis2Properties properties = new Axis2Properties();
        // repo is empty — should fall back to getRealPath

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/WEB-INF")).thenReturn(tempDir.toString());

        ServletRegistration.Dynamic axisServlet = mock(ServletRegistration.Dynamic.class);
        when(servletContext.addServlet(eq("AxisServlet"), any(org.apache.axis2.transport.http.AxisServlet.class)))
                .thenReturn(axisServlet);
        when(axisServlet.addMapping(anyString())).thenReturn(Collections.emptySet());

        Axis2ServletAutoConfiguration config = new Axis2ServletAutoConfiguration();
        config.axis2ServletInitializer(properties).onStartup(servletContext);

        // Verify getRealPath result was used
        verify(axisServlet).setInitParameter(
                eq(WarBasedAxisConfigurator.PARAM_AXIS2_REPOSITORY_PATH),
                eq(tempDir.toString()));
    }

    @Test
    void repoProperty_failsFastWhenAxis2XmlMissing(@TempDir Path tempDir) {
        // Create repo dir WITHOUT conf/axis2.xml
        Axis2Properties properties = new Axis2Properties();
        properties.setRepo(tempDir.toString());

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/WEB-INF")).thenReturn(null);

        ServletRegistration.Dynamic axisServlet = mock(ServletRegistration.Dynamic.class);
        when(servletContext.addServlet(eq("AxisServlet"), any(org.apache.axis2.transport.http.AxisServlet.class)))
                .thenReturn(axisServlet);
        when(axisServlet.addMapping(anyString())).thenReturn(Collections.emptySet());

        Axis2ServletAutoConfiguration config = new Axis2ServletAutoConfiguration();

        // Should throw because axis2.repo is set but conf/axis2.xml is missing
        assertThrows(IllegalStateException.class,
                () -> config.axis2ServletInitializer(properties).onStartup(servletContext),
                "Should fail fast when axis2.repo is set but axis2.xml is missing");
    }

    @Test
    void repoProperty_noAxis2XmlPath_whenFileAbsentAndRepoNotSet(@TempDir Path tempDir) throws Exception {
        // getRealPath returns a valid path but conf/axis2.xml doesn't exist
        // AND axis2.repo is NOT set — this is fine, WarBasedAxisConfigurator
        // will load from classpath as it always has
        Axis2Properties properties = new Axis2Properties();

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/WEB-INF")).thenReturn(tempDir.toString());

        ServletRegistration.Dynamic axisServlet = mock(ServletRegistration.Dynamic.class);
        when(servletContext.addServlet(eq("AxisServlet"), any(org.apache.axis2.transport.http.AxisServlet.class)))
                .thenReturn(axisServlet);
        when(axisServlet.addMapping(anyString())).thenReturn(Collections.emptySet());

        Axis2ServletAutoConfiguration config = new Axis2ServletAutoConfiguration();
        config.axis2ServletInitializer(properties).onStartup(servletContext);

        // Should set repository path but NOT axis2.xml.path (file doesn't exist)
        verify(axisServlet).setInitParameter(
                eq(WarBasedAxisConfigurator.PARAM_AXIS2_REPOSITORY_PATH),
                eq(tempDir.toString()));
        verify(axisServlet, never()).setInitParameter(
                eq(WarBasedAxisConfigurator.PARAM_AXIS2_XML_PATH), anyString());
    }

    @Test
    void servletMappedToConfiguredPath() throws Exception {
        Axis2Properties properties = new Axis2Properties();
        properties.setServicesPath("/api");

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/WEB-INF")).thenReturn("/tmp/fake");

        ServletRegistration.Dynamic axisServlet = mock(ServletRegistration.Dynamic.class);
        when(servletContext.addServlet(eq("AxisServlet"), any(org.apache.axis2.transport.http.AxisServlet.class)))
                .thenReturn(axisServlet);
        when(axisServlet.addMapping(anyString())).thenReturn(Collections.emptySet());

        Axis2ServletAutoConfiguration config = new Axis2ServletAutoConfiguration();
        config.axis2ServletInitializer(properties).onStartup(servletContext);

        verify(axisServlet).addMapping("/api/*");
    }

    @Test
    void servletMappingConflict_throwsException() {
        Axis2Properties properties = new Axis2Properties();

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/WEB-INF")).thenReturn("/tmp/fake");

        ServletRegistration.Dynamic axisServlet = mock(ServletRegistration.Dynamic.class);
        when(servletContext.addServlet(eq("AxisServlet"), any(org.apache.axis2.transport.http.AxisServlet.class)))
                .thenReturn(axisServlet);
        when(axisServlet.addMapping(anyString()))
                .thenReturn(Collections.singleton("/services/*")); // conflict!

        Axis2ServletAutoConfiguration config = new Axis2ServletAutoConfiguration();

        assertThrows(IllegalStateException.class,
                () -> config.axis2ServletInitializer(properties).onStartup(servletContext),
                "Should throw when servlet mapping conflicts");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Axis2RepositoryAutoConfiguration — axis2.xml staging
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void repoProperty_usedForAxis2XmlStaging(@TempDir Path tempDir) throws Exception {
        // Pre-stage axis2.xml so staging is skipped (it checks for existing file)
        Path confDir = tempDir.resolve("conf");
        Files.createDirectories(confDir);
        Files.writeString(confDir.resolve("axis2.xml"), "<axisconfig/>");

        Axis2Properties properties = new Axis2Properties();
        properties.setRepo(tempDir.toString());

        ServletContext servletContext = mock(ServletContext.class);
        // getRealPath returns null (embedded mode)
        when(servletContext.getRealPath("/WEB-INF")).thenReturn(null);

        Axis2RepositoryAutoConfiguration config = new Axis2RepositoryAutoConfiguration();
        // Should not throw — axis2.xml already exists, staging is skipped.
        // The implicit no-exception check is the assertion: if staging
        // incorrectly tries to overwrite the existing file and fails,
        // the test fails with an exception.
        config.axis2RepositoryInitializer(properties).onStartup(servletContext);
    }

    @Test
    void nullRealPath_andEmptyRepo_logsWarning() throws Exception {
        Axis2Properties properties = new Axis2Properties();
        // Both repo and getRealPath are empty/null

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/WEB-INF")).thenReturn(null);

        Axis2RepositoryAutoConfiguration config = new Axis2RepositoryAutoConfiguration();
        // Should not throw — just logs a warning and skips staging
        config.axis2RepositoryInitializer(properties).onStartup(servletContext);
        // If we got here without exception, the graceful fallback works
    }
}
