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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
 */
public class OpenApiSpecGenerator {

    private static final Log log = LogFactory.getLog(OpenApiSpecGenerator.class);

    private final ConfigurationContext configurationContext;
    private final ServiceIntrospector serviceIntrospector;
    private final ObjectMapper objectMapper;

    public OpenApiSpecGenerator(ConfigurationContext configContext) {
        this.configurationContext = configContext;
        this.serviceIntrospector = new ServiceIntrospector(configContext);

        // Configure Jackson for OpenAPI model serialization
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Generate complete OpenAPI specification for all deployed services.
     *
     * @param request HTTP request for server URL context
     * @return OpenAPI specification object
     */
    public OpenAPI generateOpenApiSpec(HttpServletRequest request) {
        log.debug("Generating OpenAPI specification for Axis2 services");

        OpenAPI openApi = new OpenAPI();
        openApi.setOpenapi("3.0.1");

        // Set API information
        openApi.setInfo(createApiInfo());

        // Set servers based on request context
        openApi.setServers(createServerList(request));

        // Generate paths from services
        openApi.setPaths(generatePaths());

        // TODO: Add components/schemas section for request/response models

        return openApi;
    }

    /**
     * Generate OpenAPI specification as JSON string.
     */
    public String generateOpenApiJson(HttpServletRequest request) {
        try {
            OpenAPI spec = generateOpenApiSpec(request);
            return objectMapper.writeValueAsString(spec);
        } catch (Exception e) {
            log.error("Failed to generate OpenAPI JSON", e);
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
     * Create API information section.
     */
    private Info createApiInfo() {
        Info info = new Info();
        info.setTitle("Apache Axis2 REST API");
        info.setDescription("Auto-generated OpenAPI documentation for Apache Axis2 REST services");
        info.setVersion("1.0.0");

        // Add contact information
        Contact contact = new Contact();
        contact.setName("Apache Axis2");
        contact.setUrl("https://axis.apache.org/axis2/java/core/");
        info.setContact(contact);

        // Add license information
        License license = new License();
        license.setName("Apache License 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0");
        info.setLicense(license);

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
     * Generate paths from all deployed services.
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
                if (path != null) {
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
}