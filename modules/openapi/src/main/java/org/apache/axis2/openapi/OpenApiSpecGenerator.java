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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.axis2.json.moshih2.JsonProcessingMetrics;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates OpenAPI 3.0.1 specifications from Axis2 service metadata.
 *
 * This class introspects deployed Axis2 services and generates comprehensive
 * OpenAPI documentation including paths, operations, request/response schemas,
 * and server information.
 *
 * Enhanced in v2.0.1 with comprehensive configuration support, security schemes,
 * and customization capabilities.
 */
public class OpenApiSpecGenerator {

    private static final Log log = LogFactory.getLog(OpenApiSpecGenerator.class);

    private final ConfigurationContext configurationContext;
    private final ServiceIntrospector serviceIntrospector;
    private final ObjectMapper objectMapper;
    private final JsonProcessingMetrics metrics;
    private final OpenApiConfiguration configuration;

    /**
     * Constructor with default configuration.
     */
    public OpenApiSpecGenerator(ConfigurationContext configContext) {
        this(configContext, new OpenApiConfiguration());
    }

    /**
     * Constructor with custom configuration.
     */
    public OpenApiSpecGenerator(ConfigurationContext configContext, OpenApiConfiguration config) {
        this.configurationContext = configContext;
        this.configuration = config != null ? config : new OpenApiConfiguration();
        this.serviceIntrospector = new ServiceIntrospector(configContext);

        // Configure Jackson for OpenAPI model serialization with HTTP/2 optimization metrics
        this.objectMapper = new ObjectMapper();

        if (configuration.isPrettyPrint()) {
            this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        }

        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // Initialize performance metrics from moshih2 package for HTTP/2 optimization tracking
        this.metrics = new JsonProcessingMetrics();

        log.info("OpenAPI JSON processing configured with Jackson + Enhanced HTTP/2 Metrics (moshih2 performance tracking enabled)");
    }

    /**
     * Generate complete OpenAPI specification for all deployed services.
     *
     * @param request HTTP request for server URL context
     * @return OpenAPI specification object
     */
    public OpenAPI generateOpenApiSpec(HttpServletRequest request) {
        log.debug("Generating OpenAPI specification for Axis2 services using configuration: " + configuration);

        OpenAPI openApi = new OpenAPI();
        openApi.setOpenapi("3.0.1");

        // Set API information from configuration
        openApi.setInfo(createApiInfo());

        // Set servers based on request context and configuration
        openApi.setServers(createServerList(request));

        // Generate paths from services with filtering
        openApi.setPaths(generatePaths());

        // Add security schemes from configuration
        addSecuritySchemes(openApi);

        // Add components/schemas section
        addComponents(openApi);

        // Apply customizer if configured
        applyCustomizer(openApi);

        return openApi;
    }

    /**
     * Generate OpenAPI specification as JSON string.
     */
    public String generateOpenApiJson(HttpServletRequest request) {
        String requestId = "openapi-" + System.currentTimeMillis();
        try {
            OpenAPI spec = generateOpenApiSpec(request);

            // Use Jackson processing with enhanced HTTP/2 performance metrics tracking
            long startTime = System.currentTimeMillis();
            String jsonSpec = objectMapper.writeValueAsString(spec);
            long processingTime = System.currentTimeMillis() - startTime;

            // Record performance metrics using moshih2 infrastructure
            long specSize = jsonSpec.getBytes().length;
            metrics.recordProcessingStart(requestId, specSize, false);
            metrics.recordProcessingComplete(requestId, specSize, processingTime);

            // Pretty printing is handled by Jackson configuration in constructor

            log.debug("Generated OpenAPI JSON specification (" + (specSize / 1024) + "KB) in " + processingTime + "ms using Jackson with HTTP/2 metrics");
            return jsonSpec;
        } catch (Exception e) {
            long errorTime = 0; // Error occurred, no meaningful processing time
            metrics.recordProcessingError(requestId, e, errorTime);
            log.error("Failed to generate OpenAPI JSON using Jackson with HTTP/2 metrics", e);
            return "{\"error\":\"Failed to generate OpenAPI specification\"}";
        }
    }

    /**
     * Generate OpenAPI specification as YAML string.
     */
    public String generateOpenApiYaml(HttpServletRequest request) {
        // For now, return JSON - YAML conversion can be added later
        return generateOpenApiJson(request);
    }

    /**
     * Create API information section from configuration.
     */
    private Info createApiInfo() {
        Info info = new Info();
        info.setTitle(configuration.getTitle());
        info.setDescription(configuration.getDescription());
        info.setVersion(configuration.getVersion());

        if (configuration.getTermsOfServiceUrl() != null) {
            info.setTermsOfService(configuration.getTermsOfServiceUrl());
        }

        // Add contact information if configured
        if (configuration.getContactName() != null || configuration.getContactEmail() != null ||
            configuration.getContactUrl() != null) {
            Contact contact = new Contact();
            contact.setName(configuration.getContactName());
            contact.setEmail(configuration.getContactEmail());
            contact.setUrl(configuration.getContactUrl());
            info.setContact(contact);
        }

        // Add license information if configured
        if (configuration.getLicense() != null || configuration.getLicenseUrl() != null) {
            License license = new License();
            license.setName(configuration.getLicense());
            license.setUrl(configuration.getLicenseUrl());
            info.setLicense(license);
        }

        return info;
    }

    /**
     * Create server list based on request context.
     */
    private List<Server> createServerList(HttpServletRequest request) {
        List<Server> servers = new ArrayList<>();

        if (request != null) {
            // Build server URL from request
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();

            StringBuilder serverUrl = new StringBuilder();
            serverUrl.append(scheme).append("://").append(serverName);

            // Add port if not default
            if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
                serverUrl.append(":").append(serverPort);
            }

            if (contextPath != null && !contextPath.isEmpty()) {
                serverUrl.append(contextPath);
            }

            Server server = new Server();
            server.setUrl(serverUrl.toString());
            server.setDescription("Current server");
            servers.add(server);
        } else {
            // Default server
            Server server = new Server();
            server.setUrl("http://localhost:8080");
            server.setDescription("Default server");
            servers.add(server);
        }

        return servers;
    }

    /**
     * Generate paths from all deployed services with configuration-based filtering.
     */
    private Paths generatePaths() {
        Paths paths = new Paths();

        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

            // Iterate through all services
            Iterator<AxisService> services = axisConfig.getServices().values().iterator();
            while (services.hasNext()) {
                AxisService service = services.next();

                // Skip system services
                if (isSystemService(service)) {
                    continue;
                }

                // Apply resource filtering from configuration
                if (!shouldIncludeService(service)) {
                    log.debug("Skipping service due to configuration filters: " + service.getName());
                    continue;
                }

                log.debug("Processing service: " + service.getName());

                // Generate paths for this service
                generateServicePaths(service, paths);
            }

        } catch (Exception e) {
            log.error("Failed to generate paths from services", e);
        }

        return paths;
    }

    /**
     * Generate paths for a specific service.
     */
    private void generateServicePaths(AxisService service, Paths paths) {
        try {
            // Get REST-enabled operations
            Iterator<AxisOperation> operations = service.getOperations();
            while (operations.hasNext()) {
                AxisOperation operation = operations.next();

                // Generate path for REST operation
                String path = generateOperationPath(service, operation);
                if (path != null && !isIgnoredRoute(path)) {
                    PathItem pathItem = paths.get(path);
                    if (pathItem == null) {
                        pathItem = new PathItem();
                        paths.addPathItem(path, pathItem);
                    }

                    // Add operation details
                    Operation openApiOp = generateOperation(service, operation);
                    pathItem.setPost(openApiOp); // Assuming POST for now
                }
            }

        } catch (Exception e) {
            log.error("Failed to generate paths for service: " + service.getName(), e);
        }
    }

    /**
     * Generate operation path from service and operation metadata.
     */
    private String generateOperationPath(AxisService service, AxisOperation operation) {
        // Simple path generation - can be enhanced based on REST configuration
        return "/services/" + service.getName() + "/" + operation.getName().getLocalPart();
    }

    /**
     * Generate OpenAPI operation from Axis operation.
     */
    private Operation generateOperation(AxisService service, AxisOperation axisOperation) {
        Operation operation = new Operation();
        operation.setOperationId(axisOperation.getName().getLocalPart());
        operation.setSummary("Service operation: " + axisOperation.getName().getLocalPart());
        operation.setDescription("Generated from Axis2 service: " + service.getName());

        // Add tags
        List<String> tags = new ArrayList<>();
        tags.add(service.getName());
        operation.setTags(tags);

        // Add responses
        ApiResponses responses = new ApiResponses();

        ApiResponse successResponse = new ApiResponse();
        successResponse.setDescription("Successful operation");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        Schema schema = new Schema();
        schema.setType("object");
        mediaType.setSchema(schema);
        content.addMediaType("application/json", mediaType);
        successResponse.setContent(content);

        responses.addApiResponse("200", successResponse);
        operation.setResponses(responses);

        return operation;
    }

    /**
     * Check if service is a system service that should be excluded.
     */
    private boolean isSystemService(AxisService service) {
        String serviceName = service.getName();
        return serviceName.equals("Version") ||
               serviceName.equals("AdminService") ||
               serviceName.startsWith("__") ||
               serviceName.contains("AdminService");
    }

    /**
     * Check if a service should be included based on configuration filters.
     */
    private boolean shouldIncludeService(AxisService service) {
        String serviceName = service.getName();
        String servicePackage = getServicePackage(service);

        // If readAllResources is false, check specific resource classes/packages
        if (!configuration.isReadAllResources()) {
            // Check if service class is in configured resource classes
            if (!configuration.getResourceClasses().isEmpty()) {
                String serviceClass = getServiceClassName(service);
                if (serviceClass != null && !configuration.getResourceClasses().contains(serviceClass)) {
                    return false;
                }
            }

            // Check if service package is in configured resource packages
            if (!configuration.getResourcePackages().isEmpty()) {
                if (servicePackage == null || !isPackageIncluded(servicePackage)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if a route path should be ignored based on configuration.
     */
    private boolean isIgnoredRoute(String path) {
        if (configuration.getIgnoredRoutes().isEmpty()) {
            return false;
        }

        for (String ignoredRoute : configuration.getIgnoredRoutes()) {
            if (path.matches(ignoredRoute) || path.contains(ignoredRoute)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add security schemes from configuration to OpenAPI specification.
     */
    private void addSecuritySchemes(OpenAPI openApi) {
        if (configuration.getSecurityDefinitions().isEmpty()) {
            return;
        }

        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components.setSecuritySchemes(configuration.getSecurityDefinitions());

        // Add security requirements to the API level
        addSecurityRequirements(openApi);

        log.debug("Added " + configuration.getSecurityDefinitions().size() + " security schemes to OpenAPI specification");
    }

    /**
     * Add security requirements to OpenAPI specification.
     */
    private void addSecurityRequirements(OpenAPI openApi) {
        // Add a security requirement for each defined security scheme
        for (String schemeName : configuration.getSecurityDefinitions().keySet()) {
            SecurityRequirement securityRequirement = new SecurityRequirement();
            securityRequirement.addList(schemeName);
            openApi.addSecurityItem(securityRequirement);
        }
    }

    /**
     * Add components section to OpenAPI specification.
     */
    private void addComponents(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        // TODO: Add schema definitions for request/response models
        // This can be enhanced to automatically generate schemas from service metadata

        openApi.setComponents(components);
    }


    /**
     * Apply customizer to OpenAPI specification if configured.
     */
    private void applyCustomizer(OpenAPI openApi) {
        OpenApiCustomizer customizer = configuration.getCustomizer();
        if (customizer == null) {
            return;
        }

        try {
            if (customizer.shouldApply(openApi)) {
                log.debug("Applying OpenAPI customizer: " + customizer.getClass().getSimpleName());
                customizer.customize(openApi);
            }
        } catch (Exception e) {
            log.error("Error applying OpenAPI customizer", e);
        }
    }

    // ========== Utility Methods ==========

    /**
     * Get the package name of a service.
     */
    private String getServicePackage(AxisService service) {
        try {
            String serviceClass = getServiceClassName(service);
            if (serviceClass != null && serviceClass.contains(".")) {
                return serviceClass.substring(0, serviceClass.lastIndexOf('.'));
            }
        } catch (Exception e) {
            log.debug("Could not determine package for service: " + service.getName(), e);
        }
        return null;
    }

    /**
     * Get the class name of a service.
     */
    private String getServiceClassName(AxisService service) {
        try {
            if (service.getParameter("ServiceClass") != null) {
                return (String) service.getParameter("ServiceClass").getValue();
            }
        } catch (Exception e) {
            log.debug("Could not determine class name for service: " + service.getName(), e);
        }
        return null;
    }

    /**
     * Check if a package is included in the configured resource packages.
     */
    private boolean isPackageIncluded(String packageName) {
        for (String configuredPackage : configuration.getResourcePackages()) {
            if (packageName.equals(configuredPackage) ||
                packageName.startsWith(configuredPackage + ".")) {
                return true;
            }
        }
        return false;
    }

    // ========== Getters for Configuration Access ==========

    /**
     * Get the current configuration.
     */
    public OpenApiConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get the configuration context.
     */
    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    /**
     * Get OpenAPI JSON processing performance statistics using moshih2 metrics.
     */
    public JsonProcessingMetrics.Statistics getProcessingStatistics() {
        return metrics.getStatistics();
    }

    /**
     * Get optimization recommendations for OpenAPI processing performance.
     */
    public String getOptimizationRecommendations() {
        JsonProcessingMetrics.Statistics stats = metrics.getStatistics();
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("OpenAPI JSON Processing Performance Analysis (Jackson + HTTP/2 Metrics):\n");
        recommendations.append("  - Total specifications generated: ").append(stats.getTotalRequests()).append("\n");
        recommendations.append("  - Average processing time: ").append(stats.getAverageProcessingTimeMs()).append("ms\n");
        recommendations.append("  - Total data processed: ").append(stats.getTotalBytes() / 1024).append("KB\n");

        if (stats.getSlowRequestCount() > 0) {
            recommendations.append("  - Slow requests detected: ").append(stats.getSlowRequestCount()).append("\n");
            recommendations.append("    → Consider enabling HTTP/2 transport for better OpenAPI delivery performance\n");
        }

        if (stats.getTotalBytes() > (10 * 1024 * 1024)) { // > 10MB total
            recommendations.append("  - Large OpenAPI specifications detected\n");
            recommendations.append("    → HTTP/2 multiplexing provides 30-40% performance improvement for large specs\n");
            recommendations.append("    → Consider enabling compression for OpenAPI endpoints\n");
        }

        recommendations.append("  - Enhanced HTTP/2 metrics: Active (moshih2 performance tracking patterns applied)\n");
        return recommendations.toString();
    }
}