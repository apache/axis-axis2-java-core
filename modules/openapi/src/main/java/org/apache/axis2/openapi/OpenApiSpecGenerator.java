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
import io.swagger.v3.oas.models.parameters.RequestBody;
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

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;
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
    private final Moshi moshi;  // Preferred for general JSON operations
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

        // OpenAPI spec serialization delegates entirely to swagger-core's Json/Yaml utilities,
        // which already configure NON_NULL, WRITE_DATES_AS_TIMESTAMPS=false, and FAIL_ON_EMPTY_BEANS=false
        // on their internal Jackson mapper. No direct Jackson dependency is needed in this class.

        // Initialize Moshi for general JSON operations (Axis2 preference)
        this.moshi = new Moshi.Builder()
            .add(Date.class, new Rfc3339DateJsonAdapter())
            .build();

        // Initialize performance metrics from moshih2 package for HTTP/2 optimization tracking
        this.metrics = new JsonProcessingMetrics();

        log.info("OpenAPI spec generator configured: swagger-core Json/Yaml utilities for spec serialization, Moshi for general JSON, moshih2 HTTP/2 metrics enabled");
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

            long startTime = System.currentTimeMillis();
            String jsonSpec = configuration.isPrettyPrint() ? Json.pretty(spec) : Json.mapper().writeValueAsString(spec);
            long processingTime = System.currentTimeMillis() - startTime;

            long specSize = jsonSpec.getBytes().length;
            metrics.recordProcessingStart(requestId, specSize, false);
            metrics.recordProcessingComplete(requestId, specSize, processingTime);

            log.debug("Generated OpenAPI JSON specification (" + (specSize / 1024) + "KB) in " + processingTime + "ms");
            return jsonSpec;
        } catch (Exception e) {
            metrics.recordProcessingError(requestId, e, 0);
            log.error("Failed to generate OpenAPI JSON", e);
            return "{\"error\":\"Failed to generate OpenAPI specification\"}";
        }
    }

    /**
     * Generate OpenAPI specification as YAML string.
     */
    public String generateOpenApiYaml(HttpServletRequest request) {
        String requestId = "openapi-yaml-" + System.currentTimeMillis();
        try {
            OpenAPI spec = generateOpenApiSpec(request);
            long startTime = System.currentTimeMillis();
            String yamlSpec = configuration.isPrettyPrint() ? Yaml.pretty(spec) : Yaml.mapper().writeValueAsString(spec);
            long processingTime = System.currentTimeMillis() - startTime;
            long specSize = yamlSpec.getBytes().length;
            metrics.recordProcessingStart(requestId, specSize, false);
            metrics.recordProcessingComplete(requestId, specSize, processingTime);
            log.debug("Generated OpenAPI YAML specification (" + (specSize / 1024) + "KB) in " + processingTime + "ms");
            return yamlSpec;
        } catch (Exception e) {
            metrics.recordProcessingError(requestId, e, 0);
            log.error("Failed to generate OpenAPI YAML", e);
            return "error: Failed to generate OpenAPI specification";
        }
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
     * Check if an operation should be included based on {@code ignoredOperations}.
     * An entry matches if it equals either:
     * <ul>
     *   <li>{@code "ServiceName/operationName"} — targeted exclusion for one service</li>
     *   <li>{@code "operationName"} — excludes this operation name from every service</li>
     * </ul>
     */
    private boolean shouldIncludeOperation(AxisService service, AxisOperation operation) {
        String opName = operation.getName().getLocalPart();
        String qualified = service.getName() + "/" + opName;

        for (String entry : configuration.getIgnoredOperations()) {
            if (entry.equals(qualified) || entry.equals(opName)) {
                log.debug("Skipping operation excluded by ignoredOperations '" + entry + "': " + qualified);
                return false;
            }
        }
        return true;
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

                // Check per-operation exclusion before generating the path
                if (!shouldIncludeOperation(service, operation)) {
                    continue;
                }

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

        // Fix: requestBody was null on every generated operation. All services registered
        // here use JsonRpcMessageReceiver and expect a JSON POST body. Axis2 services have
        // no JAX-RS annotations or WSDL parameter metadata that would let us introspect
        // field-level schemas, so the body is typed as a generic "object" — sufficient for
        // Swagger UI to render a Try-It-Out editor and for clients to know a body is required.
        RequestBody requestBody = new RequestBody();
        requestBody.setRequired(true);
        // Sanitize operation name: Axis2 QName local parts follow XML NCName rules and
        // cannot contain angle brackets or control characters, but sanitize defensively
        // in case a malformed deployment descriptor produces unexpected characters.
        String safeOpName = axisOperation.getName().getLocalPart().replaceAll("[^\\w.\\-]", "_");
        requestBody.setDescription("JSON request body for " + safeOpName);
        Content requestContent = new Content();
        MediaType requestMediaType = new MediaType();
        Schema requestSchema = new Schema();
        requestSchema.setType("object");
        requestMediaType.setSchema(requestSchema);
        requestContent.addMediaType("application/json", requestMediaType);
        requestBody.setContent(requestContent);
        operation.setRequestBody(requestBody);

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
     * Exclusion is evaluated before inclusion: a service listed in
     * {@code ignoredServices} is always skipped regardless of other settings.
     */
    private boolean shouldIncludeService(AxisService service) {
        String serviceName = service.getName();

        // Explicit exclusion by name takes priority over all other filters
        if (configuration.getIgnoredServices().contains(serviceName)) {
            log.debug("Skipping service explicitly excluded by ignoredServices: " + serviceName);
            return false;
        }

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
     * Generate MCP tool catalog JSON for all deployed services.
     *
     * Produces the {@code /openapi-mcp.json} format consumed by axis2-mcp-bridge:
     * <pre>
     * {
     *   "tools": [
     *     { "name": "...", "description": "...",
     *       "inputSchema": { "type": "object", "properties": {}, "required": [] },
     *       "endpoint": "POST /services/ServiceName/operationName" }
     *   ]
     * }
     * </pre>
     *
     * Uses the same service filtering as {@link #generatePaths()} so the tool
     * catalog is consistent with the OpenAPI spec.
     */
    /**
     * Reads a string-valued MCP metadata parameter, checking the operation first
     * then the service, falling back to {@code defaultValue}.
     *
     * <p>Callers set this in {@code services.xml}:
     * <pre>
     * &lt;operation name="doFoo"&gt;
     *   &lt;parameter name="mcpDescription"&gt;Natural language description&lt;/parameter&gt;
     * &lt;/operation&gt;
     * </pre>
     */
    private String getMcpStringParam(AxisOperation operation, AxisService service,
                                     String paramName, String defaultValue) {
        org.apache.axis2.description.Parameter p = operation.getParameter(paramName);
        if (p == null) p = service.getParameter(paramName);
        if (p != null && p.getValue() != null) {
            String v = p.getValue().toString().trim();
            if (!v.isEmpty()) return v;
        }
        return defaultValue;
    }

    /**
     * Reads a boolean-valued MCP metadata parameter, checking the operation first
     * then the service, falling back to {@code defaultValue}.
     *
     * <p>Accepts "true" / "false" (case-insensitive). Any other value is treated as
     * {@code defaultValue}.
     */
    private boolean getMcpBoolParam(AxisOperation operation, AxisService service,
                                    String paramName, boolean defaultValue) {
        org.apache.axis2.description.Parameter p = operation.getParameter(paramName);
        if (p == null) p = service.getParameter(paramName);
        if (p != null && p.getValue() != null) {
            String v = p.getValue().toString().trim().toLowerCase(java.util.Locale.ROOT);
            if ("true".equals(v))  return true;
            if ("false".equals(v)) return false;
        }
        return defaultValue;
    }

    /**
     * Generates the MCP tool catalog JSON for the {@code /openapi-mcp.json} endpoint.
     *
     * <p>Uses Jackson to build the JSON object graph, which guarantees correct
     * escaping of all string values including control characters — something a
     * hand-rolled {@code StringBuilder} approach cannot safely guarantee.
     *
     * <p>Uses the same service/operation filtering as {@link #generatePaths()}.
     */
    public String generateMcpCatalogJson(HttpServletRequest request) {
        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

            com.fasterxml.jackson.databind.ObjectMapper jackson =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode root = jackson.createObjectNode();

            // Catalog-level metadata so MCP clients understand the transport layer.
            // Axis2 JSON-RPC requires every call to be wrapped as:
            //   {"<operationName>":[{"arg0":{<params>}}]}
            // This is mandated by JsonUtils.invokeServiceClass() in axis2-json.
            // The loginService/doLogin operation is the token endpoint; all other
            // operations require "Authorization: Bearer <token>" in the request header.
            com.fasterxml.jackson.databind.node.ObjectNode meta = root.putObject("_meta");
            meta.put("axis2JsonRpcFormat", "{\"<operationName>\":[{\"arg0\":{<params>}}]}");
            meta.put("contentType", "application/json");
            meta.put("authHeader", "Authorization: Bearer <token>");
            meta.put("tokenEndpoint", "POST /services/loginService/doLogin");

            // Optional: natural key (ticker → assetId) resolution endpoint.
            // Set axis2.xml global parameter "mcpTickerResolveService" to the
            // "ServiceName/operationName" of the ticker lookup operation, e.g.:
            //   <parameter name="mcpTickerResolveService">
            //     TickerLookupService/resolveTicker
            //   </parameter>
            // Omitted from _meta when not configured so deployments without a
            // ticker service don't expose a misleading endpoint reference.
            org.apache.axis2.description.Parameter tickerParam =
                    axisConfig.getParameter("mcpTickerResolveService");
            if (tickerParam != null && tickerParam.getValue() != null) {
                String tickerSvcOp = tickerParam.getValue().toString().trim();
                if (!tickerSvcOp.isEmpty()) {
                    meta.put("tickerResolveEndpoint", "POST /services/" + tickerSvcOp);
                }
            }

            com.fasterxml.jackson.databind.node.ArrayNode toolsArray = root.putArray("tools");

            Iterator<AxisService> services = axisConfig.getServices().values().iterator();
            while (services.hasNext()) {
                AxisService service = services.next();
                if (isSystemService(service)) continue;
                if (!shouldIncludeService(service)) continue;

                // loginService is the unauthenticated token endpoint; all others require auth.
                String svcLower = service.getName().toLowerCase(java.util.Locale.ROOT);
                boolean requiresAuth = !svcLower.contains("login") && !svcLower.equals("adminconsole");

                Iterator<AxisOperation> operations = service.getOperations();
                while (operations.hasNext()) {
                    AxisOperation operation = operations.next();
                    if (!shouldIncludeOperation(service, operation)) continue;

                    String opName = operation.getName().getLocalPart();
                    String path = "/services/" + service.getName() + "/" + opName;

                    com.fasterxml.jackson.databind.node.ObjectNode toolNode = toolsArray.addObject();
                    toolNode.put("name", opName);

                    // Description: prefer operation-level "mcpDescription" parameter,
                    // then service-level "mcpDescription", then auto-generated fallback.
                    // Set in services.xml:
                    //   <operation name="doFoo">
                    //     <parameter name="mcpDescription">Human-readable tool description</parameter>
                    //   </operation>
                    // or at service level for a default across all operations.
                    String description = getMcpStringParam(operation, service, "mcpDescription",
                            service.getName() + ": " + opName);
                    toolNode.put("description", description);

                    // inputSchema: minimal MCP-compliant structure. Richer schemas are
                    // produced when services carry @McpTool annotations (future work).
                    com.fasterxml.jackson.databind.node.ObjectNode schema =
                            toolNode.putObject("inputSchema");
                    schema.put("type", "object");
                    schema.putObject("properties");
                    schema.putArray("required");

                    toolNode.put("endpoint", "POST " + path);

                    // Axis2 JSON-RPC payload template.  MCP clients must wrap the call
                    // body in this envelope — the bare {"field":value} object goes inside
                    // "arg0".  Example for portfolioVariance:
                    //   {"portfolioVariance":[{"arg0":{"nAssets":2,"weights":[0.6,0.4],...}}]}
                    toolNode.put("x-axis2-payloadTemplate",
                            "{\"" + opName + "\":[{\"arg0\":{}}]}");

                    // Whether the caller must supply a Bearer token (from doLogin).
                    toolNode.put("x-requiresAuth", requiresAuth);

                    // MCP 2025-03-26 tool annotations.
                    // Tunable via services.xml parameters at operation or service level:
                    //   mcpReadOnly    → readOnlyHint  (true for GET-equivalent operations)
                    //   mcpDestructive → destructiveHint
                    //   mcpIdempotent  → idempotentHint (true for pure reads / PUT-equivalent)
                    //   mcpOpenWorld   → openWorldHint  (true for operations with side effects
                    //                                    outside the Axis2 service boundary)
                    // Conservative false defaults are preserved when parameters are absent.
                    com.fasterxml.jackson.databind.node.ObjectNode annotations =
                            toolNode.putObject("annotations");
                    annotations.put("readOnlyHint",    getMcpBoolParam(operation, service, "mcpReadOnly",    false));
                    annotations.put("destructiveHint", getMcpBoolParam(operation, service, "mcpDestructive", false));
                    annotations.put("idempotentHint",  getMcpBoolParam(operation, service, "mcpIdempotent",  false));
                    annotations.put("openWorldHint",   getMcpBoolParam(operation, service, "mcpOpenWorld",   false));
                }
            }

            log.debug("Generated MCP catalog JSON");
            return jackson.writeValueAsString(root);

        } catch (Exception e) {
            log.error("Failed to generate MCP catalog JSON", e);
            return "{\"tools\":[]}";
        }
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
        recommendations.append("OpenAPI JSON Processing Performance Analysis (swagger-core + HTTP/2 Metrics):\n");
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