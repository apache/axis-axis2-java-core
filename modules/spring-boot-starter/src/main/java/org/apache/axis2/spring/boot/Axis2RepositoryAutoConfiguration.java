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

import org.apache.axis2.transport.http.AxisServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stages the Axis2 repository (axis2.xml, modules directory) into the WAR's
 * WEB-INF directory at startup time.
 *
 * <p><b>axis2.xml selection</b>: SOAP and JSON-RPC require completely different
 * axis2.xml configurations — different message receivers, different dispatchers,
 * and a different enableJSONOnly setting. These modes cannot be mixed in a
 * single deployment. The {@code axis2.mode} property selects which built-in
 * template to use:
 * <ul>
 *   <li><b>soap</b> — RawXMLINOutMessageReceiver, full SOAP dispatcher stack
 *       (SOAPAction, RequestURI, SOAPMessageBody, etc.)</li>
 *   <li><b>json</b> — JsonRpcMessageReceiver, JSONBasedDefaultDispatcher</li>
 * </ul>
 *
 * <p>If {@code axis2.configuration-file} is set, the custom file is used instead
 * and {@code axis2.mode} is ignored.
 *
 * <p><b>.aar file staging</b>: Phase 1 requires pre-built .aar files to be
 * placed in WEB-INF/services/ by the Maven build (via maven-antrun-plugin or
 * axis2-aar-maven-plugin). The starter does not generate .aar files at runtime.
 *
 * <p><b>.mar module staging</b>: When axis2-openapi is on the classpath, the
 * consuming app's Maven build must copy the .jar as a .mar into WEB-INF/modules/.
 * A future version of this starter may handle this automatically.
 */
@AutoConfiguration(before = Axis2ServletAutoConfiguration.class)
@ConditionalOnClass(AxisServlet.class)
@ConditionalOnProperty(prefix = "axis2", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Axis2RepositoryAutoConfiguration {

    private static final Log log = LogFactory.getLog(Axis2RepositoryAutoConfiguration.class);

    private static final String BUILTIN_SOAP_CONFIG = "META-INF/axis2/axis2-soap.xml";
    private static final String BUILTIN_JSON_CONFIG = "META-INF/axis2/axis2-json.xml";

    @Bean
    public ServletContextInitializer axis2RepositoryInitializer(Axis2Properties properties) {
        return (servletContext) -> {
            stageAxis2Config(servletContext, properties);
        };
    }

    private void stageAxis2Config(ServletContext servletContext, Axis2Properties properties) {
        String webInfPath = servletContext.getRealPath("/WEB-INF");
        if (webInfPath == null) {
            log.warn("Cannot resolve WEB-INF path — axis2.xml staging skipped. "
                    + "Ensure axis2.xml is pre-staged in WEB-INF/conf/");
            return;
        }

        Path confDir = Paths.get(webInfPath, "conf");
        Path axis2XmlTarget = confDir.resolve("axis2.xml");

        // If axis2.xml already exists (pre-staged by Maven build), don't overwrite
        if (Files.exists(axis2XmlTarget)) {
            log.info("axis2.xml already present at " + axis2XmlTarget + " — using existing");
            return;
        }

        // Resolve source: custom file or built-in template
        String source = resolveAxis2XmlSource(properties);
        log.info("Staging axis2.xml from " + source + " to " + axis2XmlTarget);

        try {
            ClassPathResource resource = new ClassPathResource(source);
            if (!resource.exists()) {
                log.error("axis2.xml source not found: " + source
                        + " — Axis2 will fail to start. Check axis2.mode or axis2.configuration-file");
                return;
            }

            Files.createDirectories(confDir);
            try (InputStream in = resource.getInputStream();
                 OutputStream out = Files.newOutputStream(axis2XmlTarget)) {
                in.transferTo(out);
            }
            log.info("Staged axis2.xml (" + properties.getMode() + " mode)");

        } catch (IOException e) {
            log.error("Failed to stage axis2.xml: " + e.getMessage(), e);
        }
    }

    private String resolveAxis2XmlSource(Axis2Properties properties) {
        // Custom file takes precedence
        String customFile = properties.getConfigurationFile();
        if (customFile != null && !customFile.isEmpty()) {
            // Strip "classpath:" prefix if present — ClassPathResource handles it
            if (customFile.startsWith("classpath:")) {
                return customFile.substring("classpath:".length());
            }
            return customFile;
        }

        // Built-in template based on mode
        String mode = properties.getMode();
        if ("soap".equalsIgnoreCase(mode)) {
            return BUILTIN_SOAP_CONFIG;
        }
        return BUILTIN_JSON_CONFIG;
    }
}
