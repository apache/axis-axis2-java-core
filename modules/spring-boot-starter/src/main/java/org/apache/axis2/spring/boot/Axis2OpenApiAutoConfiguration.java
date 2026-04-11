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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.openapi.OpenApiModule;
import org.apache.axis2.openapi.SwaggerUIHandler;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Registers the OpenAPI/MCP servlet when {@code axis2-openapi} is on the classpath.
 *
 * <p>This eliminates the need for consuming apps to provide their own
 * {@code OpenApiServlet} class. The servlet delegates to Axis2's
 * {@link SwaggerUIHandler} for all four endpoints:
 * <ul>
 *   <li>{@code /openapi.json} — OpenAPI 3.0.1 specification (JSON)</li>
 *   <li>{@code /openapi.yaml} — OpenAPI 3.0.1 specification (YAML)</li>
 *   <li>{@code /swagger-ui} — Interactive Swagger UI</li>
 *   <li>{@code /openapi-mcp.json} — MCP tool catalog</li>
 * </ul>
 *
 * <p>Disabled by setting {@code axis2.openapi.enabled=false} or by removing
 * {@code axis2-openapi} from the classpath.
 */
@AutoConfiguration(after = Axis2ServletAutoConfiguration.class)
@ConditionalOnClass({AxisServlet.class, OpenApiModule.class})
@ConditionalOnProperty(prefix = "axis2.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Axis2OpenApiAutoConfiguration {

    private static final Log log = LogFactory.getLog(Axis2OpenApiAutoConfiguration.class);

    @Bean
    public ServletContextInitializer openApiServletInitializer(Axis2Properties properties) {
        return (servletContext) -> {
            String[] paths = properties.getOpenapi().getPaths();
            ServletRegistration.Dynamic openApi = servletContext.addServlet(
                    "OpenApiServlet", new Axis2OpenApiServlet());
            openApi.setLoadOnStartup(2);
            openApi.addMapping(paths);
            log.info("OpenApiServlet registered at " + String.join(", ", paths));
        };
    }

    /**
     * Built-in OpenAPI servlet that delegates to Axis2's SwaggerUIHandler.
     * Replaces the hand-coded OpenApiServlet in consuming applications.
     */
    static class Axis2OpenApiServlet extends HttpServlet {

        private static final Log log = LogFactory.getLog(Axis2OpenApiServlet.class);

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            String uri = request.getRequestURI();

            ConfigurationContext configContext = (ConfigurationContext)
                    getServletContext().getAttribute(AxisServlet.CONFIGURATION_CONTEXT);
            if (configContext == null) {
                log.warn("AxisServlet ConfigurationContext not found in ServletContext");
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Axis2 not initialized — AxisServlet must load before OpenApiServlet");
                return;
            }

            SwaggerUIHandler handler = OpenApiModule.getSwaggerUIHandler(configContext);
            if (handler == null) {
                log.warn("SwaggerUIHandler not found — ensure openapi .mar is in WEB-INF/modules");
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "OpenAPI module not initialized");
                return;
            }

            try {
                if (uri.endsWith("/openapi.json")) {
                    handler.handleOpenApiJsonRequest(request, response);
                } else if (uri.endsWith("/openapi.yaml")) {
                    handler.handleOpenApiYamlRequest(request, response);
                } else if (uri.endsWith("/swagger-ui") || uri.contains("/swagger-ui/")) {
                    handler.handleSwaggerUIRequest(request, response);
                } else if (uri.endsWith("/openapi-mcp.json")) {
                    handler.handleMcpCatalogRequest(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("OpenApiServlet error handling " + uri + ": " + e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Internal Server Error");
            }
        }
    }
}
