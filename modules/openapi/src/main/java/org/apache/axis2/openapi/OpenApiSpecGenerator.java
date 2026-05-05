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

        // Add components/schemas section (needs request for Spring bean resolution)
        addComponents(openApi, request);

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

        // ── Error responses ───────────────────────────────────────────────────
        // Every operation gets 422/429/503 error responses referencing the
        // shared ErrorResponse schema (defined in addComponents()).  This
        // matches the error taxonomy from Axis2JsonErrorResponse / JsonRpcFaultException:
        //   422 = service threw JsonRpcFaultException.validationError()
        //   429 = service threw JsonRpcFaultException.rateLimited()
        //   503 = service threw JsonRpcFaultException.serviceUnavailable()
        // All share the same JSON shape — only the "error" code field differs.
        Content errorContent = new Content();
        MediaType errorMediaType = new MediaType();
        Schema errorRef = new Schema();
        errorRef.set$ref("#/components/schemas/ErrorResponse");
        errorMediaType.setSchema(errorRef);
        errorContent.addMediaType("application/json", errorMediaType);

        ApiResponse validationError = new ApiResponse();
        validationError.setDescription("Validation error — request body failed input checks");
        validationError.setContent(errorContent);
        responses.addApiResponse("422", validationError);

        ApiResponse rateLimitError = new ApiResponse();
        rateLimitError.setDescription("Rate limited — too many requests");
        rateLimitError.setContent(errorContent);
        responses.addApiResponse("429", rateLimitError);

        ApiResponse serviceUnavailable = new ApiResponse();
        serviceUnavailable.setDescription("Service unavailable — downstream dependency or overload");
        serviceUnavailable.setContent(errorContent);
        responses.addApiResponse("503", serviceUnavailable);

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
     * Add components section to OpenAPI specification, including the shared
     * ErrorResponse schema and auto-generated request/response schemas from
     * Java type introspection.
     */
    private void addComponents(OpenAPI openApi, HttpServletRequest request) {
        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        java.util.Map<String, Schema> schemas = components.getSchemas();
        if (schemas == null) {
            schemas = new java.util.LinkedHashMap<>();
        }

        // ── Shared ErrorResponse schema — matches Axis2JsonErrorResponse wire format ──
        Schema<Object> errorSchema = new Schema<>();
        errorSchema.setType("object");
        errorSchema.setDescription("Structured error response for Axis2 JSON-RPC services");

        Schema<String> errorCode = new Schema<>();
        errorCode.setType("string");
        errorCode.setDescription("Error code: VALIDATION_ERROR, RATE_LIMITED, SERVICE_UNAVAILABLE, BAD_REQUEST, INTERNAL_ERROR");

        Schema<String> message = new Schema<>();
        message.setType("string");
        message.setDescription("Human-readable error message");

        Schema<String> errorRefField = new Schema<>();
        errorRefField.setType("string");
        errorRefField.setDescription("Opaque correlation ID (UUID) for server-side log lookup");

        Schema<String> timestamp = new Schema<>();
        timestamp.setType("string");
        timestamp.setFormat("date-time");
        timestamp.setDescription("ISO 8601 timestamp of when the error occurred");

        Schema<Integer> retryAfter = new Schema<>();
        retryAfter.setType("integer");
        retryAfter.setDescription("Seconds until the client should retry (present on 429/503)");

        java.util.Map<String, Schema> errorProps = new java.util.LinkedHashMap<>();
        errorProps.put("error", errorCode);
        errorProps.put("message", message);
        errorProps.put("errorRef", errorRefField);
        errorProps.put("timestamp", timestamp);
        errorProps.put("retryAfter", retryAfter);
        errorSchema.setProperties(errorProps);

        java.util.List<String> errorRequired = new java.util.ArrayList<>();
        errorRequired.add("error");
        errorRequired.add("message");
        errorRequired.add("errorRef");
        errorRequired.add("timestamp");
        errorSchema.setRequired(errorRequired);
        schemas.put("ErrorResponse", errorSchema);

        // ── Auto-generate request/response schemas from service introspection ──
        // This runs AFTER generatePaths() so all PathItem/Operation objects already
        // exist.  For each operation we:
        //   1. Reflect on the service method's parameter POJO → request schema
        //   2. Reflect on the return type POJO → response schema
        //   3. Update the already-generated operation to $ref these schemas
        //      (replacing the generic {"type":"object"} placeholder)
        //
        // Introspection is best-effort: if the service class can't be loaded
        // (e.g. Spring bean not yet initialized, or missing from classpath),
        // the operation keeps its generic schema — no error, just less detail.
        //
        // Schema naming convention: {operationName}Request / {operationName}Response
        // (e.g. portfolioVarianceRequest, portfolioVarianceResponse).
        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            com.fasterxml.jackson.databind.ObjectMapper jackson = io.swagger.v3.core.util.Json.mapper();

            for (AxisService service : axisConfig.getServices().values()) {
                if (isSystemService(service) || !shouldIncludeService(service)) continue;

                // Resolve service class ONCE per service (expensive reflection),
                // then reuse for all operations within this service.
                Class<?> serviceClass = resolveServiceClass(service, request);

                Iterator<AxisOperation> ops = service.getOperations();
                while (ops.hasNext()) {
                    AxisOperation op = ops.next();
                    if (!shouldIncludeOperation(service, op)) continue;

                    String opName = op.getName().getLocalPart();
                    String path = "/services/" + service.getName() + "/" + opName;

                    // ── Request schema: introspect the method's parameter POJO ──
                    // Uses the serviceClass resolved once per service above.
                    com.fasterxml.jackson.databind.node.ObjectNode requestSchemaNode =
                            generateSchemaFromServiceClass(serviceClass, service.getName(), opName);
                    if (requestSchemaNode != null) {
                        String requestSchemaName = opName + "Request";
                        try {
                            // Convert Jackson ObjectNode → swagger-core Schema via
                            // in-memory tree conversion (no intermediate String alloc).
                            Schema<?> reqSchema = jackson.treeToValue(
                                    requestSchemaNode, Schema.class);
                            reqSchema.setDescription("Request body for " + service.getName()
                                    + "/" + opName);
                            schemas.put(requestSchemaName, reqSchema);

                            // Update the operation's requestBody to reference this schema
                            PathItem pathItem = openApi.getPaths() != null
                                    ? openApi.getPaths().get(path) : null;
                            if (pathItem != null && pathItem.getPost() != null) {
                                RequestBody reqBody = pathItem.getPost().getRequestBody();
                                if (reqBody != null && reqBody.getContent() != null) {
                                    MediaType mt = reqBody.getContent().get("application/json");
                                    if (mt != null) {
                                        Schema refSchema = new Schema();
                                        refSchema.set$ref("#/components/schemas/"
                                                + requestSchemaName);
                                        mt.setSchema(refSchema);
                                    }
                                }
                            }
                            log.debug("Added components/schemas/" + requestSchemaName
                                    + " from Java type introspection");
                        } catch (Exception e) {
                            log.debug("Could not convert request schema for " + opName
                                    + ": " + e.getMessage());
                        }
                    }

                    // ── Response schema: introspect the method's return type POJO ──
                    // Uses the serviceClass resolved once per service above.
                    // Skips primitives, void, and java.* types (only POJOs get schemas).
                    try {
                        if (serviceClass != null) {
                            java.lang.reflect.Method targetMethod = null;
                            int candidateCount = 0;
                            for (java.lang.reflect.Method m : serviceClass.getMethods()) {
                                if (m.getName().equals(opName) && m.getParameterCount() == 1) {
                                    if (targetMethod == null) {
                                        targetMethod = m;
                                    }
                                    candidateCount++;
                                }
                            }
                            if (candidateCount > 1) {
                                log.warn("Ambiguous method '" + opName + "' in "
                                        + serviceClass.getName() + " has " + candidateCount
                                        + " one-argument overloads — generating response "
                                        + "schema from first candidate");
                            }
                            if (targetMethod != null) {
                                Class<?> returnType = targetMethod.getReturnType();
                                if (!returnType.isPrimitive() && returnType != void.class
                                        && returnType != Void.class
                                        && !returnType.getName().startsWith("java.")) {
                                    com.fasterxml.jackson.databind.node.ObjectNode respNode =
                                            buildSchemaFromPojo(returnType);
                                    if (respNode != null) {
                                        String responseSchemaName = opName + "Response";
                                        Schema<?> respSchema = jackson.treeToValue(
                                                respNode, Schema.class);
                                        respSchema.setDescription("Response body for "
                                                + service.getName() + "/" + opName);
                                        schemas.put(responseSchemaName, respSchema);

                                        // Update the operation's 200 response to reference it
                                        PathItem pathItem = openApi.getPaths() != null
                                                ? openApi.getPaths().get(path) : null;
                                        if (pathItem != null && pathItem.getPost() != null) {
                                            ApiResponse ok = pathItem.getPost().getResponses()
                                                    .get("200");
                                            if (ok != null && ok.getContent() != null) {
                                                MediaType mt = ok.getContent()
                                                        .get("application/json");
                                                if (mt != null) {
                                                    Schema refSchema = new Schema();
                                                    refSchema.set$ref(
                                                            "#/components/schemas/"
                                                            + responseSchemaName);
                                                    mt.setSchema(refSchema);
                                                }
                                            }
                                        }
                                        log.debug("Added components/schemas/"
                                                + responseSchemaName
                                                + " from Java type introspection");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Could not generate response schema for "
                                + service.getName() + "/" + opName + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error during auto-schema generation for components", e);
        }

        components.setSchemas(schemas);
        openApi.setComponents(components);
    }

    /**
     * Resolve the service implementation class for type introspection.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>{@code ServiceClass} parameter in services.xml → direct classload</li>
     *   <li>{@code SpringBeanName} parameter → resolve via Spring's
     *       {@code WebApplicationContextUtils} (invoked reflectively to avoid
     *       a compile-time Spring dependency in the openapi module)</li>
     * </ol>
     *
     * <p>Returns null if neither path succeeds.  Callers should treat null
     * as "schema generation not possible" and fall back to generic schemas.
     *
     * <p>This is factored out of {@link #generateSchemaFromServiceClass} so that
     * both request and response schema generation can share the same lookup
     * without loading the class twice per operation.
     */
    private Class<?> resolveServiceClass(AxisService service, HttpServletRequest request) {
        try {
            // Try 1: explicit ServiceClass parameter (e.g. POJO deployment).
            // Use the service's own classloader — it has visibility to
            // WEB-INF/classes and WEB-INF/lib in WAR deployments, whereas
            // Thread.currentThread().getContextClassLoader() may not.
            String className = getServiceClassName(service);
            if (className != null) {
                ClassLoader cl = service.getClassLoader();
                if (cl == null) {
                    cl = Thread.currentThread().getContextClassLoader();
                }
                return cl.loadClass(className);
            }
            // Try 2: Spring bean — uses reflection to call
            // WebApplicationContextUtils.getWebApplicationContext(servletContext)
            // so the openapi module compiles without spring-web on the classpath.
            if (request != null) {
                String beanName = null;
                org.apache.axis2.description.Parameter springBeanParam =
                        service.getParameter("SpringBeanName");
                if (springBeanParam != null && springBeanParam.getValue() != null) {
                    beanName = (String) springBeanParam.getValue();
                }
                if (beanName != null) {
                    jakarta.servlet.ServletContext sc = request.getServletContext();
                    Class<?> wacUtils = Class.forName(
                            "org.springframework.web.context.support.WebApplicationContextUtils");
                    java.lang.reflect.Method getWac = wacUtils.getMethod(
                            "getWebApplicationContext", jakarta.servlet.ServletContext.class);
                    Object ctx = getWac.invoke(null, sc);
                    if (ctx != null) {
                        java.lang.reflect.Method getBean = ctx.getClass().getMethod(
                                "getBean", String.class);
                        Object bean = getBean.invoke(ctx, beanName);
                        if (bean != null) return bean.getClass();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve service class for " + service.getName()
                    + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Build a JSON Schema ObjectNode from a POJO class using getter introspection.
     * Reuses the same introspection pattern as {@link #generateSchemaFromServiceClass}
     * but works on an arbitrary class (used for response types).
     */
    private com.fasterxml.jackson.databind.node.ObjectNode buildSchemaFromPojo(Class<?> pojoClass) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    io.swagger.v3.core.util.Json.mapper();
            com.fasterxml.jackson.databind.node.ObjectNode schema = mapper.createObjectNode();
            schema.put("type", "object");
            com.fasterxml.jackson.databind.node.ObjectNode properties =
                    schema.putObject("properties");

            for (java.lang.reflect.Method getter : pojoClass.getMethods()) {
                String name = getter.getName();
                if (name.equals("getClass") || getter.getParameterCount() != 0) continue;

                String fieldName = null;
                if (name.startsWith("get") && name.length() > 3) {
                    fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                } else if (name.startsWith("is") && name.length() > 2
                        && (getter.getReturnType() == boolean.class
                            || getter.getReturnType() == Boolean.class)) {
                    fieldName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                }
                if (fieldName == null) continue;

                com.fasterxml.jackson.databind.node.ObjectNode prop =
                        properties.putObject(fieldName);
                mapJavaTypeToJsonSchema(getter.getReturnType(),
                        getter.getGenericReturnType(), prop);
            }
            return schema;
        } catch (Exception e) {
            log.debug("Could not build schema from POJO " + pojoClass.getName()
                    + ": " + e.getMessage());
            return null;
        }
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
     * Auto-generate a JSON Schema from the Java service method's parameter type.
     *
     * <p>Looks up the service class, finds the method matching the operation name,
     * and introspects the parameter POJO's fields to produce a schema. This is the
     * "Option 2" fallback when no explicit {@code mcpInputSchema} is set in
     * services.xml.
     *
     * <p>Supports: primitives (int/long/double/boolean/String), arrays, and
     * nested POJOs (one level). Returns null if introspection fails for any reason.
     *
     * @param service the Axis2 service descriptor
     * @param operationName the operation (method) name
     * @return an ObjectNode containing the JSON Schema, or null
     */
    private com.fasterxml.jackson.databind.node.ObjectNode generateSchemaFromServiceClass(
            AxisService service, String operationName) {
        return generateSchemaFromServiceClass(service, operationName, null);
    }

    private com.fasterxml.jackson.databind.node.ObjectNode generateSchemaFromServiceClass(
            AxisService service, String operationName, HttpServletRequest request) {
        Class<?> serviceClass = resolveServiceClass(service, request);
        return generateSchemaFromServiceClass(serviceClass, service.getName(), operationName);
    }

    /**
     * Generate a JSON Schema from a pre-resolved service class and operation name.
     * This overload avoids redundant class resolution when the caller has already
     * resolved the class (e.g. once per service in addComponents).
     *
     * @param serviceClass the resolved service implementation class (may be null)
     * @param serviceName  the service name (for logging)
     * @param operationName the operation (method) name
     * @return an ObjectNode containing the JSON Schema, or null
     */
    private com.fasterxml.jackson.databind.node.ObjectNode generateSchemaFromServiceClass(
            Class<?> serviceClass, String serviceName, String operationName) {
        try {
            if (serviceClass == null) return null;
            java.lang.reflect.Method targetMethod = null;
            for (java.lang.reflect.Method m : serviceClass.getMethods()) {
                if (m.getName().equals(operationName) && m.getParameterCount() == 1) {
                    if (targetMethod != null) {
                        log.warn("[MCP] Ambiguous method '" + operationName
                                + "' in " + serviceClass.getName()
                                + " — multiple one-arg overloads, using first match");
                    }
                    if (targetMethod == null) {
                        targetMethod = m;
                    }
                }
            }
            if (targetMethod == null) return null;

            Class<?> paramType = targetMethod.getParameterTypes()[0];
            // Skip primitives and common JDK types — only introspect POJOs
            if (paramType.isPrimitive() || paramType == String.class
                    || paramType.getName().startsWith("java.")) {
                return null;
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = io.swagger.v3.core.util.Json.mapper();
            com.fasterxml.jackson.databind.node.ObjectNode schema = mapper.createObjectNode();
            schema.put("type", "object");
            com.fasterxml.jackson.databind.node.ObjectNode properties = schema.putObject("properties");
            com.fasterxml.jackson.databind.node.ArrayNode required = schema.putArray("required");

            for (java.lang.reflect.Method getter : paramType.getMethods()) {
                String name = getter.getName();
                if (!name.startsWith("get") || name.equals("getClass") || getter.getParameterCount() != 0) {
                    if (name.startsWith("is") && getter.getParameterCount() == 0
                            && (getter.getReturnType() == boolean.class || getter.getReturnType() == Boolean.class)) {
                        // boolean getter: isNormalizeWeights -> normalizeWeights
                        String fieldName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                        com.fasterxml.jackson.databind.node.ObjectNode prop = properties.putObject(fieldName);
                        prop.put("type", "boolean");
                    }
                    continue;
                }
                // getWeights -> weights
                String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                Class<?> returnType = getter.getReturnType();

                com.fasterxml.jackson.databind.node.ObjectNode prop = properties.putObject(fieldName);
                mapJavaTypeToJsonSchema(returnType, getter.getGenericReturnType(), prop);
            }

            return schema;
        } catch (SecurityException e) {
            log.debug("[MCP] Could not auto-generate schema for " + serviceName
                    + "/" + operationName + ": " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("[MCP] Unexpected error during auto-schema generation for "
                    + serviceName + "/" + operationName, e);
            return null;
        }
    }

    /**
     * Maps a Java type to a JSON Schema type/format in the given ObjectNode.
     */
    private void mapJavaTypeToJsonSchema(Class<?> type, java.lang.reflect.Type genericType,
            com.fasterxml.jackson.databind.node.ObjectNode prop) {
        if (type == int.class || type == Integer.class) {
            prop.put("type", "integer");
        } else if (type == long.class || type == Long.class) {
            prop.put("type", "integer");
        } else if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
            prop.put("type", "number");
        } else if (type == boolean.class || type == Boolean.class) {
            prop.put("type", "boolean");
        } else if (type == String.class) {
            prop.put("type", "string");
        } else if (type.isArray()) {
            prop.put("type", "array");
            com.fasterxml.jackson.databind.node.ObjectNode items = prop.putObject("items");
            Class<?> componentType = type.getComponentType();
            if (componentType.isArray()) {
                // double[][] -> array of arrays of numbers
                items.put("type", "array");
                com.fasterxml.jackson.databind.node.ObjectNode innerItems = items.putObject("items");
                mapJavaTypeToJsonSchema(componentType.getComponentType(), null, innerItems);
            } else {
                mapJavaTypeToJsonSchema(componentType, null, items);
            }
        } else if (java.util.List.class.isAssignableFrom(type) && genericType instanceof java.lang.reflect.ParameterizedType) {
            prop.put("type", "array");
            java.lang.reflect.Type[] typeArgs = ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                com.fasterxml.jackson.databind.node.ObjectNode items = prop.putObject("items");
                mapJavaTypeToJsonSchema((Class<?>) typeArgs[0], null, items);
            }
        } else {
            prop.put("type", "object");
        }
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
        org.apache.axis2.description.Parameter p =
                (operation != null) ? operation.getParameter(paramName) : null;
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
        org.apache.axis2.description.Parameter p =
                (operation != null) ? operation.getParameter(paramName) : null;
        if (p == null) p = service.getParameter(paramName);
        if (p != null && p.getValue() != null) {
            String v = p.getValue().toString().trim().toLowerCase(java.util.Locale.ROOT);
            if ("true".equals(v))  return true;
            if ("false".equals(v)) return false;
            if (!v.isEmpty()) {
                log.warn("[MCP] Unrecognised boolean value '" + p.getValue().toString().trim()
                        + "' for parameter '" + paramName + "' — using default " + defaultValue);
            }
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

            // Re-use the swagger-core Jackson instance: already configured with
            // NON_NULL, WRITE_DATES_AS_TIMESTAMPS=false, FAIL_ON_EMPTY_BEANS=false
            // and available on the classpath — avoids allocating a new ObjectMapper
            // per request (ObjectMapper construction is expensive due to module scanning).
            com.fasterxml.jackson.databind.ObjectMapper jackson = io.swagger.v3.core.util.Json.mapper();
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

            // ── Error contract in MCP _meta ──────────────────────────────────
            // Describes the structured error envelope (Axis2JsonErrorResponse)
            // that all operations may return on 4xx/5xx responses.  Mirrors the
            // OpenAPI components/schemas/ErrorResponse definition.
            //
            // MCP clients can use this to:
            //   1. Auto-parse error responses without per-operation handling
            //   2. Display the errorRef to users for support correlation
            //   3. Implement retry logic based on retryAfter (429/503)
            com.fasterxml.jackson.databind.node.ObjectNode errorContract = meta.putObject("errorContract");
            errorContract.put("schemaRef", "#/components/schemas/ErrorResponse");
            com.fasterxml.jackson.databind.node.ObjectNode errorFields = errorContract.putObject("fields");
            errorFields.put("error", "Error code: VALIDATION_ERROR | RATE_LIMITED | SERVICE_UNAVAILABLE | BAD_REQUEST | INTERNAL_ERROR");
            errorFields.put("message", "Human-readable error message");
            errorFields.put("errorRef", "UUID correlation ID — quote in support requests");
            errorFields.put("timestamp", "ISO 8601 when the error occurred");
            errorFields.put("retryAfter", "Seconds to wait before retrying (429/503 only, null otherwise)");
            com.fasterxml.jackson.databind.node.ObjectNode statusMap = errorContract.putObject("httpStatusMapping");
            statusMap.put("400", "BAD_REQUEST — malformed JSON or missing required fields");
            statusMap.put("422", "VALIDATION_ERROR — valid JSON but fails business validation");
            statusMap.put("429", "RATE_LIMITED — too many requests, check retryAfter");
            statusMap.put("500", "INTERNAL_ERROR — server fault, errorRef logged server-side");
            statusMap.put("503", "SERVICE_UNAVAILABLE — downstream dependency or overload");

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
                // Validate format: must be "ServiceName/operationName" — each segment
                // is an XML NCName (word chars, dots, hyphens).  Reject anything else
                // to prevent a misconfigured path-traversal value reaching MCP clients.
                if (!tickerSvcOp.isEmpty()) {
                    if (tickerSvcOp.matches("[\\w.\\-]+/[\\w.\\-]+")) {
                        meta.put("tickerResolveEndpoint", "POST /services/" + tickerSvcOp);
                    } else {
                        log.warn("[MCP] Ignoring invalid mcpTickerResolveService value '"
                                + tickerSvcOp + "' — expected ServiceName/operationName");
                    }
                }
            }

            com.fasterxml.jackson.databind.node.ArrayNode toolsArray = root.putArray("tools");

            Iterator<AxisService> services = axisConfig.getServices().values().iterator();
            while (services.hasNext()) {
                AxisService service = services.next();
                if (isSystemService(service)) continue;
                if (!shouldIncludeService(service)) continue;

                // Prefer explicit mcpRequiresAuth parameter; fall back to name heuristic
                // only when the parameter is absent.  The heuristic uses exact match on
                // "loginservice" (case-insensitive) and "adminconsole" to avoid false
                // positives for services like "LoginHistoryService" or "CatalogLoginRecords".
                String svcLower = service.getName().toLowerCase(java.util.Locale.ROOT);
                boolean requiresAuth;
                String mcpRequiresAuthParam = getMcpStringParam(
                        null, service, "mcpRequiresAuth", null);
                if (mcpRequiresAuthParam != null) {
                    requiresAuth = !"false".equalsIgnoreCase(mcpRequiresAuthParam);
                } else {
                    requiresAuth = !svcLower.equals("loginservice")
                            && !svcLower.equals("adminconsole");
                }

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

                    // inputSchema: prefer mcpInputSchema parameter (literal JSON Schema
                    // string set in services.xml at operation or service level).
                    // Falls back to an empty schema when absent or malformed.
                    //
                    // Option 1 usage (services.xml):
                    //   <operation name="portfolioVariance">
                    //     <parameter name="mcpInputSchema">{
                    //       "type": "object",
                    //       "required": ["n_assets", "weights"],
                    //       "properties": {
                    //         "n_assets": {"type": "integer"},
                    //         "weights":  {"type": "array", "items": {"type": "number"}}
                    //       }
                    //     }</parameter>
                    //   </operation>
                    //
                    // Option 3: schemas can also be written by the build-time code-gen
                    // script (tools/gen_mcp_schema.py) which reads C header structs and
                    // emits mcpInputSchema parameters into services.xml automatically.
                    String mcpInputSchemaStr = getMcpStringParam(operation, service,
                            "mcpInputSchema", null);
                    if (mcpInputSchemaStr != null) {
                        try {
                            com.fasterxml.jackson.databind.JsonNode parsedSchema =
                                    jackson.readTree(mcpInputSchemaStr);
                            toolNode.set("inputSchema", parsedSchema);
                        } catch (Exception parseEx) {
                            log.warn("[MCP] Invalid mcpInputSchema JSON for operation '"
                                    + opName + "' in service '" + service.getName()
                                    + "' — falling back to empty schema: "
                                    + parseEx.getMessage());
                            com.fasterxml.jackson.databind.node.ObjectNode schema =
                                    toolNode.putObject("inputSchema");
                            schema.put("type", "object");
                            schema.putObject("properties");
                            schema.putArray("required");
                        }
                    } else {
                        // Option 2: auto-generate schema from Java method parameter type.
                        // Introspects the service class to find the method matching
                        // this operation name, then reflects on the request POJO's
                        // fields to build a JSON Schema. Falls back to empty schema
                        // if introspection fails (e.g., no ServiceClass parameter,
                        // method not found, or primitive parameters).
                        com.fasterxml.jackson.databind.node.ObjectNode schema =
                                generateSchemaFromServiceClass(service, opName, request);
                        if (schema != null) {
                            toolNode.set("inputSchema", schema);
                            log.debug("[MCP] Auto-generated inputSchema for "
                                    + service.getName() + "/" + opName
                                    + " from Java type introspection");
                        } else {
                            schema = toolNode.putObject("inputSchema");
                            schema.put("type", "object");
                            schema.putObject("properties");
                            schema.putArray("required");
                        }
                    }

                    toolNode.put("endpoint", "POST " + path);

                    // Axis2 JSON-RPC payload template.  MCP clients must wrap the call
                    // body in this envelope — the bare {"field":value} object goes inside
                    // "arg0".  Example for portfolioVariance:
                    //   {"portfolioVariance":[{"arg0":{"nAssets":2,"weights":[0.6,0.4],...}}]}
                    //
                    // Built via Jackson tree API (not string concatenation) so that opName
                    // values containing JSON-special chars (", \, control chars) are
                    // correctly escaped.  jackson.writeValueAsString() cannot throw here
                    // because the tree is well-formed by construction.
                    try {
                        com.fasterxml.jackson.databind.node.ObjectNode tmpl =
                                jackson.createObjectNode();
                        tmpl.putArray(opName).addObject().putObject("arg0");
                        toolNode.put("x-axis2-payloadTemplate",
                                jackson.writeValueAsString(tmpl));
                    } catch (com.fasterxml.jackson.core.JsonProcessingException jpe) {
                        // Cannot happen for a well-formed Jackson node tree; fall back
                        // to a safe static placeholder so the tool node is still usable.
                        log.warn("[MCP] Failed to serialize payloadTemplate for '"
                                + opName + "': " + jpe.getMessage());
                        toolNode.put("x-axis2-payloadTemplate", "{}");
                    }

                    // Whether the caller must supply a Bearer token (from doLogin).
                    toolNode.put("x-requiresAuth", requiresAuth);

                    // B2 — mcpAuthScope: optional OAuth2 / custom scope string.
                    // When present, MCP clients that support fine-grained auth can
                    // request just this scope rather than a full-access token.
                    // Declared at operation OR service level (operation wins).
                    // Example services.xml:  <parameter name="mcpAuthScope">read:portfolio</parameter>
                    String authScope = getMcpStringParam(operation, service, "mcpAuthScope", null);
                    if (authScope != null) {
                        toolNode.put("x-authScope", authScope);
                    }

                    // B3 — mcpStreaming: signals that this operation returns a stream
                    // (chunked JSON, SSE, or long-poll) rather than a single response.
                    // MCP clients that support progressive rendering can use this hint
                    // to display partial results as they arrive.
                    // Example services.xml:  <parameter name="mcpStreaming">true</parameter>
                    if (getMcpBoolParam(operation, service, "mcpStreaming", false)) {
                        toolNode.put("x-streaming", true);
                    }

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
            // Return a distinct error shape so callers can distinguish generation failure
            // from a legitimate empty catalog (no services deployed).
            return "{\"tools\":[],\"_error\":\"catalog generation failed — see server log\"}";
        }
    }

    /**
     * Generate an MCP Resources listing (C3).
     *
     * <p>MCP Resources are read-only, browseable data items — conceptually the
     * complement of Tools (which take actions).  Here each deployed Axis2 service
     * becomes a resource so that an AI client can discover what services exist and
     * fetch their WSDL or metadata without executing an operation.
     *
     * <p>Output shape (MCP 2025-03-26 {@code resources/list} response):
     * <pre>
     * {
     *   "resources": [
     *     {
     *       "uri":         "axis2://services/PortfolioService",
     *       "name":        "PortfolioService",
     *       "description": "...",          // mcpDescription service param or auto-generated
     *       "mimeType":    "application/json",
     *       "metadata": {
     *         "wsdlUrl":      "POST /services/PortfolioService?wsdl",
     *         "operations":   ["getPortfolio", "updateWeights", ...],
     *         "requiresAuth": true
     *       }
     *     }
     *   ]
     * }
     * </pre>
     *
     * <p>System services ("Version", "AdminService", names starting with "__") are
     * excluded, matching the tool catalog filter.
     *
     * @param request the incoming HTTP request (used only to determine the base URL)
     * @return JSON string; never null.  On error returns
     *         {@code {"resources":[],"_error":"..."}} so callers can distinguish
     *         failure from an empty deployment.
     */
    public String generateMcpResourcesJson(HttpServletRequest request) {
        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            com.fasterxml.jackson.databind.ObjectMapper jackson = io.swagger.v3.core.util.Json.mapper();
            com.fasterxml.jackson.databind.node.ObjectNode root = jackson.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode resources = root.putArray("resources");

            java.util.Map<String, AxisService> services = axisConfig.getServices();
            for (AxisService service : services.values()) {
                String svcName = service.getName();
                if (isSystemService(service)) continue;

                // URI: logical identifier for the resource in the MCP protocol.
                // Uses the "axis2://" scheme so clients can distinguish these
                // resources from generic HTTP URLs.
                String uri = "axis2://services/" + svcName;

                // Human-readable description: service-level mcpDescription param
                // or auto-generated fallback.
                String description = getMcpStringParam(null, service, "mcpDescription",
                        "Axis2 service: " + svcName);

                com.fasterxml.jackson.databind.node.ObjectNode resource =
                        resources.addObject();
                resource.put("uri",         uri);
                resource.put("name",         svcName);
                resource.put("description",  description);
                resource.put("mimeType",     "application/json");

                // metadata sub-object: service-specific details for MCP clients
                // that want to introspect available operations before calling.
                com.fasterxml.jackson.databind.node.ObjectNode metadata =
                        resource.putObject("metadata");
                metadata.put("wsdlUrl", "GET /services/" + svcName + "?wsdl");

                // List all non-system operation names.
                com.fasterxml.jackson.databind.node.ArrayNode ops = metadata.putArray("operations");
                java.util.Iterator<AxisOperation> opIter = service.getOperations();
                while (opIter.hasNext()) {
                    AxisOperation op = opIter.next();
                    if (op != null && op.getName() != null) {
                        String opName = op.getName().getLocalPart();
                        if (opName != null && !opName.startsWith("__")) {
                            ops.add(opName);
                        }
                    }
                }

                // Auth requirement mirrors the tool catalog heuristic.
                String svcLower = svcName.toLowerCase(java.util.Locale.ROOT);
                boolean requiresAuth;
                String mcpRequiresAuthParam = getMcpStringParam(null, service,
                        "mcpRequiresAuth", null);
                if (mcpRequiresAuthParam != null) {
                    requiresAuth = !"false".equalsIgnoreCase(mcpRequiresAuthParam);
                } else {
                    requiresAuth = !svcLower.equals("loginservice")
                                && !svcLower.equals("adminconsole");
                }
                metadata.put("requiresAuth", requiresAuth);
            }

            log.debug("Generated MCP resources JSON (" + resources.size() + " services)");
            return jackson.writeValueAsString(root);

        } catch (Exception e) {
            log.error("Failed to generate MCP resources JSON", e);
            return "{\"resources\":[],\"_error\":\"resources generation failed — see server log\"}";
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