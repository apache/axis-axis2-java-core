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
package userguide.springboot.configuration;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.openapi.OpenApiModule;
import org.apache.axis2.openapi.SwaggerUIHandler;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Servlet that serves OpenAPI documentation endpoints by delegating to
 * the Axis2 OpenAPI module's SwaggerUIHandler.
 *
 * Registered directly in Axis2WebAppInitializer at:
 *   /openapi.json  - OpenAPI 3.0.1 specification (JSON)
 *   /openapi.yaml  - OpenAPI 3.0.1 specification (YAML)
 *   /swagger-ui    - Interactive Swagger UI documentation
 */
public class OpenApiServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(OpenApiServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        log.info("OpenApiServlet.doGet() called for URI: " + uri);

        ConfigurationContext configContext = (ConfigurationContext)
                getServletContext().getAttribute(AxisServlet.CONFIGURATION_CONTEXT);
        if (configContext == null) {
            log.warn("AxisServlet ConfigurationContext not found in ServletContext");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OpenAPI module not available");
            return;
        }

        SwaggerUIHandler handler = OpenApiModule.getSwaggerUIHandler(configContext);
        if (handler == null) {
            log.warn("OpenAPI SwaggerUIHandler not found — ensure openapi module is in WEB-INF/modules");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OpenAPI module not initialized");
            return;
        }

        try {
            if (uri.endsWith("/openapi.json")) {
                handler.handleOpenApiJsonRequest(request, response);
            } else if (uri.endsWith("/openapi.yaml")) {
                handler.handleOpenApiYamlRequest(request, response);
            } else if (uri.contains("/swagger-ui")) {
                handler.handleSwaggerUIRequest(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("OpenApiServlet error handling " + uri + ": " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
