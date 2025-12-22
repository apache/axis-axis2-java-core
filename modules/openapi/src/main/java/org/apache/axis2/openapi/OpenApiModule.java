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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Apache Axis2 OpenAPI/Swagger integration module.
 *
 * This module provides automatic OpenAPI specification generation and Swagger UI support
 * for Axis2 REST services. It integrates with the Axis2 transport layer to serve
 * OpenAPI documentation at standard endpoints.
 *
 * Key features (Enhanced in v2.0.1):
 * - Automatic OpenAPI 3.0.1 specification generation from service metadata
 * - Comprehensive configuration system with properties file support
 * - Security schemes integration (OAuth2, API Key, Basic Auth, etc.)
 * - Advanced customization via OpenApiCustomizer interface
 * - Swagger UI integration for interactive API documentation
 * - Resource filtering and route management
 * - Support for REST service introspection and annotation processing
 * - Integration with Axis2's existing metadata query mechanisms
 */
public class OpenApiModule implements Module {

    private static final Log log = LogFactory.getLog(OpenApiModule.class);

    // Configuration property keys
    private static final String CONFIG_PROPERTY = "axis2.openapi.configuration";
    private static final String GENERATOR_PROPERTY = "axis2.openapi.generator";
    private static final String UI_HANDLER_PROPERTY = "axis2.openapi.ui";
    private static final String INTROSPECTOR_PROPERTY = "axis2.openapi.introspector";

    // Module parameters
    private static final String CONFIG_FILE_PARAM = "configFile";
    private static final String PROPERTIES_FILE_PARAM = "propertiesFile";

    // Global configuration instance
    private static OpenApiConfiguration globalConfiguration;

    /**
     * Initialize the OpenAPI module with comprehensive configuration support.
     *
     * This method is called when the module is loaded and initializes the OpenAPI
     * integration components including specification generation and UI serving.
     * Enhanced in v2.0.1 with full configuration system integration.
     */
    @Override
    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
        log.info("Initializing Apache Axis2 OpenAPI module v2.0.1");

        try {
            // Load configuration from various sources
            OpenApiConfiguration configuration = loadConfiguration(configContext, module);

            // Validate configuration
            validateConfiguration(configuration);

            // Store configuration in context
            configContext.setProperty(CONFIG_PROPERTY, configuration);

            // Initialize OpenAPI specification generator with configuration
            OpenApiSpecGenerator specGenerator = new OpenApiSpecGenerator(configContext, configuration);
            configContext.setProperty(GENERATOR_PROPERTY, specGenerator);

            // Initialize Swagger UI handler with configuration
            SwaggerUIHandler uiHandler = new SwaggerUIHandler(configContext, configuration);
            configContext.setProperty(UI_HANDLER_PROPERTY, uiHandler);

            // Initialize OpenAPI service introspector with configuration
            ServiceIntrospector introspector = new ServiceIntrospector(configContext, configuration);
            configContext.setProperty(INTROSPECTOR_PROPERTY, introspector);

            // Set global configuration for static access
            globalConfiguration = configuration;

            log.info("OpenAPI module initialization completed successfully with configuration: " + configuration);

        } catch (Exception e) {
            log.error("Failed to initialize OpenAPI module", e);
            throw new AxisFault("OpenAPI module initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Called when this module is engaged to a service or operation.
     *
     * This allows the module to customize behavior per service and validate
     * that the service is compatible with OpenAPI documentation generation.
     * Enhanced in v2.0.1 with configuration-based service filtering.
     */
    @Override
    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        String serviceName = axisDescription.getClass().getSimpleName();
        if (axisDescription instanceof org.apache.axis2.description.AxisService) {
            serviceName = ((org.apache.axis2.description.AxisService) axisDescription).getName();
        }

        log.debug("OpenAPI module engaged to: " + serviceName);

        // Get current configuration
        OpenApiConfiguration configuration = getGlobalConfiguration();

        // Validate that the service supports REST operations for OpenAPI generation
        if (axisDescription.getParameter("enableREST") == null) {
            if (configuration != null && configuration.isReadAllResources()) {
                log.info("Service " + serviceName +
                        " does not have REST enabled but will be included due to readAllResources configuration");
            } else {
                log.warn("Service " + serviceName +
                        " does not have REST enabled - OpenAPI documentation may be limited");
            }
        }

        // Apply service-specific configuration if needed
        applyServiceConfiguration(axisDescription, configuration);
    }

    /**
     * Shutdown the OpenAPI module and clean up resources.
     * Enhanced in v2.0.1 with comprehensive cleanup.
     */
    @Override
    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
        log.info("Shutting down Apache Axis2 OpenAPI module");

        try {
            // Clean up registered components
            configurationContext.removeProperty(CONFIG_PROPERTY);
            configurationContext.removeProperty(GENERATOR_PROPERTY);
            configurationContext.removeProperty(UI_HANDLER_PROPERTY);
            configurationContext.removeProperty(INTROSPECTOR_PROPERTY);

            // Clear global configuration
            globalConfiguration = null;

            log.info("OpenAPI module shutdown completed successfully");

        } catch (Exception e) {
            log.warn("Error during OpenAPI module shutdown", e);
        }
    }

    /**
     * Policy assertion support - currently not implemented for OpenAPI.
     */
    @Override
    public boolean canSupportAssertion(Assertion assertion) {
        return false;
    }

    /**
     * Policy processing - currently not implemented for OpenAPI.
     */
    @Override
    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {
        // OpenAPI module does not currently support WS-Policy integration
    }

    // ========== Configuration Management Methods ==========

    /**
     * Load OpenAPI configuration from various sources.
     */
    private OpenApiConfiguration loadConfiguration(ConfigurationContext configContext, AxisModule module) {
        log.debug("Loading OpenAPI configuration");

        OpenApiConfiguration configuration = new OpenApiConfiguration();

        try {
            // 1. Load from module parameters
            loadConfigurationFromModuleParameters(configuration, module);

            // 2. Load from properties file if specified
            loadConfigurationFromPropertiesFile(configuration, module);

            // 3. Load from system properties and environment variables
            configuration.loadConfiguration();

            // 4. Apply any context-based configuration
            if (configuration.isUseContextBasedConfig()) {
                loadContextBasedConfiguration(configuration, configContext);
            }

        } catch (Exception e) {
            log.warn("Error loading OpenAPI configuration, using defaults", e);
        }

        return configuration;
    }

    /**
     * Load configuration from module parameters in module.xml.
     */
    private void loadConfigurationFromModuleParameters(OpenApiConfiguration configuration, AxisModule module) {
        if (module == null) return;

        // Load basic configuration from module parameters
        Parameter titleParam = module.getParameter("title");
        if (titleParam != null && titleParam.getValue() != null) {
            configuration.setTitle(titleParam.getValue().toString());
        }

        Parameter versionParam = module.getParameter("version");
        if (versionParam != null && versionParam.getValue() != null) {
            configuration.setVersion(versionParam.getValue().toString());
        }

        Parameter descriptionParam = module.getParameter("description");
        if (descriptionParam != null && descriptionParam.getValue() != null) {
            configuration.setDescription(descriptionParam.getValue().toString());
        }

        // Load boolean flags
        Parameter prettyPrintParam = module.getParameter("prettyPrint");
        if (prettyPrintParam != null && prettyPrintParam.getValue() != null) {
            configuration.setPrettyPrint(Boolean.parseBoolean(prettyPrintParam.getValue().toString()));
        }

        Parameter supportSwaggerUiParam = module.getParameter("supportSwaggerUi");
        if (supportSwaggerUiParam != null && supportSwaggerUiParam.getValue() != null) {
            configuration.setSupportSwaggerUi(Boolean.parseBoolean(supportSwaggerUiParam.getValue().toString()));
        }

        log.debug("Loaded configuration from module parameters");
    }

    /**
     * Load configuration from specified properties file.
     */
    private void loadConfigurationFromPropertiesFile(OpenApiConfiguration configuration, AxisModule module) {
        if (module == null) return;

        Parameter propertiesFileParam = module.getParameter(PROPERTIES_FILE_PARAM);
        if (propertiesFileParam != null && propertiesFileParam.getValue() != null) {
            String propertiesFile = propertiesFileParam.getValue().toString();
            log.debug("Loading configuration from properties file: " + propertiesFile);

            try {
                OpenApiConfiguration fileConfig = new OpenApiConfiguration(propertiesFile);
                // Merge file configuration with current configuration
                mergeConfigurations(configuration, fileConfig);
            } catch (Exception e) {
                log.warn("Failed to load configuration from properties file: " + propertiesFile, e);
            }
        }
    }

    /**
     * Load context-based configuration.
     */
    private void loadContextBasedConfiguration(OpenApiConfiguration configuration, ConfigurationContext configContext) {
        try {
            // Load configuration from servlet context if available
            // This would be implemented based on specific deployment environment
            log.debug("Context-based configuration is enabled but not yet implemented");
        } catch (Exception e) {
            log.warn("Error loading context-based configuration", e);
        }
    }

    /**
     * Merge two configurations, with the source configuration overriding the target.
     */
    private void mergeConfigurations(OpenApiConfiguration target, OpenApiConfiguration source) {
        if (source.getTitle() != null && !source.getTitle().equals("Apache Axis2 REST API")) {
            target.setTitle(source.getTitle());
        }
        if (source.getDescription() != null && !source.getDescription().contains("Auto-generated")) {
            target.setDescription(source.getDescription());
        }
        if (source.getVersion() != null && !source.getVersion().equals("1.0.0")) {
            target.setVersion(source.getVersion());
        }

        // Merge collections
        if (!source.getResourcePackages().isEmpty()) {
            target.getResourcePackages().addAll(source.getResourcePackages());
        }
        if (!source.getResourceClasses().isEmpty()) {
            target.getResourceClasses().addAll(source.getResourceClasses());
        }
        if (!source.getIgnoredRoutes().isEmpty()) {
            target.getIgnoredRoutes().addAll(source.getIgnoredRoutes());
        }
        if (!source.getSecurityDefinitions().isEmpty()) {
            target.getSecurityDefinitions().putAll(source.getSecurityDefinitions());
        }

        // Copy other important properties
        target.setPrettyPrint(source.isPrettyPrint());
        target.setSupportSwaggerUi(source.isSupportSwaggerUi());
        target.setReadAllResources(source.isReadAllResources());
        target.setUseContextBasedConfig(source.isUseContextBasedConfig());

        if (source.getCustomizer() != null) {
            target.setCustomizer(source.getCustomizer());
        }
    }

    /**
     * Validate configuration for consistency and required values.
     */
    private void validateConfiguration(OpenApiConfiguration configuration) throws AxisFault {
        if (configuration == null) {
            throw new AxisFault("OpenAPI configuration cannot be null");
        }

        // Validate required fields
        if (configuration.getTitle() == null || configuration.getTitle().trim().isEmpty()) {
            configuration.setTitle("Apache Axis2 REST API");
        }

        if (configuration.getVersion() == null || configuration.getVersion().trim().isEmpty()) {
            configuration.setVersion("1.0.0");
        }

        // Validate Swagger UI version format if specified
        if (configuration.getSwaggerUiVersion() != null) {
            if (!configuration.getSwaggerUiVersion().matches("\\d+\\.\\d+\\.\\d+")) {
                log.warn("Invalid Swagger UI version format: " + configuration.getSwaggerUiVersion() +
                        ". Using default version.");
                configuration.setSwaggerUiVersion("4.15.5");
            }
        }

        // Validate security definitions
        validateSecurityDefinitions(configuration);

        log.debug("Configuration validation completed successfully");
    }

    /**
     * Validate security definitions in configuration.
     */
    private void validateSecurityDefinitions(OpenApiConfiguration configuration) {
        if (configuration.getSecurityDefinitions().isEmpty()) {
            return;
        }

        for (Map.Entry<String, SecurityScheme> entry :
                configuration.getSecurityDefinitions().entrySet()) {

            String schemeName = entry.getKey();
            SecurityScheme scheme = entry.getValue();

            if (scheme.getType() == null) {
                log.warn("Security scheme '" + schemeName + "' has no type defined, removing from configuration");
                configuration.getSecurityDefinitions().remove(schemeName);
                continue;
            }

            // Additional validation based on scheme type
            switch (scheme.getType()) {
                case HTTP:
                    if (scheme.getScheme() == null) {
                        log.warn("HTTP security scheme '" + schemeName + "' has no scheme defined");
                    }
                    break;
                case APIKEY:
                    if (scheme.getName() == null || scheme.getIn() == null) {
                        log.warn("API Key security scheme '" + schemeName + "' is missing name or location");
                    }
                    break;
                case OAUTH2:
                case OPENIDCONNECT:
                    // OAuth2 and OpenID Connect validation would go here
                    break;
            }
        }
    }

    /**
     * Apply service-specific configuration.
     */
    private void applyServiceConfiguration(AxisDescription axisDescription, OpenApiConfiguration configuration) {
        // This method can be extended to apply per-service configuration overrides
        // For example, different security schemes for different services
        String descriptorName = "unknown";
        if (axisDescription instanceof org.apache.axis2.description.AxisService) {
            descriptorName = ((org.apache.axis2.description.AxisService) axisDescription).getName();
        } else if (axisDescription instanceof org.apache.axis2.description.AxisOperation) {
            descriptorName = ((org.apache.axis2.description.AxisOperation) axisDescription).getName().getLocalPart();
        }
        log.debug("Applying service-specific configuration for: " + descriptorName);
    }

    // ========== Public API Methods ==========

    /**
     * Get the global OpenAPI configuration.
     */
    public static OpenApiConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    /**
     * Set the global OpenAPI configuration.
     */
    public static void setGlobalConfiguration(OpenApiConfiguration configuration) {
        globalConfiguration = configuration;
        log.info("Updated global OpenAPI configuration: " + configuration);
    }

    /**
     * Get OpenAPI configuration from a configuration context.
     */
    public static OpenApiConfiguration getConfiguration(ConfigurationContext configContext) {
        if (configContext == null) {
            return getGlobalConfiguration();
        }

        Object config = configContext.getProperty(CONFIG_PROPERTY);
        if (config instanceof OpenApiConfiguration) {
            return (OpenApiConfiguration) config;
        }

        return getGlobalConfiguration();
    }

    /**
     * Get OpenAPI specification generator from a configuration context.
     */
    public static OpenApiSpecGenerator getSpecGenerator(ConfigurationContext configContext) {
        if (configContext == null) {
            return null;
        }

        Object generator = configContext.getProperty(GENERATOR_PROPERTY);
        if (generator instanceof OpenApiSpecGenerator) {
            return (OpenApiSpecGenerator) generator;
        }

        return null;
    }

    /**
     * Get Swagger UI handler from a configuration context.
     */
    public static SwaggerUIHandler getSwaggerUIHandler(ConfigurationContext configContext) {
        if (configContext == null) {
            return null;
        }

        Object handler = configContext.getProperty(UI_HANDLER_PROPERTY);
        if (handler instanceof SwaggerUIHandler) {
            return (SwaggerUIHandler) handler;
        }

        return null;
    }

    /**
     * Reload configuration from sources.
     */
    public static void reloadConfiguration(ConfigurationContext configContext) {
        try {
            OpenApiConfiguration configuration = getConfiguration(configContext);
            if (configuration != null) {
                configuration.loadConfiguration();
                log.info("OpenAPI configuration reloaded successfully");
            }
        } catch (Exception e) {
            log.error("Failed to reload OpenAPI configuration", e);
        }
    }
}