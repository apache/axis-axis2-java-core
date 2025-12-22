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
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles Swagger UI serving for interactive API documentation.
 *
 * This class provides a comprehensive Swagger UI implementation that can be served
 * directly from Axis2 with extensive customization capabilities. Enhanced in v2.0.1
 * with full configuration system integration.
 *
 * Key features:
 * - Configuration-driven UI customization via SwaggerUiConfig
 * - Support for custom CSS and JavaScript injection
 * - Multiple resource serving modes (CDN, local, hybrid)
 * - CORS handling and security configuration
 * - Custom media type handling
 * - Resource caching and optimization
 */
public class SwaggerUIHandler {

    private static final Log log = LogFactory.getLog(SwaggerUIHandler.class);

    private final ConfigurationContext configurationContext;
    private final OpenApiSpecGenerator specGenerator;
    private final OpenApiConfiguration configuration;
    private final SwaggerUiConfig swaggerUiConfig;

    // Default Swagger UI version (can be overridden by configuration)
    private static final String DEFAULT_SWAGGER_UI_VERSION = "4.15.5";

    // Default resource paths
    private static final String SWAGGER_UI_ROOT = "/swagger-ui/";
    private static final String API_DOCS_PATH = "/api-docs/";

    /**
     * Constructor with default configuration.
     */
    public SwaggerUIHandler(ConfigurationContext configContext) {
        this(configContext, new OpenApiConfiguration());
    }

    /**
     * Constructor with custom configuration.
     */
    public SwaggerUIHandler(ConfigurationContext configContext, OpenApiConfiguration config) {
        this.configurationContext = configContext;
        this.configuration = config != null ? config : new OpenApiConfiguration();
        this.swaggerUiConfig = this.configuration.getSwaggerUiConfig();
        this.specGenerator = new OpenApiSpecGenerator(configContext, this.configuration);

        log.debug("SwaggerUIHandler initialized with configuration: " + this.configuration);
    }

    /**
     * Handle Swagger UI request and serve the interactive documentation page.
     * Enhanced in v2.0.1 with configuration-driven customization.
     *
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @throws IOException if response writing fails
     */
    public void handleSwaggerUIRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.debug("Serving Swagger UI for OpenAPI documentation with configuration");

        // Check if Swagger UI is enabled
        if (!configuration.isSupportSwaggerUi()) {
            log.warn("Swagger UI is disabled in configuration");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Swagger UI is not available");
            return;
        }

        response.setContentType("text/html; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Add security headers
        addSecurityHeaders(response);

        // Build OpenAPI specification URL from configuration
        String openApiUrl = buildOpenApiUrl(request);

        // Generate customized Swagger UI HTML
        String swaggerHtml = generateSwaggerUIHtml(openApiUrl, request);

        PrintWriter writer = response.getWriter();
        writer.write(swaggerHtml);
        writer.flush();
    }

    /**
     * Handle OpenAPI specification serving (JSON format).
     * Enhanced in v2.0.1 with configuration-driven response formatting.
     */
    public void handleOpenApiJsonRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.debug("Serving OpenAPI specification in JSON format");

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Add security headers
        addSecurityHeaders(response);

        // Enable CORS for browser access (configurable)
        addCorsHeaders(response);

        // Add caching headers if configured
        addCachingHeaders(response);

        try {
            String openApiJson = specGenerator.generateOpenApiJson(request);

            PrintWriter writer = response.getWriter();
            writer.write(openApiJson);
            writer.flush();

        } catch (Exception e) {
            log.error("Failed to generate OpenAPI JSON specification", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                              "Failed to generate OpenAPI specification");
        }
    }

    /**
     * Handle OpenAPI specification serving (YAML format).
     * Enhanced in v2.0.1 with configuration-driven response formatting.
     */
    public void handleOpenApiYamlRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.debug("Serving OpenAPI specification in YAML format");

        response.setContentType("application/yaml; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Add security headers
        addSecurityHeaders(response);

        // Enable CORS for browser access (configurable)
        addCorsHeaders(response);

        // Add caching headers if configured
        addCachingHeaders(response);

        try {
            String openApiYaml = specGenerator.generateOpenApiYaml(request);

            PrintWriter writer = response.getWriter();
            writer.write(openApiYaml);
            writer.flush();

        } catch (Exception e) {
            log.error("Failed to generate OpenAPI YAML specification", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                              "Failed to generate OpenAPI specification");
        }
    }

    /**
     * Build the URL for the OpenAPI specification endpoint using configuration.
     */
    private String buildOpenApiUrl(HttpServletRequest request) {
        // Check if URL is configured in SwaggerUiConfig
        if (swaggerUiConfig != null && swaggerUiConfig.getUrl() != null) {
            String configuredUrl = swaggerUiConfig.getUrl();
            if (configuredUrl.startsWith("http")) {
                return configuredUrl; // Absolute URL
            } else if (configuredUrl.startsWith("/")) {
                // Relative URL - build full URL
                return buildBaseUrl(request) + configuredUrl;
            } else {
                // Relative path - append to current path
                return buildBaseUrl(request) + "/" + configuredUrl;
            }
        }

        // Build default URL
        StringBuilder url = new StringBuilder();
        url.append(buildBaseUrl(request));
        url.append("/openapi.json");
        return url.toString();
    }

    /**
     * Build the base URL from request information.
     */
    private String buildBaseUrl(HttpServletRequest request) {
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

        return url.toString();
    }

    /**
     * Generate HTML for Swagger UI page with configuration-driven customization.
     */
    private String generateSwaggerUIHtml(String openApiUrl, HttpServletRequest request) {
        String swaggerUiVersion = configuration.getSwaggerUiVersion() != null ?
                configuration.getSwaggerUiVersion() : DEFAULT_SWAGGER_UI_VERSION;

        String title = configuration.getTitle() + " - API Documentation";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>").append(escapeHtml(title)).append("</title>\n")
            .append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://unpkg.com/swagger-ui-dist@")
            .append(swaggerUiVersion).append("/swagger-ui.css\" />\n");

        // Add custom CSS if configured
        if (swaggerUiConfig.getCustomCss() != null) {
            html.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"")
                .append(swaggerUiConfig.getCustomCss()).append("\" />\n");
        }

        // Add default and custom styles
        html.append(generateDefaultStyles());

        html.append("</head>\n")
            .append("<body>\n");

        // Add custom header
        html.append(generateHeader());

        html.append("    <div id=\"swagger-ui\"></div>\n");

        // Add Swagger UI scripts
        html.append("    <script src=\"https://unpkg.com/swagger-ui-dist@")
            .append(swaggerUiVersion).append("/swagger-ui-bundle.js\"></script>\n")
            .append("    <script src=\"https://unpkg.com/swagger-ui-dist@")
            .append(swaggerUiVersion).append("/swagger-ui-standalone-preset.js\"></script>\n");

        // Add Swagger UI initialization script
        html.append(generateSwaggerUIScript(openApiUrl));

        // Add custom JavaScript if configured
        if (swaggerUiConfig.getCustomJs() != null) {
            html.append("    <script src=\"").append(swaggerUiConfig.getCustomJs()).append("\"></script>\n");
        }

        html.append("</body>\n")
            .append("</html>");

        return html.toString();
    }

    /**
     * Generate default CSS styles for Swagger UI.
     */
    private String generateDefaultStyles() {
        return "    <style>\n" +
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
               "        .axis2-header {\n" +
               "            background: #1976d2;\n" +
               "            color: white;\n" +
               "            padding: 1rem;\n" +
               "            text-align: center;\n" +
               "            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
               "        }\n" +
               "        .axis2-header h1 {\n" +
               "            margin: 0;\n" +
               "            font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;\n" +
               "        }\n" +
               "        .axis2-header p {\n" +
               "            margin: 0.5rem 0 0 0;\n" +
               "            opacity: 0.9;\n" +
               "        }\n" +
               "        .swagger-ui .topbar {\n" +
               "            display: none;\n" +
               "        }\n" +
               "        .swagger-ui .info .title {\n" +
               "            color: #1976d2;\n" +
               "        }\n" +
               "        .swagger-ui .scheme-container {\n" +
               "            background: #f8f9fa;\n" +
               "            border: 1px solid #dee2e6;\n" +
               "        }\n" +
               "    </style>\n";
    }

    /**
     * Generate header HTML section.
     */
    private String generateHeader() {
        return "    <div class=\"axis2-header\">\n" +
               "        <h1>" + escapeHtml(configuration.getTitle()) + "</h1>\n" +
               "        <p>" + escapeHtml(configuration.getDescription()) + "</p>\n" +
               "    </div>\n\n";
    }

    /**
     * Generate Swagger UI initialization script with configuration.
     */
    private String generateSwaggerUIScript(String openApiUrl) {
        String configJs = swaggerUiConfig.toJavaScriptConfig();

        StringBuilder script = new StringBuilder();
        script.append("    <script>\n")
              .append("        window.onload = function() {\n")
              .append("            const ui = SwaggerUIBundle(Object.assign(")
              .append(configJs).append(", {\n")
              .append("                url: '").append(openApiUrl).append("',\n")
              .append("                dom_id: '#swagger-ui',\n")
              .append("                presets: [\n")
              .append("                    SwaggerUIBundle.presets.apis,\n")
              .append("                    SwaggerUIStandalonePreset\n")
              .append("                ],\n")
              .append("                plugins: [\n")
              .append("                    SwaggerUIBundle.plugins.DownloadUrl\n")
              .append("                ],\n")
              .append("                layout: \"StandaloneLayout\",\n")
              .append("                requestInterceptor: function(request) {\n")
              .append("                    // Add any request modifications here\n")
              .append("                    return request;\n")
              .append("                },\n")
              .append("                responseInterceptor: function(response) {\n")
              .append("                    // Add any response modifications here\n")
              .append("                    return response;\n")
              .append("                }\n")
              .append("            }));\n")
              .append("        };\n")
              .append("    </script>\n");

        return script.toString();
    }

    // ========== Resource Handling Methods ==========

    /**
     * Handle static resource requests (CSS, JS, images) for Swagger UI.
     */
    public void handleResourceRequest(HttpServletRequest request, HttpServletResponse response,
                                     String resourcePath) throws IOException {

        log.debug("Serving Swagger UI resource: " + resourcePath);

        // Determine content type from file extension
        String contentType = getContentType(resourcePath);
        if (contentType != null) {
            response.setContentType(contentType);
        }

        // Add caching headers for static resources
        addResourceCachingHeaders(response);

        // Try to load resource from classpath or CDN
        byte[] resourceContent = loadResource(resourcePath);
        if (resourceContent != null) {
            response.getOutputStream().write(resourceContent);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + resourcePath);
        }
    }

    // ========== Security and Headers Methods ==========

    /**
     * Add security headers to response.
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "1; mode=block");
    }

    /**
     * Add CORS headers to response based on configuration.
     */
    private void addCorsHeaders(HttpServletResponse response) {
        // Basic CORS support - can be enhanced with configuration
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Add caching headers to response.
     */
    private void addCachingHeaders(HttpServletResponse response) {
        // Add basic caching for API specs (can be configured)
        response.setHeader("Cache-Control", "public, max-age=300"); // 5 minutes
    }

    /**
     * Add caching headers for static resources.
     */
    private void addResourceCachingHeaders(HttpServletResponse response) {
        // Static resources can be cached longer
        response.setHeader("Cache-Control", "public, max-age=86400"); // 24 hours
    }

    // ========== Utility Methods ==========

    /**
     * Get content type for a resource based on file extension.
     */
    private String getContentType(String resourcePath) {
        Map<String, String> mediaTypes = configuration.getSwaggerUiMediaTypes();
        if (mediaTypes != null) {
            String extension = getFileExtension(resourcePath);
            return mediaTypes.get(extension);
        }

        // Fallback to basic content type detection
        if (resourcePath.endsWith(".css")) return "text/css";
        if (resourcePath.endsWith(".js")) return "application/javascript";
        if (resourcePath.endsWith(".json")) return "application/json";
        if (resourcePath.endsWith(".png")) return "image/png";
        if (resourcePath.endsWith(".ico")) return "image/x-icon";

        return null;
    }

    /**
     * Get file extension from path.
     */
    private String getFileExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : "";
    }

    /**
     * Load resource content from classpath or generate dynamically.
     */
    private byte[] loadResource(String resourcePath) {
        try {
            // Try to load from classpath first
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is != null) {
                return readInputStream(is);
            }

            // Resource not found
            return null;

        } catch (Exception e) {
            log.warn("Failed to load resource: " + resourcePath, e);
            return null;
        }
    }

    /**
     * Read input stream to byte array.
     */
    private byte[] readInputStream(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } finally {
            is.close();
        }
    }

    /**
     * Escape HTML special characters.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    // ========== Getters for Configuration Access ==========

    /**
     * Get the current configuration.
     */
    public OpenApiConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get the Swagger UI configuration.
     */
    public SwaggerUiConfig getSwaggerUiConfig() {
        return swaggerUiConfig;
    }

    /**
     * Get the OpenAPI specification generator.
     */
    public OpenApiSpecGenerator getSpecGenerator() {
        return specGenerator;
    }
}