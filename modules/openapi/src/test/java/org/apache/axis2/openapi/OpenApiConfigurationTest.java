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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for OpenApiConfiguration.
 *
 * Tests all aspects of configuration including:
 * - Default values and initialization
 * - Property loading from various sources
 * - Configuration validation and merging
 * - Security scheme definitions
 * - Resource filtering configuration
 * - Swagger UI configuration integration
 */
public class OpenApiConfigurationTest {

    private OpenApiConfiguration config;

    @Before
    public void setUp() {
        config = new OpenApiConfiguration();
    }

    // ========== Default Configuration Tests ==========

    @Test
    public void testInitializeWithDefaults() {
        assertNotNull(config);
        assertEquals("Apache Axis2 REST API", config.getTitle());
        assertTrue(config.getDescription().contains("Auto-generated"));
        assertEquals("1.0.0", config.getVersion());
        assertEquals("Apache Axis2", config.getContactName());
        assertEquals("https://axis.apache.org/axis2/java/core/", config.getContactUrl());
        assertEquals("Apache License 2.0", config.getLicense());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", config.getLicenseUrl());
    }

    @Test
    public void testDefaultBehaviorFlags() {
        assertTrue(config.isReadAllResources());
        assertTrue(config.isPrettyPrint());
        assertTrue(config.isSupportSwaggerUi());
        assertTrue(config.isScanKnownConfigLocations());
        assertFalse(config.isUseContextBasedConfig());
    }

    @Test
    public void testDefaultCollections() {
        assertNotNull(config.getResourcePackages());
        assertNotNull(config.getResourceClasses());
        assertNotNull(config.getIgnoredRoutes());
        assertNotNull(config.getSecurityDefinitions());
        assertNotNull(config.getSwaggerUiMediaTypes());

        // Should have default security scheme
        assertTrue(config.getSecurityDefinitions().containsKey("basicAuth"));
        SecurityScheme basicAuth = config.getSecurityDefinitions().get("basicAuth");
        assertEquals(SecurityScheme.Type.HTTP, basicAuth.getType());
        assertEquals("basic", basicAuth.getScheme());
    }

    @Test
    public void testDefaultSwaggerUiConfig() {
        assertNotNull(config.getSwaggerUiConfig());
        assertEquals("4.15.5", config.getSwaggerUiVersion());
        assertEquals("org.webjars:swagger-ui", config.getSwaggerUiMavenGroupAndArtifact());
    }

    // ========== Property Configuration Tests ==========

    @Test
    public void testLoadFromPropertiesFile() {
        // Test loading from classpath properties file
        OpenApiConfiguration fileConfig = new OpenApiConfiguration("test-openapi.properties");

        // Verify properties were loaded
        assertNotNull(fileConfig);
        assertEquals("Test API Title", fileConfig.getTitle());
        assertEquals("Test API Description from Properties File", fileConfig.getDescription());
        assertEquals("2.5.0", fileConfig.getVersion());
        assertEquals("Test Contact", fileConfig.getContactName());
        assertEquals("test@example.com", fileConfig.getContactEmail());
        assertTrue(fileConfig.getResourcePackages().contains("com.test.api"));
        assertTrue(fileConfig.getResourcePackages().contains("com.test.services"));
    }

    @Test
    public void testMergePropertiesConfiguration() {
        Properties props = new Properties();
        props.setProperty("openapi.title", "Custom API Title");
        props.setProperty("openapi.version", "2.0.0");
        props.setProperty("openapi.prettyPrint", "false");
        props.setProperty("openapi.resourcePackages", "com.example.api,com.example.services");

        // Simulate property application
        config.setTitle(props.getProperty("openapi.title", config.getTitle()));
        config.setVersion(props.getProperty("openapi.version", config.getVersion()));
        config.setPrettyPrint(Boolean.parseBoolean(props.getProperty("openapi.prettyPrint", "true")));

        String packages = props.getProperty("openapi.resourcePackages");
        if (packages != null) {
            config.getResourcePackages().addAll(Arrays.asList(packages.split("\\s*,\\s*")));
        }

        assertEquals("Custom API Title", config.getTitle());
        assertEquals("2.0.0", config.getVersion());
        assertFalse(config.isPrettyPrint());
        assertTrue(config.getResourcePackages().contains("com.example.api"));
        assertTrue(config.getResourcePackages().contains("com.example.services"));
    }

    @Test
    public void testUserProperties() {
        Map<String, Object> userOptions = new java.util.HashMap<>();
        userOptions.put("custom.property", "value1");
        userOptions.put("another.property", 123);

        Properties userProps = config.getUserProperties(userOptions);

        assertNotNull(userProps);
        assertEquals("value1", userProps.getProperty("custom.property"));
        assertEquals("123", userProps.getProperty("another.property"));
    }

    @Test
    public void testHandleMissingPropertiesFile() {
        OpenApiConfiguration config = new OpenApiConfiguration("non-existent.properties");

        // Should fall back to defaults
        assertEquals("Apache Axis2 REST API", config.getTitle());
        assertEquals("1.0.0", config.getVersion());
    }

    @Test
    public void testPropertyPrecedence() {
        // Set system property
        System.setProperty("openapi.title", "System Property Title");

        try {
            OpenApiConfiguration config = new OpenApiConfiguration("test-openapi.properties");
            config.loadConfiguration();

            // System property should override file property
            assertEquals("System Property Title", config.getTitle());
        } finally {
            // Clean up system property
            System.clearProperty("openapi.title");
        }
    }

    // ========== Security Configuration Tests ==========

    @Test
    public void testCustomSecuritySchemes() {
        // Test OAuth2 security scheme
        SecurityScheme oauth2 = new SecurityScheme();
        oauth2.setType(SecurityScheme.Type.OAUTH2);
        oauth2.setDescription("OAuth2 authentication");

        config.addSecurityDefinition("oauth2", oauth2);

        assertTrue(config.getSecurityDefinitions().containsKey("oauth2"));
        assertEquals(SecurityScheme.Type.OAUTH2, config.getSecurityDefinitions().get("oauth2").getType());
    }

    @Test
    public void testApiKeySecuritySchemes() {
        SecurityScheme apiKey = new SecurityScheme();
        apiKey.setType(SecurityScheme.Type.APIKEY);
        apiKey.setName("X-API-Key");
        apiKey.setIn(SecurityScheme.In.HEADER);
        apiKey.setDescription("API Key authentication");

        config.addSecurityDefinition("apiKey", apiKey);

        SecurityScheme stored = config.getSecurityDefinitions().get("apiKey");
        assertEquals(SecurityScheme.Type.APIKEY, stored.getType());
        assertEquals("X-API-Key", stored.getName());
        assertEquals(SecurityScheme.In.HEADER, stored.getIn());
    }

    @Test
    public void testBearerTokenSecuritySchemes() {
        SecurityScheme bearer = new SecurityScheme();
        bearer.setType(SecurityScheme.Type.HTTP);
        bearer.setScheme("bearer");
        bearer.setBearerFormat("JWT");
        bearer.setDescription("Bearer token authentication");

        config.addSecurityDefinition("bearerAuth", bearer);

        SecurityScheme stored = config.getSecurityDefinitions().get("bearerAuth");
        assertEquals(SecurityScheme.Type.HTTP, stored.getType());
        assertEquals("bearer", stored.getScheme());
        assertEquals("JWT", stored.getBearerFormat());
    }

    // ========== Resource Filtering Tests ==========

    @Test
    public void testResourcePackages() {
        config.addResourcePackage("com.example.api");
        config.addResourcePackage("com.example.services");

        Set<String> packages = config.getResourcePackages();
        assertTrue(packages.contains("com.example.api"));
        assertTrue(packages.contains("com.example.services"));
        assertEquals(2, packages.size());
    }

    @Test
    public void testResourceClasses() {
        config.addResourceClass("com.example.api.UserService");
        config.addResourceClass("com.example.api.OrderService");

        Set<String> classes = config.getResourceClasses();
        assertTrue(classes.contains("com.example.api.UserService"));
        assertTrue(classes.contains("com.example.api.OrderService"));
        assertEquals(2, classes.size());
    }

    @Test
    public void testIgnoredRoutes() {
        config.addIgnoredRoute("/health");
        config.addIgnoredRoute("/metrics");
        config.addIgnoredRoute("/admin/.*");

        assertTrue(config.getIgnoredRoutes().contains("/health"));
        assertTrue(config.getIgnoredRoutes().contains("/metrics"));
        assertTrue(config.getIgnoredRoutes().contains("/admin/.*"));
        assertEquals(3, config.getIgnoredRoutes().size());
    }

    @Test
    public void testReadAllResourcesFlag() {
        // Test default
        assertTrue(config.isReadAllResources());

        // Test setting to false
        config.setReadAllResources(false);
        assertFalse(config.isReadAllResources());

        // When false, specific resource configuration should matter
        config.addResourcePackage("com.example.api");
        assertEquals(1, config.getResourcePackages().size());
    }

    // ========== Swagger UI Configuration Tests ==========

    @Test
    public void testSwaggerUiConfiguration() {
        SwaggerUiConfig uiConfig = config.getSwaggerUiConfig();
        assertNotNull(uiConfig);

        // Test customization
        uiConfig.setDeepLinking(true);
        uiConfig.setDocExpansion("list");
        uiConfig.setFilter(true);

        assertTrue(uiConfig.isDeepLinking());
        assertEquals("list", uiConfig.getDocExpansion());
        assertTrue(uiConfig.isFilter());
    }

    @Test
    public void testCustomSwaggerUiVersion() {
        config.setSwaggerUiVersion("4.18.0");
        assertEquals("4.18.0", config.getSwaggerUiVersion());
    }

    @Test
    public void testCustomCssAndJs() {
        SwaggerUiConfig uiConfig = config.getSwaggerUiConfig();
        uiConfig.setCustomCss("/custom/theme.css");
        uiConfig.setCustomJs("/custom/behavior.js");

        assertEquals("/custom/theme.css", uiConfig.getCustomCss());
        assertEquals("/custom/behavior.js", uiConfig.getCustomJs());
    }

    @Test
    public void testMediaTypeConfiguration() {
        Map<String, String> mediaTypes = config.getSwaggerUiMediaTypes();
        assertNotNull(mediaTypes);

        // Should have default media types
        assertTrue(mediaTypes.containsKey("css"));
        assertTrue(mediaTypes.containsKey("js"));
        assertTrue(mediaTypes.containsKey("json"));

        assertEquals("text/css", mediaTypes.get("css"));
        assertEquals("application/javascript", mediaTypes.get("js"));
        assertEquals("application/json", mediaTypes.get("json"));
    }

    // ========== Configuration Copy and Merge Tests ==========

    @Test
    public void testDeepCopy() {
        // Setup original configuration
        config.setTitle("Original Title");
        config.addResourcePackage("com.example");
        config.addSecurityDefinition("test", new SecurityScheme().type(SecurityScheme.Type.HTTP));

        // Create copy
        OpenApiConfiguration copy = config.copy();

        // Verify copy is independent
        copy.setTitle("Copy Title");
        copy.addResourcePackage("com.copy");

        assertEquals("Original Title", config.getTitle());
        assertEquals("Copy Title", copy.getTitle());

        assertTrue(config.getResourcePackages().contains("com.example"));
        assertTrue(copy.getResourcePackages().contains("com.example"));
        assertTrue(copy.getResourcePackages().contains("com.copy"));
        assertFalse(config.getResourcePackages().contains("com.copy"));
    }

    @Test
    public void testHandleNullValuesInCopy() {
        config.setContactEmail(null);
        config.setTermsOfServiceUrl(null);

        OpenApiConfiguration copy = config.copy();

        assertNull(copy.getContactEmail());
        assertNull(copy.getTermsOfServiceUrl());
    }

    // ========== Customizer Integration Tests ==========

    @Test
    public void testOpenApiCustomizer() {
        OpenApiCustomizer customizer = new OpenApiCustomizer() {
            @Override
            public void customize(io.swagger.v3.oas.models.OpenAPI openAPI) {
                openAPI.getInfo().setTitle("Customized Title");
            }

            @Override
            public int getPriority() {
                return 100;
            }
        };

        config.setCustomizer(customizer);

        assertNotNull(config.getCustomizer());
        assertEquals(100, config.getCustomizer().getPriority());
    }

    @Test
    public void testNullCustomizer() {
        config.setCustomizer(null);
        assertNull(config.getCustomizer());
    }

    // ========== Validation and Error Handling Tests ==========

    @Test
    public void testHandleEmptyValues() {
        config.setTitle("");
        config.setDescription(null);

        assertEquals("", config.getTitle());
        assertNull(config.getDescription());
    }

    @Test
    public void testLargeConfiguration() {
        // Add many resources
        for (int i = 0; i < 1000; i++) {
            config.addResourcePackage("com.example.package" + i);
            config.addIgnoredRoute("/route" + i + "/.*");
        }

        assertEquals(1000, config.getResourcePackages().size());
        assertEquals(1000, config.getIgnoredRoutes().size());
    }

    @Test
    public void testMeaningfulToString() {
        String toString = config.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("OpenApiConfiguration"));
        assertTrue(toString.contains(config.getTitle()));
    }

    @Test
    public void testHandleNullPropertiesInToString() {
        config.setContactEmail(null);
        config.setTermsOfServiceUrl(null);

        String toString = config.toString();
        assertNotNull(toString);
        // Should not throw NPE
    }

    // ========== Performance Tests ==========

    @Test
    public void testLargeConfigurationPerformance() {
        long startTime = System.currentTimeMillis();

        // Add large number of configurations
        for (int i = 0; i < 10000; i++) {
            config.addResourcePackage("com.large.test.package" + i);
            config.addResourceClass("com.large.test.Class" + i);
            config.addIgnoredRoute("/api/v" + i + "/.*");

            SecurityScheme scheme = new SecurityScheme();
            scheme.setType(SecurityScheme.Type.APIKEY);
            scheme.setName("api-key-" + i);
            scheme.setIn(SecurityScheme.In.HEADER);
            config.addSecurityDefinition("apiKey" + i, scheme);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Should complete within reasonable time (less than 5 seconds)
        assertTrue("Configuration should be created efficiently, took " + duration + "ms", duration < 5000);

        assertEquals(10000, config.getResourcePackages().size());
        assertEquals(10000, config.getResourceClasses().size());
        assertEquals(10000, config.getIgnoredRoutes().size());
        // +1 for default basicAuth scheme
        assertEquals(10001, config.getSecurityDefinitions().size());
    }

    @Test
    public void testCopyPerformance() {
        // Setup large configuration
        for (int i = 0; i < 5000; i++) {
            config.addResourcePackage("com.copy.test.package" + i);
            config.addResourceClass("com.copy.test.Class" + i);
        }

        long startTime = System.currentTimeMillis();
        OpenApiConfiguration copy = config.copy();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Should copy within reasonable time (less than 2 seconds)
        assertTrue("Copy should be efficient, took " + duration + "ms", duration < 2000);

        assertEquals(config.getResourcePackages().size(), copy.getResourcePackages().size());
        assertEquals(config.getResourceClasses().size(), copy.getResourceClasses().size());

        // Verify independence
        copy.addResourcePackage("com.copy.unique");
        assertFalse(config.getResourcePackages().contains("com.copy.unique"));
    }

    // ========== Integration Tests ==========

    @Test
    public void testCompleteConfigurationScenario() {
        // Complete configuration setup
        config.setTitle("Complete API");
        config.setVersion("1.5.0");
        config.setDescription("Complete API with all features");
        config.setContactName("API Team");
        config.setContactEmail("api@example.com");
        config.setLicense("MIT License");
        config.setLicenseUrl("https://opensource.org/licenses/MIT");

        // Resource configuration
        config.addResourcePackage("com.example.api");
        config.addResourceClass("com.example.UserService");
        config.addIgnoredRoute("/internal/.*");

        // Security configuration
        SecurityScheme oauth2 = new SecurityScheme();
        oauth2.setType(SecurityScheme.Type.OAUTH2);
        config.addSecurityDefinition("oauth2", oauth2);

        // UI configuration
        SwaggerUiConfig uiConfig = config.getSwaggerUiConfig();
        uiConfig.setDeepLinking(true);
        uiConfig.setDocExpansion("none");

        // Verify complete setup
        assertEquals("Complete API", config.getTitle());
        assertTrue(config.getResourcePackages().contains("com.example.api"));
        assertTrue(config.getSecurityDefinitions().containsKey("oauth2"));
        assertTrue(uiConfig.isDeepLinking());
        assertEquals("none", uiConfig.getDocExpansion());

        // Test copy with complete configuration
        OpenApiConfiguration copy = config.copy();
        assertEquals(config.getTitle(), copy.getTitle());
        assertEquals(config.getResourcePackages().size(), copy.getResourcePackages().size());
    }

    @Test
    public void testRealPropertiesFileIntegration() {
        // Test complete integration with actual properties file
        OpenApiConfiguration config = new OpenApiConfiguration("test-openapi.properties");

        // Verify all properties loaded correctly
        assertEquals("Test API Title", config.getTitle());
        assertEquals("2.5.0", config.getVersion());
        assertEquals("Test Contact", config.getContactName());
        assertEquals("4.18.0", config.getSwaggerUiVersion());
        assertFalse(config.isReadAllResources());
        assertTrue(config.isPrettyPrint());

        // Test copy preserves loaded properties
        OpenApiConfiguration copy = config.copy();
        assertEquals("Test API Title", copy.getTitle());
        assertEquals("2.5.0", copy.getVersion());
        assertTrue(copy.getResourcePackages().contains("com.test.api"));
        assertTrue(copy.getResourcePackages().contains("com.test.services"));
    }
}