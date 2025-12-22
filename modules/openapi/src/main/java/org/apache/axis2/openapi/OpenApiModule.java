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
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * Apache Axis2 OpenAPI/Swagger integration module.
 *
 * This module provides automatic OpenAPI specification generation and Swagger UI support
 * for Axis2 REST services. It integrates with the Axis2 transport layer to serve
 * OpenAPI documentation at standard endpoints.
 *
 * Key features:
 * - Automatic OpenAPI 3.0.1 specification generation from service metadata
 * - Swagger UI integration for interactive API documentation
 * - Support for REST service introspection and annotation processing
 * - Integration with Axis2's existing metadata query mechanisms
 */
public class OpenApiModule implements Module {

    private static final Log log = LogFactory.getLog(OpenApiModule.class);

    /**
     * Initialize the OpenAPI module.
     *
     * This method is called when the module is loaded and initializes the OpenAPI
     * integration components including specification generation and UI serving.
     */
    @Override
    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
        log.info("Initializing Apache Axis2 OpenAPI module");

        try {
            // Register OpenAPI specification generator
            OpenApiSpecGenerator specGenerator = new OpenApiSpecGenerator(configContext);
            configContext.setProperty("axis2.openapi.generator", specGenerator);

            // Register Swagger UI handler
            SwaggerUIHandler uiHandler = new SwaggerUIHandler(configContext);
            configContext.setProperty("axis2.openapi.ui", uiHandler);

            // Initialize OpenAPI service introspector
            ServiceIntrospector introspector = new ServiceIntrospector(configContext);
            configContext.setProperty("axis2.openapi.introspector", introspector);

            log.info("OpenAPI module initialization completed successfully");

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
     */
    @Override
    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        String serviceName = axisDescription.getClass().getSimpleName();
        if (axisDescription instanceof org.apache.axis2.description.AxisService) {
            serviceName = ((org.apache.axis2.description.AxisService) axisDescription).getName();
        }

        log.debug("OpenAPI module engaged to: " + serviceName);

        // Validate that the service supports REST operations for OpenAPI generation
        if (axisDescription.getParameter("enableREST") == null) {
            log.warn("Service " + serviceName +
                    " does not have REST enabled - OpenAPI documentation may be limited");
        }
    }

    /**
     * Shutdown the OpenAPI module and clean up resources.
     */
    @Override
    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
        log.info("Shutting down Apache Axis2 OpenAPI module");

        try {
            // Clean up registered components
            configurationContext.removeProperty("axis2.openapi.generator");
            configurationContext.removeProperty("axis2.openapi.ui");
            configurationContext.removeProperty("axis2.openapi.introspector");

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
}