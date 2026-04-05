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

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.openapi.OpenApiModule;
import org.apache.axis2.openapi.SwaggerUIHandler;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Routes OpenAPI documentation requests to the Axis2 OpenAPI module handlers.
 *
 * The axis2-openapi module stores its handlers in ConfigurationContext during
 * module init. AxisServlet stores ConfigurationContext in ServletContext under
 * the key AxisServlet.CONFIGURATION_CONTEXT ("CONFIGURATION_CONTEXT"). This
 * controller bridges Spring MVC routing to those handlers, enabling the standard
 * OpenAPI endpoints alongside the Axis2 /services/* path.
 *
 * Endpoints served:
 *   GET /openapi.json  - OpenAPI 3.0.1 specification (JSON)
 *   GET /openapi.yaml  - OpenAPI 3.0.1 specification (YAML)
 *   GET /swagger-ui    - Interactive Swagger UI documentation page
 */
@Controller
public class OpenApiController {

    private static final Log log = LogFactory.getLog(OpenApiController.class);

    @Autowired
    private ServletContext servletContext;

    private SwaggerUIHandler getHandler() {
        ConfigurationContext configContext = (ConfigurationContext)
                servletContext.getAttribute(AxisServlet.CONFIGURATION_CONTEXT);
        if (configContext == null) {
            log.warn("AxisServlet ConfigurationContext not found in ServletContext — AxisServlet may not have started yet");
            return null;
        }
        SwaggerUIHandler handler = OpenApiModule.getSwaggerUIHandler(configContext);
        if (handler == null) {
            log.warn("OpenAPI module not initialized — ensure axis2-openapi is on the classpath and <module ref=\"openapi\"/> is in axis2.xml");
        }
        return handler;
    }

    @GetMapping("/openapi.json")
    public void openApiJson(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SwaggerUIHandler handler = getHandler();
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OpenAPI module not available");
            return;
        }
        handler.handleOpenApiJsonRequest(request, response);
    }

    @GetMapping("/openapi.yaml")
    public void openApiYaml(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SwaggerUIHandler handler = getHandler();
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OpenAPI module not available");
            return;
        }
        handler.handleOpenApiYamlRequest(request, response);
    }

    @GetMapping("/swagger-ui")
    public void swaggerUi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SwaggerUIHandler handler = getHandler();
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OpenAPI module not available");
            return;
        }
        handler.handleSwaggerUIRequest(request, response);
    }
}
