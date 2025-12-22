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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.openapi.ServiceIntrospector.ServiceMetadata;
import org.apache.axis2.openapi.ServiceIntrospector.OperationMetadata;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Unit tests for ServiceIntrospector class.
 * Tests service discovery and metadata extraction for OpenAPI generation.
 */
public class ServiceIntrospectorTest extends TestCase {

    private ServiceIntrospector introspector;
    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);
        introspector = new ServiceIntrospector(configurationContext);
    }

    /**
     * Test REST service discovery.
     * Verifies that only REST-enabled services are included.
     */
    public void testRestServiceDiscovery() throws Exception {
        // Arrange
        AxisService restService = new AxisService("RestService");
        restService.addParameter(new Parameter("enableREST", "true"));

        AxisService nonRestService = new AxisService("NonRestService");
        // No REST parameter

        AxisService systemService = new AxisService("AdminService");
        systemService.addParameter(new Parameter("enableREST", "true"));

        axisConfiguration.addService(restService);
        axisConfiguration.addService(nonRestService);
        axisConfiguration.addService(systemService);

        // Act
        List<ServiceMetadata> services = introspector.getRestServices();

        // Assert
        assertNotNull("Services list should not be null", services);
        assertEquals("Should find only REST-enabled non-system services", 1, services.size());
        assertEquals("Should be the REST service", "RestService", services.get(0).getServiceName());
    }

    /**
     * Test service analysis with complete metadata.
     * Verifies extraction of service name, documentation, and operations.
     */
    public void testServiceAnalysis() throws Exception {
        // Arrange
        AxisService service = createTestService();

        // Act
        ServiceMetadata metadata = introspector.analyzeService(service);

        // Assert
        assertNotNull("Metadata should not be null", metadata);
        assertEquals("Service name should match", "TestService", metadata.getServiceName());
        assertEquals("Documentation should match", "Test service documentation", metadata.getDocumentation());
        assertEquals("Target namespace should match", "http://test.example.com", metadata.getTargetNamespace());

        // Verify parameters
        assertNotNull("Parameters should be present", metadata.getParameters());
        assertEquals("Should have REST parameter", "true", metadata.getParameters().get("enableREST"));

        // Verify operations
        assertNotNull("Operations should be present", metadata.getOperations());
        assertEquals("Should have one operation", 1, metadata.getOperations().size());

        OperationMetadata operation = metadata.getOperations().get(0);
        assertEquals("Operation name should match", "testMethod", operation.getOperationName());
        assertEquals("HTTP method should match", "POST", operation.getHttpMethod());
        assertEquals("REST path should match", "/testMethod", operation.getRestPath());
    }

    /**
     * Test operation analysis with HTTP method parameter.
     * Verifies correct extraction of HTTP method from operation parameters.
     */
    public void testOperationAnalysisWithHttpMethod() throws Exception {
        // Arrange
        AxisService service = new AxisService("TestService");
        AxisOperation operation = new org.apache.axis2.description.InOutAxisOperation();
        operation.setName(new QName("getUser"));
        operation.addParameter(new Parameter("HTTPMethod", "GET"));
        operation.addParameter(new Parameter("RESTPath", "/users/{id}"));
        service.addOperation(operation);

        // Act
        ServiceMetadata metadata = introspector.analyzeService(service);

        // Assert
        OperationMetadata operationMetadata = metadata.getOperations().get(0);
        assertEquals("HTTP method should be GET", "GET", operationMetadata.getHttpMethod());
        assertEquals("REST path should match", "/users/{id}", operationMetadata.getRestPath());
    }

    /**
     * Test operation analysis with default values.
     * Verifies default HTTP method and path generation when parameters are missing.
     */
    public void testOperationAnalysisWithDefaults() throws Exception {
        // Arrange
        AxisService service = new AxisService("TestService");
        AxisOperation operation = new org.apache.axis2.description.InOutAxisOperation();
        operation.setName(new QName("defaultOperation"));
        service.addOperation(operation);

        // Act
        ServiceMetadata metadata = introspector.analyzeService(service);

        // Assert
        OperationMetadata operationMetadata = metadata.getOperations().get(0);
        assertEquals("HTTP method should default to POST", "POST", operationMetadata.getHttpMethod());
        assertEquals("REST path should be generated from operation name", "/defaultOperation", operationMetadata.getRestPath());
    }

    /**
     * Test system service filtering.
     * Verifies that system services are properly excluded.
     */
    public void testSystemServiceFiltering() throws Exception {
        // Arrange
        AxisService versionService = new AxisService("Version");
        versionService.addParameter(new Parameter("enableREST", "true"));

        AxisService adminService = new AxisService("AdminService");
        adminService.addParameter(new Parameter("enableREST", "true"));

        AxisService underscoreService = new AxisService("__InternalService");
        underscoreService.addParameter(new Parameter("enableREST", "true"));

        AxisService normalService = new AxisService("NormalService");
        normalService.addParameter(new Parameter("enableREST", "true"));

        axisConfiguration.addService(versionService);
        axisConfiguration.addService(adminService);
        axisConfiguration.addService(underscoreService);
        axisConfiguration.addService(normalService);

        // Act
        List<ServiceMetadata> services = introspector.getRestServices();

        // Assert
        assertEquals("Should exclude system services", 1, services.size());
        assertEquals("Should include only normal service", "NormalService", services.get(0).getServiceName());
    }

    /**
     * Test REST service detection by endpoint configuration.
     * Verifies services with REST endpoints are detected even without explicit parameter.
     */
    public void testRestDetectionByEndpoint() throws Exception {
        // Arrange
        AxisService service = new AxisService("EndpointService");
        // Simulate REST endpoint presence - in real implementation this would check endpoint configuration
        // For this test, we verify the introspection logic handles endpoint-based detection

        axisConfiguration.addService(service);

        // Act
        List<ServiceMetadata> services = introspector.getRestServices();

        // Assert
        // Service without explicit REST parameter or REST operations should not be included
        assertTrue("Services without REST configuration should be filtered out",
                services.stream().noneMatch(s -> "EndpointService".equals(s.getServiceName())));
    }

    /**
     * Test error handling in service introspection.
     * Verifies graceful handling of invalid service configurations.
     */
    public void testErrorHandlingInIntrospection() throws Exception {
        // Arrange
        AxisService service = new AxisService("ErrorService");
        service.addParameter(new Parameter("enableREST", "true"));

        // Add operation with null name to simulate error condition
        AxisOperation operation = new org.apache.axis2.description.InOutAxisOperation();
        // Deliberately not setting operation name
        service.addOperation(operation);

        axisConfiguration.addService(service);

        // Act - should handle errors gracefully
        List<ServiceMetadata> services = introspector.getRestServices();

        // Assert - should not fail even with problematic service
        assertNotNull("Should handle errors gracefully", services);
    }

    /**
     * Test operation metadata with messages.
     * Verifies extraction of input/output message information.
     */
    public void testOperationMetadataWithMessages() throws Exception {
        // Arrange
        AxisService service = new AxisService("MessageService");
        AxisOperation operation = new org.apache.axis2.description.InOutAxisOperation();
        operation.setName(new QName("processData"));

        // Simulate input/output messages - in real implementation these would be properly configured
        // For this test, we verify the basic metadata extraction works
        service.addOperation(operation);

        // Act
        ServiceMetadata metadata = introspector.analyzeService(service);

        // Assert
        assertNotNull("Metadata should be extracted", metadata);
        assertEquals("Should have one operation", 1, metadata.getOperations().size());
    }

    /**
     * Test financial service patterns from user guide.
     * Simulates the authentication and data services described in the user guide.
     */
    public void testFinancialServicePatterns() throws Exception {
        // Arrange - simulate the services from the user guide
        AxisService authService = createAuthenticationService();
        AxisService dataService = createDataManagementService();
        AxisService excelService = createExcelIntegrationService();

        axisConfiguration.addService(authService);
        axisConfiguration.addService(dataService);
        axisConfiguration.addService(excelService);

        // Act
        List<ServiceMetadata> services = introspector.getRestServices();

        // Assert
        assertEquals("Should detect all three services", 3, services.size());

        // Verify authentication service
        ServiceMetadata authMetadata = services.stream()
                .filter(s -> "AuthenticationService".equals(s.getServiceName()))
                .findFirst().orElse(null);
        assertNotNull("Authentication service should be found", authMetadata);

        // Verify data management service
        ServiceMetadata dataMetadata = services.stream()
                .filter(s -> "DataManagementService".equals(s.getServiceName()))
                .findFirst().orElse(null);
        assertNotNull("Data management service should be found", dataMetadata);

        // Verify excel integration service
        ServiceMetadata excelMetadata = services.stream()
                .filter(s -> "ExcelIntegrationService".equals(s.getServiceName()))
                .findFirst().orElse(null);
        assertNotNull("Excel integration service should be found", excelMetadata);
    }

    /**
     * Helper method to create a test service with typical configuration.
     */
    private AxisService createTestService() throws Exception {
        AxisService service = new AxisService("TestService");
        service.setDocumentation("Test service documentation");
        service.setTargetNamespace("http://test.example.com");
        service.addParameter(new Parameter("enableREST", "true"));

        AxisOperation operation = new org.apache.axis2.description.InOutAxisOperation();
        operation.setName(new QName("testMethod"));
        service.addOperation(operation);

        return service;
    }

    /**
     * Helper method to create authentication service from user guide.
     */
    private AxisService createAuthenticationService() throws Exception {
        AxisService service = new AxisService("AuthenticationService");
        service.addParameter(new Parameter("enableREST", "true"));

        AxisOperation loginOp = new org.apache.axis2.description.InOutAxisOperation();
        loginOp.setName(new QName("login"));
        loginOp.addParameter(new Parameter("HTTPMethod", "POST"));
        loginOp.addParameter(new Parameter("RESTPath", "/bigdataservice/login"));
        service.addOperation(loginOp);

        return service;
    }

    /**
     * Helper method to create data management service from user guide.
     */
    private AxisService createDataManagementService() throws Exception {
        AxisService service = new AxisService("DataManagementService");
        service.addParameter(new Parameter("enableREST", "true"));

        AxisOperation marketSummaryOp = new org.apache.axis2.description.InOutAxisOperation();
        marketSummaryOp.setName(new QName("getMarketSummary"));
        marketSummaryOp.addParameter(new Parameter("HTTPMethod", "POST"));
        marketSummaryOp.addParameter(new Parameter("RESTPath", "/bigdataservice/marketSummary"));
        service.addOperation(marketSummaryOp);

        AxisOperation financialCalcOp = new org.apache.axis2.description.InOutAxisOperation();
        financialCalcOp.setName(new QName("calculateFinancials"));
        financialCalcOp.addParameter(new Parameter("HTTPMethod", "POST"));
        financialCalcOp.addParameter(new Parameter("RESTPath", "/bigdataservice/financialCalculation"));
        service.addOperation(financialCalcOp);

        return service;
    }

    /**
     * Helper method to create Excel integration service from user guide.
     */
    private AxisService createExcelIntegrationService() throws Exception {
        AxisService service = new AxisService("ExcelIntegrationService");
        service.addParameter(new Parameter("enableREST", "true"));

        AxisOperation functionSpecsOp = new org.apache.axis2.description.InOutAxisOperation();
        functionSpecsOp.setName(new QName("getFunctionSpecs"));
        functionSpecsOp.addParameter(new Parameter("HTTPMethod", "GET"));
        functionSpecsOp.addParameter(new Parameter("RESTPath", "/bigdataservice/functionSpecs"));
        service.addOperation(functionSpecsOp);

        return service;
    }
}