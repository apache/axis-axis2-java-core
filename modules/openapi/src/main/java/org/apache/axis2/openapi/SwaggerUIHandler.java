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

package org.apache.axis2.openapi;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles Swagger UI serving for interactive API documentation.
 *
 * This class provides a lightweight Swagger UI implementation that can be served
 * directly from Axis2 without requiring external UI dependencies. It generates
 * a simple HTML page that loads Swagger UI from CDN and points to the generated
 * OpenAPI specification.
 */
public class SwaggerUIHandler {

    private static final Log log = LogFactory.getLog(SwaggerUIHandler.class);

    private final ConfigurationContext configurationContext;
    private final OpenApiSpecGenerator specGenerator;

    // Swagger UI version to use from CDN
    private static final String SWAGGER_UI_VERSION = "4.15.5";

    public SwaggerUIHandler(ConfigurationContext configContext) {
        this.configurationContext = configContext;
        this.specGenerator = new OpenApiSpecGenerator(configContext);
    }

    /**
     * Handle Swagger UI request and serve the interactive documentation page.
     *
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @throws IOException if response writing fails
     */
    public void handleSwaggerUIRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.debug("Serving Swagger UI for OpenAPI documentation");

        response.setContentType("text/html; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String openApiUrl = buildOpenApiUrl(request);
        String swaggerHtml = generateSwaggerUIHtml(openApiUrl);

        PrintWriter writer = response.getWriter();
        writer.write(swaggerHtml);
        writer.flush();
    }

    /**
     * Handle OpenAPI specification serving (JSON format).
     */
    public void handleOpenApiJsonRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.debug("Serving OpenAPI specification in JSON format");

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Enable CORS for browser access
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String openApiJson = specGenerator.generateOpenApiJson(request);

        PrintWriter writer = response.getWriter();
        writer.write(openApiJson);
        writer.flush();
    }

    /**
     * Handle OpenAPI specification serving (YAML format).
     */
    public void handleOpenApiYamlRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.debug("Serving OpenAPI specification in YAML format");

        response.setContentType("application/yaml; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Enable CORS for browser access
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String openApiYaml = specGenerator.generateOpenApiYaml(request);

        PrintWriter writer = response.getWriter();
        writer.write(openApiYaml);
        writer.flush();
    }

    /**
     * Build the URL for the OpenAPI specification endpoint.
     */
    private String buildOpenApiUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder();
        url.append(request.getScheme()).append("://");
        url.append(request.getServerName());

        if ((request.getScheme().equals("http") && request.getServerPort() != 80) ||
            (request.getScheme().equals("https") && request.getServerPort() != 443)) {
            url.append(":").append(request.getServerPort());
        }

        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            url.append(contextPath);
        }

        url.append("/openapi.json");
        return url.toString();
    }

    /**
     * Generate HTML for Swagger UI page.
     */
    private String generateSwaggerUIHtml(String openApiUrl) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Apache Axis2 - API Documentation</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://unpkg.com/swagger-ui-dist@" + SWAGGER_UI_VERSION + "/swagger-ui.css\" />\n" +
                "    <style>\n" +
                "        html {\n" +
                "            box-sizing: border-box;\n" +
                "            overflow: -moz-scrollbars-vertical;\n" +
                "            overflow-y: scroll;\n" +
                "        }\n" +
                "        *, *:before, *:after {\n" +
                "            box-sizing: inherit;\n" +
                "        }\n" +
                "        body {\n" +
                "            margin: 0;\n" +
                "            background: #fafafa;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background: #1976d2;\n" +
                "            color: white;\n" +
                "            padding: 1rem;\n" +
                "            text-align: center;\n" +
                "            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                "        }\n" +
                "        .header h1 {\n" +
                "            margin: 0;\n" +
                "            font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;\n" +
                "        }\n" +
                "        .swagger-ui .topbar {\n" +
                "            display: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"header\">\n" +
                "        <h1>Apache Axis2 REST API Documentation</h1>\n" +
                "        <p>Interactive OpenAPI documentation generated automatically from deployed services</p>\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"swagger-ui\"></div>\n" +
                "\n" +
                "    <script src=\"https://unpkg.com/swagger-ui-dist@" + SWAGGER_UI_VERSION + "/swagger-ui-bundle.js\"></script>\n" +
                "    <script src=\"https://unpkg.com/swagger-ui-dist@" + SWAGGER_UI_VERSION + "/swagger-ui-standalone-preset.js\"></script>\n" +
                "    <script>\n" +
                "        window.onload = function() {\n" +
                "            const ui = SwaggerUIBundle({\n" +
                "                url: '" + openApiUrl + "',\n" +
                "                dom_id: '#swagger-ui',\n" +
                "                deepLinking: true,\n" +
                "                presets: [\n" +
                "                    SwaggerUIBundle.presets.apis,\n" +
                "                    SwaggerUIStandalonePreset\n" +
                "                ],\n" +
                "                plugins: [\n" +
                "                    SwaggerUIBundle.plugins.DownloadUrl\n" +
                "                ],\n" +
                "                layout: \"StandaloneLayout\",\n" +
                "                tryItOutEnabled: true,\n" +
                "                requestInterceptor: function(request) {\n" +
                "                    // Add any request modifications here\n" +
                "                    return request;\n" +
                "                },\n" +
                "                responseInterceptor: function(response) {\n" +
                "                    // Add any response modifications here\n" +
                "                    return response;\n" +
                "                }\n" +
                "            });\n" +
                "\n" +
                "            // Custom styling\n" +
                "            const style = document.createElement('style');\n" +
                "            style.textContent = `\n" +
                "                .swagger-ui .info .title {\n" +
                "                    color: #1976d2;\n" +
                "                }\n" +
                "                .swagger-ui .scheme-container {\n" +
                "                    background: #f8f9fa;\n" +
                "                    border: 1px solid #dee2e6;\n" +
                "                }\n" +
                "            `;\n" +
                "            document.head.appendChild(style);\n" +
                "        };\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}