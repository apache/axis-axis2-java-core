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

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Comprehensive configuration class for Apache Axis2 OpenAPI integration.
 *
 * This class provides extensive configuration options for customizing OpenAPI
 * specification generation, Swagger UI behavior, and integration settings.
 * It supports both programmatic configuration and property file-based configuration.
 *
 * <p>Configuration can be loaded from:
 * <ul>
 *   <li>Programmatic API calls</li>
 *   <li>Properties files (openapi.properties)</li>
 *   <li>System properties</li>
 *   <li>Environment variables</li>
 * </ul>
 *
 * @since Axis2 2.0.1
 */
public class OpenApiConfiguration {

    private static final Log log = LogFactory.getLog(OpenApiConfiguration.class);

    // ========== API Information Configuration ==========

    /** API title - defaults to "Apache Axis2 REST API" */
    private String title = "Apache Axis2 REST API";

    /** API description */
    private String description = "Auto-generated OpenAPI documentation for Apache Axis2 REST services";

    /** API version - defaults to "1.0.0" */
    private String version = "1.0.0";

    /** Terms of service URL */
    private String termsOfServiceUrl;

    // Contact information
    private String contactName = "Apache Axis2";
    private String contactEmail;
    private String contactUrl = "https://axis.apache.org/axis2/java/core/";

    // License information
    private String license = "Apache License 2.0";
    private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";

    // ========== Service Discovery Configuration ==========

    /** Whether to read all resources automatically */
    private boolean readAllResources = true;

    /** Specific resource packages to scan */
    private Set<String> resourcePackages = new HashSet<>();

    /** Specific resource classes to include */
    private Set<String> resourceClasses = new HashSet<>();

    /** Routes/paths to ignore during generation */
    private Collection<String> ignoredRoutes = new ArrayList<>();

    /** Whether to scan known configuration locations */
    private boolean scanKnownConfigLocations = true;

    // ========== OpenAPI Generation Configuration ==========

    /** Whether to pretty print the generated JSON/YAML */
    private boolean prettyPrint = true;

    /** Whether to use context-based configuration */
    private boolean useContextBasedConfig = false;

    /** Custom scanner class for service discovery */
    private String scannerClass;

    /** OpenAPI customizer for post-processing */
    private OpenApiCustomizer customizer;

    // ========== Security Configuration ==========

    /** Security scheme definitions */
    private Map<String, SecurityScheme> securityDefinitions = new HashMap<>();

    // ========== Swagger UI Configuration ==========

    /** Whether to support Swagger UI */
    private boolean supportSwaggerUi = true;

    /** Swagger UI version to use */
    private String swaggerUiVersion = "4.15.5";

    /** Maven group and artifact for Swagger UI */
    private String swaggerUiMavenGroupAndArtifact = "org.webjars:swagger-ui";

    /** Media types for Swagger UI resources */
    private Map<String, String> swaggerUiMediaTypes = createDefaultMediaTypes();

    /** Custom Swagger UI configuration */
    private SwaggerUiConfig swaggerUiConfig = new SwaggerUiConfig();

    // ========== Configuration File Support ==========

    /** Location of OpenAPI configuration file */
    private String configLocation;

    /** Location of properties file */
    private String propertiesLocation = "openapi.properties";

    /** Loaded properties */
    private Properties properties;

    // ========== Constructors ==========

    /**
     * Default constructor with standard configuration.
     */
    public OpenApiConfiguration() {
        loadDefaultConfiguration();
    }

    /**
     * Constructor that loads configuration from specified properties file.
     *
     * @param propertiesLocation path to properties file
     */
    public OpenApiConfiguration(String propertiesLocation) {
        this.propertiesLocation = propertiesLocation;
        loadConfiguration();
    }

    // ========== Configuration Loading Methods ==========

    /**
     * Load configuration from various sources in order of precedence:
     * 1. System properties
     * 2. Properties file
     * 3. Default values
     */
    public void loadConfiguration() {
        loadDefaultConfiguration();
        loadPropertiesConfiguration();
        loadSystemPropertiesConfiguration();

        if (scanKnownConfigLocations) {
            scanForAdditionalConfigurations();
        }
    }

    /**
     * Load default configuration values.
     */
    private void loadDefaultConfiguration() {
        // Add default security scheme
        SecurityScheme basicAuth = new SecurityScheme();
        basicAuth.setType(SecurityScheme.Type.HTTP);
        basicAuth.setScheme("basic");
        basicAuth.setDescription("Basic Authentication");
        securityDefinitions.put("basicAuth", basicAuth);
    }

    /**
     * Load configuration from properties file.
     */
    private void loadPropertiesConfiguration() {
        if (propertiesLocation == null) return;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(propertiesLocation)) {
            if (is != null) {
                properties = new Properties();
                properties.load(is);
                applyPropertiesConfiguration(properties);
                log.info("Loaded OpenAPI configuration from: " + propertiesLocation);
            }
        } catch (IOException e) {
            log.warn("Failed to load OpenAPI properties from: " + propertiesLocation, e);
        }
    }

    /**
     * Load configuration from system properties.
     */
    private void loadSystemPropertiesConfiguration() {
        Properties systemProps = System.getProperties();
        applyPropertiesConfiguration(systemProps);
    }

    /**
     * Apply configuration from properties object.
     */
    private void applyPropertiesConfiguration(Properties props) {
        // API Information
        title = getProperty(props, "openapi.title", title);
        description = getProperty(props, "openapi.description", description);
        version = getProperty(props, "openapi.version", version);
        termsOfServiceUrl = getProperty(props, "openapi.termsOfServiceUrl", termsOfServiceUrl);

        // Contact
        contactName = getProperty(props, "openapi.contact.name", contactName);
        contactEmail = getProperty(props, "openapi.contact.email", contactEmail);
        contactUrl = getProperty(props, "openapi.contact.url", contactUrl);

        // License
        license = getProperty(props, "openapi.license.name", license);
        licenseUrl = getProperty(props, "openapi.license.url", licenseUrl);

        // Configuration flags
        readAllResources = getBooleanProperty(props, "openapi.readAllResources", readAllResources);
        prettyPrint = getBooleanProperty(props, "openapi.prettyPrint", prettyPrint);
        supportSwaggerUi = getBooleanProperty(props, "openapi.swaggerUi.enabled", supportSwaggerUi);
        useContextBasedConfig = getBooleanProperty(props, "openapi.useContextBasedConfig", useContextBasedConfig);

        // Swagger UI
        swaggerUiVersion = getProperty(props, "openapi.swaggerUi.version", swaggerUiVersion);

        // Resource packages (comma-separated)
        String packages = getProperty(props, "openapi.resourcePackages", null);
        if (packages != null) {
            resourcePackages.addAll(Arrays.asList(packages.split("\\s*,\\s*")));
        }
    }

    /**
     * Scan for additional configuration files in known locations.
     */
    private void scanForAdditionalConfigurations() {
        String[] knownLocations = {
            "META-INF/openapi.properties",
            "WEB-INF/openapi.properties",
            "openapi-config.properties"
        };

        for (String location : knownLocations) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(location)) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    applyPropertiesConfiguration(props);
                    log.debug("Loaded additional configuration from: " + location);
                }
            } catch (IOException e) {
                log.debug("Could not load configuration from: " + location);
            }
        }
    }

    // ========== Utility Methods ==========

    private String getProperty(Properties props, String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    private boolean getBooleanProperty(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    private Map<String, String> createDefaultMediaTypes() {
        Map<String, String> mediaTypes = new HashMap<>();
        mediaTypes.put("css", "text/css");
        mediaTypes.put("js", "application/javascript");
        mediaTypes.put("json", "application/json");
        mediaTypes.put("html", "text/html");
        mediaTypes.put("png", "image/png");
        mediaTypes.put("ico", "image/x-icon");
        return mediaTypes;
    }

    /**
     * Get user-defined properties merged with configuration properties.
     */
    public Properties getUserProperties(Map<String, Object> userDefinedOptions) {
        Properties userProps = new Properties();

        if (properties != null) {
            userProps.putAll(properties);
        }

        if (userDefinedOptions != null) {
            userDefinedOptions.forEach((key, value) ->
                userProps.setProperty(key, String.valueOf(value)));
        }

        return userProps;
    }

    // ========== Getters and Setters ==========

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getTermsOfServiceUrl() { return termsOfServiceUrl; }
    public void setTermsOfServiceUrl(String termsOfServiceUrl) { this.termsOfServiceUrl = termsOfServiceUrl; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactUrl() { return contactUrl; }
    public void setContactUrl(String contactUrl) { this.contactUrl = contactUrl; }

    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }

    public boolean isReadAllResources() { return readAllResources; }
    public void setReadAllResources(boolean readAllResources) { this.readAllResources = readAllResources; }

    public Set<String> getResourcePackages() { return resourcePackages; }
    public void setResourcePackages(Set<String> resourcePackages) { this.resourcePackages = resourcePackages; }

    public Set<String> getResourceClasses() { return resourceClasses; }
    public void setResourceClasses(Set<String> resourceClasses) { this.resourceClasses = resourceClasses; }

    public Collection<String> getIgnoredRoutes() { return ignoredRoutes; }
    public void setIgnoredRoutes(Collection<String> ignoredRoutes) { this.ignoredRoutes = ignoredRoutes; }

    public boolean isPrettyPrint() { return prettyPrint; }
    public void setPrettyPrint(boolean prettyPrint) { this.prettyPrint = prettyPrint; }

    public boolean isUseContextBasedConfig() { return useContextBasedConfig; }
    public void setUseContextBasedConfig(boolean useContextBasedConfig) { this.useContextBasedConfig = useContextBasedConfig; }

    public String getScannerClass() { return scannerClass; }
    public void setScannerClass(String scannerClass) { this.scannerClass = scannerClass; }

    public OpenApiCustomizer getCustomizer() { return customizer; }
    public void setCustomizer(OpenApiCustomizer customizer) { this.customizer = customizer; }

    public Map<String, SecurityScheme> getSecurityDefinitions() { return securityDefinitions; }
    public void setSecurityDefinitions(Map<String, SecurityScheme> securityDefinitions) { this.securityDefinitions = securityDefinitions; }

    public boolean isSupportSwaggerUi() { return supportSwaggerUi; }
    public void setSupportSwaggerUi(boolean supportSwaggerUi) { this.supportSwaggerUi = supportSwaggerUi; }

    public String getSwaggerUiVersion() { return swaggerUiVersion; }
    public void setSwaggerUiVersion(String swaggerUiVersion) { this.swaggerUiVersion = swaggerUiVersion; }

    public String getSwaggerUiMavenGroupAndArtifact() { return swaggerUiMavenGroupAndArtifact; }
    public void setSwaggerUiMavenGroupAndArtifact(String swaggerUiMavenGroupAndArtifact) {
        this.swaggerUiMavenGroupAndArtifact = swaggerUiMavenGroupAndArtifact;
    }

    public Map<String, String> getSwaggerUiMediaTypes() { return swaggerUiMediaTypes; }
    public void setSwaggerUiMediaTypes(Map<String, String> swaggerUiMediaTypes) {
        this.swaggerUiMediaTypes = swaggerUiMediaTypes;
    }

    public SwaggerUiConfig getSwaggerUiConfig() { return swaggerUiConfig; }
    public void setSwaggerUiConfig(SwaggerUiConfig swaggerUiConfig) { this.swaggerUiConfig = swaggerUiConfig; }

    public String getConfigLocation() { return configLocation; }
    public void setConfigLocation(String configLocation) { this.configLocation = configLocation; }

    public String getPropertiesLocation() { return propertiesLocation; }
    public void setPropertiesLocation(String propertiesLocation) { this.propertiesLocation = propertiesLocation; }

    public boolean isScanKnownConfigLocations() { return scanKnownConfigLocations; }
    public void setScanKnownConfigLocations(boolean scanKnownConfigLocations) {
        this.scanKnownConfigLocations = scanKnownConfigLocations;
    }

    public Properties getProperties() { return properties; }

    // ========== Convenience Methods ==========

    /**
     * Add a security definition to the configuration.
     */
    public void addSecurityDefinition(String name, SecurityScheme scheme) {
        securityDefinitions.put(name, scheme);
    }

    /**
     * Add a resource package to scan.
     */
    public void addResourcePackage(String packageName) {
        resourcePackages.add(packageName);
    }

    /**
     * Add a resource class to include.
     */
    public void addResourceClass(String className) {
        resourceClasses.add(className);
    }

    /**
     * Add a route to ignore during generation.
     */
    public void addIgnoredRoute(String route) {
        ignoredRoutes.add(route);
    }

    /**
     * Create a copy of this configuration.
     */
    public OpenApiConfiguration copy() {
        OpenApiConfiguration copy = new OpenApiConfiguration();

        // Copy primitive fields
        copy.title = this.title;
        copy.description = this.description;
        copy.version = this.version;
        copy.termsOfServiceUrl = this.termsOfServiceUrl;
        copy.contactName = this.contactName;
        copy.contactEmail = this.contactEmail;
        copy.contactUrl = this.contactUrl;
        copy.license = this.license;
        copy.licenseUrl = this.licenseUrl;
        copy.readAllResources = this.readAllResources;
        copy.prettyPrint = this.prettyPrint;
        copy.useContextBasedConfig = this.useContextBasedConfig;
        copy.scannerClass = this.scannerClass;
        copy.supportSwaggerUi = this.supportSwaggerUi;
        copy.swaggerUiVersion = this.swaggerUiVersion;
        copy.swaggerUiMavenGroupAndArtifact = this.swaggerUiMavenGroupAndArtifact;
        copy.configLocation = this.configLocation;
        copy.propertiesLocation = this.propertiesLocation;
        copy.scanKnownConfigLocations = this.scanKnownConfigLocations;

        // Copy collections
        copy.resourcePackages = new HashSet<>(this.resourcePackages);
        copy.resourceClasses = new HashSet<>(this.resourceClasses);
        copy.ignoredRoutes = new ArrayList<>(this.ignoredRoutes);
        copy.securityDefinitions = new HashMap<>(this.securityDefinitions);
        copy.swaggerUiMediaTypes = new HashMap<>(this.swaggerUiMediaTypes);

        // Copy complex objects
        copy.customizer = this.customizer;
        copy.swaggerUiConfig = this.swaggerUiConfig != null ? this.swaggerUiConfig.copy() : null;

        if (this.properties != null) {
            copy.properties = new Properties();
            copy.properties.putAll(this.properties);
        }

        return copy;
    }

    @Override
    public String toString() {
        return "OpenApiConfiguration{" +
                "title='" + title + '\'' +
                ", version='" + version + '\'' +
                ", resourcePackages=" + resourcePackages.size() +
                ", supportSwaggerUi=" + supportSwaggerUi +
                ", prettyPrint=" + prettyPrint +
                '}';
    }
}