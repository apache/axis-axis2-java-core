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
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import java.util.Set;

/**
 * Registers {@link AxisServlet} with the servlet container.
 *
 * <p>This replaces the hand-coded {@code Axis2WebAppInitializer} that every
 * Axis2 + Spring Boot project currently duplicates. The servlet is registered
 * at the path configured by {@code axis2.services-path} (default: /services).
 *
 * <p>The critical step is setting {@code axis2.repository.path} as a servlet
 * init-parameter. {@link WarBasedAxisConfigurator} uses this to locate
 * {@code WEB-INF/services/*.aar} and {@code WEB-INF/modules/*.mar}.
 * On WildFly, {@code ServletContext.getRealPath()} can fail due to VFS timing;
 * setting it eagerly at startup time bypasses this.
 */
@AutoConfiguration(after = Axis2AutoConfiguration.class)
@ConditionalOnClass(AxisServlet.class)
@ConditionalOnProperty(prefix = "axis2", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Axis2ServletAutoConfiguration {

    private static final Log log = LogFactory.getLog(Axis2ServletAutoConfiguration.class);

    @Bean
    public ServletContextInitializer axis2ServletInitializer(Axis2Properties properties) {
        return (ServletContext servletContext) -> {
            registerAxisServlet(servletContext, properties);
        };
    }

    private void registerAxisServlet(ServletContext servletContext, Axis2Properties properties) {
        // Register AxisServlet
        ServletRegistration.Dynamic axisServlet = servletContext.addServlet(
                "AxisServlet", new AxisServlet());
        axisServlet.setLoadOnStartup(1);

        // Set repository path — the critical init-parameter for WarBasedAxisConfigurator.
        // getRealPath() is called eagerly here to avoid WildFly VFS lazy-init issues.
        String webInfPath = servletContext.getRealPath("/WEB-INF");
        if (webInfPath != null) {
            axisServlet.setInitParameter(
                    WarBasedAxisConfigurator.PARAM_AXIS2_REPOSITORY_PATH, webInfPath);
            log.info("axis2.repository.path = " + webInfPath);
        } else {
            log.warn("ServletContext.getRealPath(\"/WEB-INF\") returned null — "
                    + "AxisServlet will attempt to resolve the repository path itself");
        }

        // Map to configured path
        String mapping = properties.getServicesPath() + "/*";
        Set<String> conflicts = axisServlet.addMapping(mapping);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException(
                    "AxisServlet could not be mapped to '" + mapping + "': " + conflicts);
        }

        log.info("AxisServlet registered at " + mapping);
    }
}
