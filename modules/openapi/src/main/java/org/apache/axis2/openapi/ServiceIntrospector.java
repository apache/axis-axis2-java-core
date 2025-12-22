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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Introspects Axis2 services to extract metadata for OpenAPI documentation generation.
 *
 * This class analyzes deployed Axis2 services, their operations, parameters,
 * and REST configurations to provide structured metadata for OpenAPI spec generation.
 * Enhanced in v2.0.1 with configuration-aware introspection.
 */
public class ServiceIntrospector {

    private static final Log log = LogFactory.getLog(ServiceIntrospector.class);

    private final ConfigurationContext configurationContext;
    private final OpenApiConfiguration configuration;

    /**
     * Constructor with default configuration.
     */
    public ServiceIntrospector(ConfigurationContext configContext) {
        this(configContext, new OpenApiConfiguration());
    }

    /**
     * Constructor with custom configuration.
     */
    public ServiceIntrospector(ConfigurationContext configContext, OpenApiConfiguration config) {
        this.configurationContext = configContext;
        this.configuration = config != null ? config : new OpenApiConfiguration();

        log.debug("ServiceIntrospector initialized with configuration: " + this.configuration);
    }

    /**
     * Get metadata for all REST-enabled services.
     */
    public List<ServiceMetadata> getRestServices() {
        List<ServiceMetadata> services = new ArrayList<>();

        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            Iterator<AxisService> serviceIterator = axisConfig.getServices().values().iterator();

            while (serviceIterator.hasNext()) {
                AxisService service = serviceIterator.next();

                if (isRestEnabled(service) && !isSystemService(service)) {
                    ServiceMetadata metadata = analyzeService(service);
                    services.add(metadata);
                }
            }

        } catch (Exception e) {
            log.error("Failed to introspect REST services", e);
        }

        return services;
    }

    /**
     * Analyze a single service to extract metadata.
     */
    public ServiceMetadata analyzeService(AxisService service) {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setServiceName(service.getName());
        metadata.setDocumentation(service.getDocumentation());
        metadata.setTargetNamespace(service.getTargetNamespace());

        // Extract service-level parameters
        Map<String, String> parameters = new HashMap<>();
        for (Parameter param : service.getParameters()) {
            parameters.put(param.getName(), param.getValue().toString());
        }
        metadata.setParameters(parameters);

        // Analyze operations
        List<OperationMetadata> operations = new ArrayList<>();
        Iterator<AxisOperation> operationIterator = service.getOperations();
        while (operationIterator.hasNext()) {
            AxisOperation operation = operationIterator.next();
            OperationMetadata opMetadata = analyzeOperation(operation);
            operations.add(opMetadata);
        }
        metadata.setOperations(operations);

        return metadata;
    }

    /**
     * Analyze a single operation to extract metadata.
     */
    private OperationMetadata analyzeOperation(AxisOperation operation) {
        OperationMetadata metadata = new OperationMetadata();
        metadata.setOperationName(operation.getName().getLocalPart());
        metadata.setDocumentation(operation.getDocumentation());

        // Extract HTTP method from REST configuration
        String httpMethod = getHttpMethod(operation);
        metadata.setHttpMethod(httpMethod);

        // Extract REST path from configuration
        String restPath = getRestPath(operation);
        metadata.setRestPath(restPath);

        // Analyze input/output messages
        if (operation.getMessage("In") != null) {
            metadata.setInputMessage(operation.getMessage("In").getName());
        }
        if (operation.getMessage("Out") != null) {
            metadata.setOutputMessage(operation.getMessage("Out").getName());
        }

        // Extract operation parameters
        Map<String, String> parameters = new HashMap<>();
        for (Parameter param : operation.getParameters()) {
            parameters.put(param.getName(), param.getValue().toString());
        }
        metadata.setParameters(parameters);

        return metadata;
    }

    /**
     * Check if service has REST support enabled.
     */
    private boolean isRestEnabled(AxisService service) {
        Parameter restParam = service.getParameter("enableREST");
        if (restParam != null) {
            return Boolean.parseBoolean(restParam.getValue().toString());
        }

        // Check for REST binding configuration
        return service.getEndpoint("RestEndpoint") != null ||
               hasRestOperations(service);
    }

    /**
     * Check if service has operations with REST configurations.
     */
    private boolean hasRestOperations(AxisService service) {
        Iterator<AxisOperation> operations = service.getOperations();
        while (operations.hasNext()) {
            AxisOperation operation = operations.next();
            if (getHttpMethod(operation) != null || getRestPath(operation) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract HTTP method from operation configuration.
     */
    private String getHttpMethod(AxisOperation operation) {
        Parameter httpMethod = operation.getParameter("HTTPMethod");
        if (httpMethod != null) {
            return httpMethod.getValue().toString().toUpperCase();
        }

        // Default to POST for operations without explicit HTTP method
        return "POST";
    }

    /**
     * Extract REST path from operation configuration.
     */
    private String getRestPath(AxisOperation operation) {
        Parameter restPath = operation.getParameter("RESTPath");
        if (restPath != null) {
            return restPath.getValue().toString();
        }

        // Default path based on operation name
        return "/" + operation.getName().getLocalPart();
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
     * Service metadata container.
     */
    public static class ServiceMetadata {
        private String serviceName;
        private String documentation;
        private String targetNamespace;
        private Map<String, String> parameters = new HashMap<>();
        private List<OperationMetadata> operations = new ArrayList<>();

        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public String getDocumentation() { return documentation; }
        public void setDocumentation(String documentation) { this.documentation = documentation; }

        public String getTargetNamespace() { return targetNamespace; }
        public void setTargetNamespace(String targetNamespace) { this.targetNamespace = targetNamespace; }

        public Map<String, String> getParameters() { return parameters; }
        public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }

        public List<OperationMetadata> getOperations() { return operations; }
        public void setOperations(List<OperationMetadata> operations) { this.operations = operations; }
    }

    /**
     * Operation metadata container.
     */
    public static class OperationMetadata {
        private String operationName;
        private String documentation;
        private String httpMethod = "POST";
        private String restPath;
        private String inputMessage;
        private String outputMessage;
        private Map<String, String> parameters = new HashMap<>();

        // Getters and setters
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }

        public String getDocumentation() { return documentation; }
        public void setDocumentation(String documentation) { this.documentation = documentation; }

        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

        public String getRestPath() { return restPath; }
        public void setRestPath(String restPath) { this.restPath = restPath; }

        public String getInputMessage() { return inputMessage; }
        public void setInputMessage(String inputMessage) { this.inputMessage = inputMessage; }

        public String getOutputMessage() { return outputMessage; }
        public void setOutputMessage(String outputMessage) { this.outputMessage = outputMessage; }

        public Map<String, String> getParameters() { return parameters; }
        public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
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
}