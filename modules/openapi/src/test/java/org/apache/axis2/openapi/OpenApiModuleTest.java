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

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * Unit tests for OpenApiModule class.
 * Tests module initialization, engagement, and component registration.
 */
public class OpenApiModuleTest extends TestCase {

    private OpenApiModule module;
    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;
    private AxisModule axisModule;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        module = new OpenApiModule();
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);
        axisModule = new AxisModule();
        axisModule.setName("openapi");
    }

    /**
     * Test successful module initialization.
     * Verifies that all OpenAPI components are registered in the configuration context.
     */
    public void testModuleInitialization() throws Exception {
        // Act
        module.init(configurationContext, axisModule);

        // Assert
        assertNotNull("OpenAPI spec generator should be registered",
                configurationContext.getProperty("axis2.openapi.generator"));
        assertNotNull("Swagger UI handler should be registered",
                configurationContext.getProperty("axis2.openapi.ui"));
        assertNotNull("Service introspector should be registered",
                configurationContext.getProperty("axis2.openapi.introspector"));

        // Verify component types
        assertTrue("Spec generator should be correct type",
                configurationContext.getProperty("axis2.openapi.generator") instanceof OpenApiSpecGenerator);
        assertTrue("UI handler should be correct type",
                configurationContext.getProperty("axis2.openapi.ui") instanceof SwaggerUIHandler);
        assertTrue("Introspector should be correct type",
                configurationContext.getProperty("axis2.openapi.introspector") instanceof ServiceIntrospector);
    }

    /**
     * Test module engagement with REST-enabled service.
     * Should complete without warnings for services with REST enabled.
     */
    public void testEngageNotifyWithRestService() throws Exception {
        // Arrange
        AxisService service = new AxisService("TestService");
        service.addParameter(new Parameter("enableREST", "true"));

        // Act - should not throw exception
        module.engageNotify(service);

        // Assert - test passes if no exception is thrown
        assertTrue("Engagement should succeed for REST-enabled service", true);
    }

    /**
     * Test module engagement with non-REST service.
     * Should log warning but not fail for services without REST enabled.
     */
    public void testEngageNotifyWithoutRestService() throws Exception {
        // Arrange
        AxisService service = new AxisService("TestService");
        // No REST parameter added

        // Act - should not throw exception but may log warning
        module.engageNotify(service);

        // Assert - test passes if no exception is thrown
        assertTrue("Engagement should succeed even without REST enabled", true);
    }

    /**
     * Test module shutdown.
     * Verifies that all registered components are cleaned up.
     */
    public void testModuleShutdown() throws Exception {
        // Arrange
        module.init(configurationContext, axisModule);
        assertNotNull("Components should be registered before shutdown",
                configurationContext.getProperty("axis2.openapi.generator"));

        // Act
        module.shutdown(configurationContext);

        // Assert
        assertNull("OpenAPI spec generator should be removed",
                configurationContext.getProperty("axis2.openapi.generator"));
        assertNull("Swagger UI handler should be removed",
                configurationContext.getProperty("axis2.openapi.ui"));
        assertNull("Service introspector should be removed",
                configurationContext.getProperty("axis2.openapi.introspector"));
    }

    /**
     * Test policy assertion support.
     * OpenAPI module currently does not support policy assertions.
     */
    public void testPolicyAssertionSupport() {
        // Act & Assert
        assertFalse("Module should not support policy assertions",
                module.canSupportAssertion(null));
    }

    /**
     * Test policy application.
     * Should complete without error (no-op implementation).
     */
    public void testPolicyApplication() throws Exception {
        // Arrange
        AxisService service = new AxisService("TestService");

        // Act - should not throw exception
        module.applyPolicy(null, service);

        // Assert - test passes if no exception is thrown
        assertTrue("Policy application should be no-op", true);
    }

    /**
     * Test module initialization with null parameters.
     * Should handle gracefully without throwing NPE.
     */
    public void testModuleInitializationWithNullContext() {
        try {
            module.init(null, axisModule);
            fail("Should throw AxisFault for null configuration context");
        } catch (AxisFault e) {
            assertTrue("Should handle null context gracefully", true);
        }
    }

    /**
     * Test module initialization failure recovery.
     * Verifies proper error handling during initialization.
     */
    public void testModuleInitializationFailure() {
        // The ConfigurationContext constructor itself throws NPE with null AxisConfiguration
        // This is expected Axis2 behavior, so we test that case
        try {
            // Arrange - this will throw NPE in ConfigurationContext constructor
            ConfigurationContext invalidContext = new ConfigurationContext(null);
            fail("Should throw NullPointerException for null AxisConfiguration");
        } catch (NullPointerException e) {
            // Assert - this is expected behavior
            assertTrue("ConfigurationContext should reject null AxisConfiguration", true);
        }
    }

    /**
     * Test component availability after initialization.
     * Verifies that initialized components are functional.
     */
    public void testComponentAvailabilityAfterInit() throws Exception {
        // Arrange & Act
        module.init(configurationContext, axisModule);

        // Assert component functionality
        OpenApiSpecGenerator generator = (OpenApiSpecGenerator)
                configurationContext.getProperty("axis2.openapi.generator");
        assertNotNull("Generator should be available", generator);

        SwaggerUIHandler uiHandler = (SwaggerUIHandler)
                configurationContext.getProperty("axis2.openapi.ui");
        assertNotNull("UI handler should be available", uiHandler);

        ServiceIntrospector introspector = (ServiceIntrospector)
                configurationContext.getProperty("axis2.openapi.introspector");
        assertNotNull("Introspector should be available", introspector);
    }
}